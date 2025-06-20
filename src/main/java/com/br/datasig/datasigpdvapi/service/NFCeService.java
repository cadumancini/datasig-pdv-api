package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.ConsultaNotaFiscal;
import com.br.datasig.datasigpdvapi.entity.ParamsImpressao;
import com.br.datasig.datasigpdvapi.entity.RetornoNFCe;
import com.br.datasig.datasigpdvapi.entity.SitEdocsResponse;
import com.br.datasig.datasigpdvapi.exceptions.NfceException;
import com.br.datasig.datasigpdvapi.exceptions.ResourceNotFoundException;
import com.br.datasig.datasigpdvapi.exceptions.WebServiceRuntimeException;
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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
public class NFCeService extends WebServiceRequestsService {
    private static final Logger logger = LoggerFactory.getLogger(NFCeService.class);
    private final String chaveLocal;
    private final String dirNfcLocal;
    private final boolean isLive;

    private static final ConcurrentHashMap<String, ReentrantLock> LOCKS_BY_SNFNFC = new ConcurrentHashMap<>();

    public NFCeService(Environment env) {
        chaveLocal = env.getProperty("chaveNfc");
        dirNfcLocal = env.getProperty("dirNfc");
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

    public byte[] loadInvoiceFromDisk(String token, String nfce) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException {
        if (isLive) {
            ParamsImpressao paramsImpressao = TokensManager.getInstance().getParamsImpressaoFromToken(token);
            forceInvoiceFileToDisk(paramsImpressao, nfce);
            logger.info("Nota {} baixada. Seguindo para download.", nfce);
            String dirNfc = paramsImpressao.getDirNfc();
            return loadFromDisk(nfce, dirNfc);
        } else {
            return loadFromDisk(chaveLocal, dirNfcLocal);
        }
    }

    public void forceInvoiceFileToDisk(ParamsImpressao paramsImpressao, String chave) throws ParserConfigurationException, IOException, SAXException, SOAPClientException, TransformerException {
        Map<String, Object> params = getParamsForImpressaoSDE(paramsImpressao, chave);

        String xml = soapClient.requestFromSdeWS(paramsImpressao.getUrlSde(), "Imprimir", params);
        XmlUtils.validateXmlResponse(xml);
    }

    private static Map<String, Object> getParamsForImpressaoSDE(ParamsImpressao paramsImpressao, String chave) {
        Map<String, Object> params = new HashMap<>();
        params.put("nfe:usuario", paramsImpressao.getLogNfc());
        params.put("nfe:senha", paramsImpressao.getSenNfc());
        params.put("nfe:tipoDocumento", paramsImpressao.getTipDoc());
        params.put("nfe:chaveDocumento", chave);
        return params;
    }

    private byte[] loadFromDisk(String chave, String dirNfc) {
        logger.info("Carregando PDF da nota com chave {}", chave);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dirNfc),
                path -> path.getFileName().toString().contains(chave) && path.getFileName().toString().endsWith(".pdf"))) {

            for (Path file : stream) {
                logger.info("Arquivo encontrado: {}", file.getFileName());
                return Files.readAllBytes(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new ResourceNotFoundException("Arquivo PDF da nota " + chave + " não encontrado no diretório " + dirNfc + ".");
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
}
