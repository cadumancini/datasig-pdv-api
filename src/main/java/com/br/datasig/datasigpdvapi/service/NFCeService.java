package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.*;
import com.br.datasig.datasigpdvapi.exceptions.NfceException;
import com.br.datasig.datasigpdvapi.exceptions.ResourceNotFoundException;
import com.br.datasig.datasigpdvapi.exceptions.WebServiceRuntimeException;
import com.br.datasig.datasigpdvapi.service.nfce.NFCeManager;
import com.br.datasig.datasigpdvapi.service.pedidos.DescontosProcessor;
import com.br.datasig.datasigpdvapi.service.pedidos.ParcelaParametro;
import com.br.datasig.datasigpdvapi.service.pedidos.PedidoUtils;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
public class NFCeService extends WebServiceRequestsService {
    private static final Logger logger = LoggerFactory.getLogger(NFCeService.class);
    private final boolean isLive;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private static final String IDENTIFICADOR_GERADOR = "ERP Senior";
    private static final ConcurrentHashMap<String, ReentrantLock> LOCKS_BY_SNFNFC = new ConcurrentHashMap<>();

    public NFCeService(Environment env) {
        isLive = env.getProperty("environment").equals("live");
    }

    public RetornoNFCe createNFCe(String token, String numPed) throws ParserConfigurationException, IOException, SAXException, SOAPClientException, NfceException, TransformerException {
        String regFat = TokensManager.getInstance().getParamsPDVFromToken(token).getRegFat();
        Map<String, Object> paramsNFCe = prepareParamsForGeracaoNFCe(token, numPed, regFat);

        String nfceResponse = processNFCeSynchronously(token, numPed, paramsNFCe);
        validateNfceResponse(nfceResponse);

        return buildRetornoNFCe(token, nfceResponse);
    }

    private String processNFCeSynchronously(String token, String numPed, Map<String, Object> paramsNFCe) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        String nfceResponse;

        String snfNfc = getSnfNfcForLock(token);
        ReentrantLock lock = LOCKS_BY_SNFNFC.computeIfAbsent(snfNfc, key -> new ReentrantLock());
        lock.lock();
        try {
            logger.info("Iniciando chamada para gerar NFCe com pedido {}. Thread: {}", numPed, Thread.currentThread().getName());
            nfceResponse = exeRegra(token, paramsNFCe);
            logger.info("Finalizando chamada de geração de NFCe com pedido {}", numPed);
        } finally {
            lock.unlock();
            LOCKS_BY_SNFNFC.remove(snfNfc, lock);
        }
        return nfceResponse;
    }

    private String getSnfNfcForLock(String token) {
        String snfNfc = TokensManager.getInstance().getParamsImpressaoFromToken(token).getSnfNfc().trim();
        snfNfc = snfNfc.isBlank() ? TokensManager.getInstance().getParamsPDVFromToken(token).getSnfNfc() : snfNfc;
        return snfNfc;
    }

    private RetornoNFCe buildRetornoNFCe(String token, String nfceResponse) {
        String nfce = extractNfceNumberFromResponse(nfceResponse);
        String pdf = extractNfceKeyFromResponse(nfceResponse);
        String printer = isLive ? TokensManager.getInstance().getParamsImpressaoFromToken(token).getNomImp() : "";

        return new RetornoNFCe(nfce, printer, pdf);
    }

    private String downloadPDFBase64(ParamsImpressao paramsImpressao, String chave) throws ParserConfigurationException, IOException, SAXException, SOAPClientException, TransformerException {
        Map<String, Object> params = getParamsForImpressaoSDE(paramsImpressao, chave);

        String xml = soapClient.requestFromSdeWS(paramsImpressao.getUrlSde() + "Download?wsdl", "BaixarPdf", params, true, "", "", "http://www.senior.com.br/nfe/IDownloadServico/BaixarPdf");
        XmlUtils.validateXmlResponse(xml);
        return getPdfStringBase64(xml);
    }

    private String getPdfStringBase64(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "BaixarPdfResult");
        Node nNode = nList.item(0);
        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            return DownloadNFCeResult.fromXml(nNode).getPdf();
        }
        throw new WebServiceRuntimeException("Erro ao converter retorno do WebService de Download do PDF em Base64");
    }

    private Map<String, Object> getParamsForImpressaoSDE(ParamsImpressao paramsImpressao, String chave) {
        Map<String, Object> params = new HashMap<>();
        params.put("nfe:usuario", paramsImpressao.getLogNfc());
        params.put("nfe:senha", paramsImpressao.getSenNfc());
        params.put("nfe:tipoDocumento", paramsImpressao.getTipDoc());
        params.put("nfe:chaveDocumento", chave);
        params.put("nfe:chave", chave);
        return params;
    }

    private void validateNfceResponse(String nfceResponse) {
        if (!nfceResponse.startsWith("NFC") || !nfceResponse.contains("CHAVE")) {
            logger.error(nfceResponse);
            throw new NfceException(nfceResponse);
        }
    }

    private Map<String, Object> prepareParamsForGeracaoNFCe(String token, String numPed, String numReg) {
        Map<String, Object> params = getBaseParams(token, numReg);

        params.put("aNumPedPdv", numPed);
        return params;
    }

    private String extractNfceNumberFromResponse(String response) {
        String[] terms = response.split(" ");
        return terms[1];
    }

    private String extractNfceKeyFromResponse(String response) {
        String[] terms = response.split(" ");
        return terms[4];
    }

    private String getResponseNFCeFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "result");
        if (nList.getLength() == 1) {
            Element element = (Element) nList.item(0);
            return element.getElementsByTagName("resultado").item(0).getTextContent();
        } else {
            throw new WebServiceRuntimeException("Erro na operação com NFC-e");
        }
    }

    public List<ConsultaNotaFiscal> getNFCes(String token, String numNfv, String sitNfv, String sitDoe, String datIni,
                                             String datFim, String codRep, String nomUsu)
            throws SOAPClientException, ParserConfigurationException, IOException, SAXException, ParseException, TransformerException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, Object> params = prepareBaseParams(codEmp, codFil);
        addParamsForConsultaNFCes(params, numNfv, sitNfv, sitDoe, datIni, datFim, nomUsu);

        String xml = soapClient.requestFromSeniorWS("PDV_DS_ConsultaNotaFiscal", "Consultar", token, "0", params, false);
        XmlUtils.validateXmlResponse(xml);

        List<ConsultaNotaFiscal> notas = getNotasFromXml(xml);
        if (codRep != null && !codRep.trim().isEmpty()) {
            notas = filtrarNotasPorCodRep(notas, codRep);
        }
        if (!notas.isEmpty()) return notas;
        throw new ResourceNotFoundException("Nenhuma NFC-e encontrada!");
    }

    private List<ConsultaNotaFiscal> filtrarNotasPorCodRep(List<ConsultaNotaFiscal> notas, String codRep) {
        List<String> reps = new ArrayList<>(Arrays.asList(codRep.split(",")));
        List<String> repsToFilter = reps.stream().map(String::trim).collect(Collectors.toList());
        return notas.stream().filter(nota -> repsToFilter.contains(nota.getCodRep())).collect(Collectors.toList());
    }

    private void addParamsForConsultaNFCes(HashMap<String, Object> params, String numNfv, String sitNfv, String sitDoe, String datIni, String datFim, String nomUsu) {
        params.put("numNfv", evaluateValue(numNfv));
        params.put("sitNfv", evaluateValue(sitNfv));
        params.put("sitDoe", evaluateValue(sitDoe));
        params.put("datIni", evaluateValue(datIni));
        params.put("datFim", evaluateValue(datFim));
        params.put("nomUsu", evaluateValue(nomUsu));
    }

    private String evaluateValue(String value) {
        return value == null ? "" : value;
    }

    private List<ConsultaNotaFiscal> getNotasFromXml(String xml) throws ParserConfigurationException, IOException, SAXException, ParseException {
        List<ConsultaNotaFiscal> notas = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "dados");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                notas.add(ConsultaNotaFiscal.fromXml(nNode));
            }
        }
        notas.sort(Comparator.comparing(ConsultaNotaFiscal::getNumNfvInt).reversed());
        return notas;
    }

    public String cancelarNFCe(String token, String codSnf, String numNfv, String jusCan) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        String regCan = TokensManager.getInstance().getParamsPDVFromToken(token).getRegCan();
        Map<String, Object> paramsCancelamento = preparaParamsForCancelarNFCe(token, codSnf, numNfv, jusCan, regCan);
        return exeRegra(token, paramsCancelamento);
    }

    private Map<String, Object> preparaParamsForCancelarNFCe(String token, String codSnf, String numNfv, String jusCan, String numReg) {
        Map<String, Object> params = getBaseParams(token, numReg);
        params.put("aCodSnfPDV", codSnf);
        params.put("aNumNfvPDV", numNfv);
        params.put("aJusCanPDV", jusCan);
        return params;
    }

    public String inutilizarNFCe(String token, String codSnf, String numNfv, String jusCan) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        String regInu = TokensManager.getInstance().getParamsPDVFromToken(token).getRegInu();
        Map<String, Object> paramsInutilizacao = preparaParamsForCancelarNFCe(token, codSnf, numNfv, jusCan, regInu);
        return exeRegra(token, paramsInutilizacao);
    }

    private String exeRegra(String token, Map<String, Object> params) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        String xml = soapClient.requestFromSeniorWSSID("com_senior_g5_co_ger_sid", "Executar", token, "0", params);
        XmlUtils.validateXmlResponse(xml);
        return getResponseNFCeFromXml(xml);
    }

    private Map<String, Object> getBaseParams(String token, String numReg) {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);

        Map<String, Object> params = new HashMap<>();
        params.put("acao", "sid.srv.regra");
        params.put("numreg", numReg);
        params.put("aCodEmpPdv", codEmp);
        params.put("aCodFilPdv", codFil);
        return params;
    }

    public SitEdocsResponse getSitEDocs(String token, String codSnf, String numNfv) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, ParseException, TransformerException {
        String regRet = TokensManager.getInstance().getParamsPDVFromToken(token).getRegRet();
        Map<String, Object> paramsNFCe = prepareParamsForConsultaEDocs(token, codSnf, numNfv, regRet);
        String response = exeRegra(token, paramsNFCe);
        ConsultaNotaFiscal notaFiscal = getNFCes(token, numNfv, null, null, null, null, null, null).get(0);
        return new SitEdocsResponse(response, notaFiscal);
    }

    private Map<String, Object> prepareParamsForConsultaEDocs(String token, String codSnf, String numNfv, String numReg) {
        Map<String, Object> params = getBaseParams(token, numReg);
        params.put("aCodSnfPDV", codSnf);
        params.put("aNumNfvPDV", numNfv);

        return params;
    }

    public String loadInvoiceBase64(String token, String nfce) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException {
        ParamsImpressao paramsImpressao = TokensManager.getInstance().getParamsImpressaoFromToken(token);
        return downloadPDFBase64(paramsImpressao, nfce);
    }

    public RetornoNFCe createNFCeNoOrder(String token, PayloadPedido pedido, String clientIP) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException {
        ParamsImpressao paramsImpressao = TokensManager.getInstance().getParamsImpressaoFromToken(token);
        UltimoNumNFC numNfc = getNFCNumber(token);
        NFCeManager.getInstance().addNFCE(numNfc.getUltNum());
        criarNFC(token, pedido, numNfc.getUltNum(), clientIP);
        fecharNFC(token, pedido, numNfc.getUltNum(), clientIP);
        String chave = consultarSituacaoNFC(paramsImpressao, numNfc, token);
        NFCeManager.getInstance().removeNFCE(numNfc.getUltNum());
        String printer = isLive ? TokensManager.getInstance().getParamsImpressaoFromToken(token).getNomImp() : "";
        return new RetornoNFCe(numNfc.getUltNum(), printer, chave);
    }

    private UltimoNumNFC getNFCNumber(String token) throws ParserConfigurationException, IOException, SAXException, SOAPClientException, TransformerException {
        Map<String, Object> params = getParamsForNFCNumber(token);
        String xml = soapClient.requestFromSeniorWS("PDV_DS_ConsultaNumeroNFC", "NumeroNFC", token, "0", params, false);
        XmlUtils.validateXmlResponse(xml);
        return getUltimoNumNFC(xml);
    }

    private static UltimoNumNFC getUltimoNumNFC(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "ultimoNumero");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                return UltimoNumNFC.fromXml(nNode);
            }
        }
        return null;
    }

    private Map<String, Object> getParamsForNFCNumber(String token) {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        String codSnf = TokensManager.getInstance().getParamsImpressaoFromToken(token).getSnfNfc().trim();

        Map<String, Object> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        params.put("codSnf", codSnf);

        return params;
    }

    private void criarNFC(String token, PayloadPedido pedido, String numNfc, String clientIP) throws SOAPClientException, ParserConfigurationException, TransformerException, IOException, SAXException {
        HashMap<String, Object> params = createParamsCriarNFC(token, pedido, numNfc, clientIP);
        String xml = makeRequest(token, params);
        XmlUtils.validateXmlResponse(xml);
    }

    private void fecharNFC(String token, PayloadPedido pedido, String numNfc, String clientIP) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException {
        HashMap<String, Object> params = createParamsFecharNFC(token, pedido, numNfc, clientIP);
        String xml = makeRequest(token, params);
        XmlUtils.validateXmlResponse(xml);
    }

    private String makeRequest(String token, HashMap<String, Object> params) throws SOAPClientException, ParserConfigurationException, TransformerException {
        return soapClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_notafiscal", "GravarNotasFiscaisSaida_16", token, "0", params, false);
    }

    private HashMap<String, Object> createParamsCriarNFC(String token, PayloadPedido pedido, String numNfc, String clientIP) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("tipoProcessamento", "1");
        params.put("prCallMode", "1");
        params.put("tipCal", "0");
        params.put("gerPar", "0");
        params.put("fecNot", "2");
        params.put("cstPar", "1");
        params.put("gerarDocumentoEletronico", "0");
        params.put("dadosGerais", definirParamsDadosGerais(token, pedido, numNfc, true, clientIP));

        return params;
    }

    private HashMap<String, Object> createParamsFecharNFC(String token, PayloadPedido pedido, String numNfc, String clientIP) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("tipoProcessamento", "2");
        params.put("prCallMode", "1");
        params.put("tipCal", "1");
        params.put("gerPar", "0");
        params.put("fecNot", "1");
        params.put("cstPar", "1");
        params.put("gerarDocumentoEletronico", "0");
        params.put("dadosGerais", definirParamsDadosGerais(token, pedido, numNfc, false, clientIP));

        return params;
    }

    private HashMap<String, Object> definirParamsDadosGerais(String token, PayloadPedido pedido, String numNfc, boolean addParcelas, String clientIP) {
        Date dataAtual = new Date();

        HashMap<String, Object> dadosGerais = new HashMap<>();
        dadosGerais.put("codEmp", TokensManager.getInstance().getCodEmpFromToken(token));
        dadosGerais.put("codFil", TokensManager.getInstance().getCodFilFromToken(token));
        dadosGerais.put("codSnf", TokensManager.getInstance().getParamsImpressaoFromToken(token).getSnfNfc().trim());
        dadosGerais.put("numNfv", numNfc);
        dadosGerais.put("codEdc", "65");
        dadosGerais.put("tnsPro", TokensManager.getInstance().getParamsPDVFromToken(token).getTnsNfv());
        dadosGerais.put("datEmi", dateFormat.format(dataAtual));
        dadosGerais.put("codCli", PedidoUtils.definirCodCli(pedido.getCodCli(), token));
        dadosGerais.put("codRep", pedido.getCodRep());
        dadosGerais.put("codCpg", TokensManager.getInstance().getParamsPDVFromToken(token).getCodCpg());
        dadosGerais.put("codFpg", TokensManager.getInstance().getParamsPDVFromToken(token).getCodFpg());
        dadosGerais.put("datSai", dateFormat.format(dataAtual));
        dadosGerais.put("cifFob", "X");
        dadosGerais.put("usuario", PedidoUtils.getCamposUsuario(pedido, clientIP));
        dadosGerais.put("produtos", definirParamsItens(pedido));
        if (addParcelas) dadosGerais.put("parcelas", definirParamsParcelas(pedido));

        return dadosGerais;
    }

    private List<HashMap<String, Object>> definirParamsItens(PayloadPedido pedido) {
        setSeqIpdToItems(pedido);
        var descontosMap = DescontosProcessor.calcularDescontos(pedido);
        List<HashMap<String, Object>> itens = new ArrayList<>();
        for (var itemPedido : pedido.getItens()) {
            HashMap<String, Object> paramsItem = new HashMap<>();
            paramsItem.put("seqIpv", itemPedido.getSeqIpd());
            paramsItem.put("tnsPro", itemPedido.getTnsNfv());
            paramsItem.put("codPro", itemPedido.getCodPro());
            paramsItem.put("codDer", itemPedido.getCodDer());
            paramsItem.put("codDep", itemPedido.getCodDep());
            paramsItem.put("qtdFat", PedidoUtils.normalizeQtdPed(itemPedido.getQtdPed()));
            paramsItem.put("codTpr", itemPedido.getCodTpr());
            String vlrDsc = descontosMap.get(itemPedido.getSeqIpd()).getDescontoTotalStr();
            paramsItem.put("vlrDsc", PedidoUtils.formatValue(vlrDsc));
            itens.add(paramsItem);
        }

        return itens;
    }

    private static void setSeqIpdToItems(PayloadPedido pedido) {
        int seqIpd = 0;
        for(var item : pedido.getItens()) {
            seqIpd++;
            item.setSeqIpd(String.valueOf(seqIpd));
        }
    }

    private List<HashMap<String, Object>> definirParamsParcelas(PayloadPedido pedido) {
        List<HashMap<String, Object>> parcelas = new ArrayList<>();
        int seqPar = 0;
        int seqParCpg;
        for(PagamentoPedido pagto : pedido.getPagamentos()) {
            seqParCpg = 0;
            Date dataParcela = new Date();
            ParcelaParametro parcelaParametro = ParcelaParametro.definirValorParcela(pagto);
            pagto.getCondicao().getParcelas().sort(Comparator.comparing(Parcela::getSeqIcp));
            List<HashMap<String, Object>> pacelasPagto = new ArrayList<>();
            for (Parcela parcela : pagto.getCondicao().getParcelas()) {
                for (int i = 0; i < parcela.getQtdPar(); i++) {
                    seqPar++;
                    seqParCpg++;
                    dataParcela = PedidoUtils.definirDataParcela(dataParcela, parcela.getDiaPar());
                    HashMap<String, Object> paramsParcela = new HashMap<>();
                    paramsParcela.put("numPar", String.valueOf(seqPar));
                    paramsParcela.put("codFpg", pagto.getForma().getCodFpg());
                    paramsParcela.put("vctPar", dateFormat.format(dataParcela));
                    paramsParcela.put("vctDat", dataParcela);
                    paramsParcela.put("vlrPar", PedidoUtils.getVlrPar(parcelaParametro, seqParCpg, pagto, parcela));
                    paramsParcela.put("tipInt", pagto.getForma().getTipInt());
                    paramsParcela.put("banOpe", pagto.getBanOpe());
                    paramsParcela.put("catTef", pagto.getCatTef());
                    paramsParcela.put("nsuTef", pagto.getNsuTef());
                    paramsParcela.put("cgcCre", pagto.getCgcCre());

                    pacelasPagto.add(paramsParcela);
                }
            }
            PedidoUtils.ajustarValores(pacelasPagto, pagto.getValorPago());
            parcelas.addAll(pacelasPagto);
        }
        PedidoUtils.orderParcelas(parcelas);
        return parcelas;
    }

    private String consultarSituacaoNFC(ParamsImpressao paramsImpressao, UltimoNumNFC numNfc, String token) throws SOAPClientException, ParserConfigurationException, TransformerException, IOException, SAXException {
        HashMap<String, Object> params = getParamsForConsultaSituacaoNFC(paramsImpressao);
        String additionalTag = "nfe:Documentos";
        String additionalParams = "<nfe:IdentificacaoDocumento>";
        additionalParams += "<nfe:IdentificadorGerador>" + IDENTIFICADOR_GERADOR + "</nfe:IdentificadorGerador>";
        additionalParams += "<nfe:CnpjEmissor>" + numNfc.getNumCgc() + "</nfe:CnpjEmissor>";
        additionalParams += "<nfe:Numero>" + numNfc.getUltNum() + "</nfe:Numero>";
        additionalParams += "<nfe:Serie>" + numNfc.getCodSel() + "</nfe:Serie>";
        additionalParams += "</nfe:IdentificacaoDocumento>";

        String xml = soapClient.requestFromSdeWS(paramsImpressao.getUrlSde() + "Integracao?wsdl", "ConsultarSituacaoDocumentos", params, false, additionalTag, additionalParams, "http://www.senior.com.br/nfe/IIntegracaoDocumento/ConsultarSituacaoDocumentos");
//        String xml = soapClient.requestFromSdeWS("http://192.168.11.197:8989/SDE/Integracao?wsdl", "ConsultarSituacaoDocumentos", params, false, additionalTag, additionalParams, "http://www.senior.com.br/nfe/IIntegracaoDocumento/ConsultarSituacaoDocumentos");
        XmlUtils.validateXmlResponse(xml);

        return getChaveFromXml(xml, numNfc.getUltNum(), paramsImpressao, token);
    }

    private String getChaveFromXml(String xml, String numNfc, ParamsImpressao paramsImpressao, String token) throws ParserConfigurationException, IOException, SAXException, SOAPClientException, TransformerException {
        if (xml.contains("<Codigo>999</Codigo>")) // Nota com erro
            consultaMensagemCriticasSde(paramsImpressao, getChaveNfc(numNfc, token));
        else if (xml.contains("<Codigo>602</Codigo>")) // Nota rejeitada
            throw new NfceException(XmlUtils.getTextFromXmlElement(xml, "ConsultarSituacaoDocumentosResult", "Mensagem"));
        else if (xml.contains("<Situacao>5</Situacao>")) // Nota inutilizada
            throw new NfceException("A nota número " + numNfc + " está inutilizada.");
        else if (xml.contains("<Situacao>3</Situacao>")) // Nota cancelada
            throw new NfceException("A nota número " + numNfc + " está cancelada.");

        return XmlUtils.getTextFromXmlElement(xml, "Documento", "ChaveDocumento");
    }

    private String getChaveNfc(String numNfc, String token) throws SOAPClientException, ParserConfigurationException, TransformerException, IOException, SAXException {
        Map<String, Object> params = getParamsForNFCNumber(token);
        params.put("numNfc", numNfc);
        String xml = soapClient.requestFromSeniorWS("PDV_DS_ConsultaChave", "Consultar", token, "0", params, false);
        XmlUtils.validateXmlResponse(xml);
        return XmlUtils.getTextFromXmlElement(xml, "result", "chvNfc");
    }

    private String consultaMensagemCriticasSde(ParamsImpressao paramsImpressao, String chave) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException {
        HashMap<String, Object> params = getParamsForConsultaCriticasNFC(paramsImpressao);
        String additionalTag = "nfe:Identificadores";
        String additionalParams = "<arr:string>";
        additionalParams += chave;
        additionalParams += "</arr:string>";

        String xml = soapClient.requestFromSdeWS(paramsImpressao.getUrlSde() + "Integracao?wsdl", "ObterCriticasPorIdentificador", params, false, additionalTag, additionalParams, "http://www.senior.com.br/nfe/IIntegracaoDocumento/ObterCriticasPorIdentificador");
//        String xml = soapClient.requestFromSdeWS("http://192.168.11.197:8989/SDE/Integracao?wsdl", "ObterCriticasPorIdentificador", params, false, additionalTag, additionalParams, "http://www.senior.com.br/nfe/IIntegracaoDocumento/ObterCriticasPorIdentificador");
        String msgCritica = XmlUtils.getTextFromXmlElement(xml, "CriticaIntegracaoRetorno", "Critica", "http://schemas.datacontract.org/2004/07/Senior.SapiensNfe.DataAccess.Dados.Documento");
        msgCritica = msgCritica == null ? XmlUtils.getTextFromXmlElement(xml, "ObterCriticasPorIdentificadorResult","Mensagem") : msgCritica;
        throw new NfceException(msgCritica);
    }

    private HashMap<String, Object> getParamsForConsultaSituacaoNFC(ParamsImpressao paramsImpressao) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("nfe:usuario", paramsImpressao.getLogNfc());
        params.put("nfe:senha", paramsImpressao.getSenNfc());
        params.put("nfe:tipoDocumento", paramsImpressao.getTipDoc());
        params.put("nfe:tipoProcessamento", "1");
        return params;
    }

    private HashMap<String, Object> getParamsForConsultaCriticasNFC(ParamsImpressao paramsImpressao) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("nfe:usuario", paramsImpressao.getLogNfc());
        params.put("nfe:senha", paramsImpressao.getSenNfc());
        params.put("nfe:tipoDocumento", paramsImpressao.getTipDoc());
        return params;
    }
}
