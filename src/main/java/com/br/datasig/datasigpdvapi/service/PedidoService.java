package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.*;
import com.br.datasig.datasigpdvapi.exceptions.OrderException;
import com.br.datasig.datasigpdvapi.exceptions.WebServiceRuntimeException;
import com.br.datasig.datasigpdvapi.service.pedidos.ParcelaParametro;
import com.br.datasig.datasigpdvapi.service.pedidos.PedidoUtils;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PedidoService extends WebServiceRequestsService {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public RetornoPedido createPedido(String token, PayloadPedido pedido, String clientIP) throws ParserConfigurationException, IOException, SAXException, SOAPClientException, TransformerException {
        setEmpFilToPedido(pedido, token);
        if(pedido.getNumPed().equals("0")) {
            RetornoPedido retornoPedido = sendPedidoRequest(pedido, token, clientIP);
            if (pedido.isFechar() || pedido.isGerar()) {
                pedido.setNumPed(retornoPedido.getNumPed());
                fecharPedido(pedido, token, false, clientIP);
            }
            return retornoPedido;
        } else {
            return handlePedidoExistente(pedido, token, clientIP);
        }
    }

    private RetornoPedido sendPedidoRequest(PayloadPedido pedido, String token, String clientIP) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        HashMap<String, Object> params = prepareParamsForPedido(pedido, token, clientIP);
        String xml = makeRequest(token, params);
        XmlUtils.validateXmlResponse(xml);
        RetornoPedido retornoPedido = getRetornoPedidoFromXml(xml);
        validateRetornoPedido(retornoPedido);
        return retornoPedido;
    }

    private String makeRequest(String token, HashMap<String, Object> params) throws SOAPClientException, ParserConfigurationException, TransformerException {
        return soapClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_pedidos", "GravarPedidos_13", token, "0", params, false);
    }

    private RetornoPedido handlePedidoExistente(PayloadPedido pedido, String token, String clientIP) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        if (pedido.isFechar() || pedido.isGerar()) {
            boolean alterarTransacao = pedido.isFechar();
            return fecharPedido(pedido, token, alterarTransacao, clientIP);
        } else {
            return sendPedidoRequest(pedido, token, clientIP);
        }
    }

    private void setEmpFilToPedido(PayloadPedido pedido, String token) {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        pedido.setCodEmp(codEmp);
        pedido.setCodFil(codFil);
    }

    private HashMap<String, Object> prepareParamsForPedido(PayloadPedido pedido, String token, String clientIP) {
        HashMap<String, Object> paramsPedido = new HashMap<>();
        paramsPedido.put("converterQtdUnidadeEstoque", "N");
        paramsPedido.put("converterQtdUnidadeVenda", "N");
        paramsPedido.put("dataBuild", "");
        paramsPedido.put("flowInstanceID", "");
        paramsPedido.put("flowName", "");
        paramsPedido.put("ignorarErrosItens", "N");
        paramsPedido.put("ignorarErrosParcela", "N");
        paramsPedido.put("ignorarErrosPedidos", "N");
        paramsPedido.put("ignorarPedidoBloqueado", "N");
        paramsPedido.put("inserirApenasPedidoCompleto", "S");

        String tnsPed = definirTnsPro(pedido, token);

        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedido.getCodEmp());
        params.put("codFil", pedido.getCodFil());
        params.put("codCli", PedidoUtils.definirCodCli(pedido.getCodCli(), token));
        params.put("codCpg", TokensManager.getInstance().getParamsPDVFromToken(token).getCodCpg());
        params.put("codFpg", TokensManager.getInstance().getParamsPDVFromToken(token).getCodFpg());
        params.put("codRep", pedido.getCodRep());
        params.put("cifFob", "X");
        params.put("indPre", "1");
        if (pedido.getNumPed().equals("0")) {
            params.put("opeExe", "I");
        } else {
            params.put("numPed", pedido.getNumPed());
            params.put("opeExe", "A");
        }
        params.put("tnsPro", tnsPed);
        params.put("temPar", "N");
        params.put("acePar", "N");
        params.put("vlrDar", getVlrDarFormatted(pedido.getVlrDar()));
        params.put("perDs1", getVlrDarFormatted(pedido.getPerDs1()));
        params.put("usuario", getCampoUsuario("USU_CodIp", clientIP));

        if(!pedido.getItens().isEmpty()) {
            List<HashMap<String, Object>> itens = definirParamsItens(pedido, tnsPed);
            params.put("produto", itens);
        }

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    private static String getVlrDarFormatted(Double vlrDar) {
        if (vlrDar == 0) return "0";
        return PedidoUtils.doubleToString(vlrDar);
    }

    private List<HashMap<String, Object>> getCampoUsuario(String campo, String valor) {
        HashMap<String, Object> paramsIP = new HashMap<>();
        paramsIP.put("cmpUsu", campo);
        paramsIP.put("vlrUsu", valor);

        List<HashMap<String, Object>> list = new ArrayList<>();
        list.add(paramsIP);
        return list;
    }

    private String definirTnsPro(PayloadPedido pedido, String token) {
        ParamsPDV params = TokensManager.getInstance().getParamsPDVFromToken(token);
        if (pedido.isFechar()) {
            return params.getTnsPed();
        } else if (pedido.isGerar()) {
            return params.getPedTns();
        } else {
            if (pedido.getTnsPed() != null && !pedido.getTnsPed().isEmpty())
                return pedido.getTnsPed();
            else
                return params.getTnsOrc();
        }
    }

    List<HashMap<String, Object>> definirParamsItens(PayloadPedido pedido, String tnsPed) {
        List<HashMap<String, Object>> listaItens = new ArrayList<>();
        pedido.getItens().forEach(itemPedido -> {
            HashMap<String, Object> paramsItem = new HashMap<>();
            if (itemPedido.isExcluir()) {
                paramsItem.put("seqIpd", itemPedido.getSeqIpd());
                paramsItem.put("opeExe", "E");
            } else {
                paramsItem.put("codPro", itemPedido.getCodPro());
                paramsItem.put("codDer", itemPedido.getCodDer());
                paramsItem.put("qtdPed", PedidoUtils.normalizeQtdPed(itemPedido.getQtdPed()));
                paramsItem.put("codTpr", itemPedido.getCodTpr());
                paramsItem.put("obsIpd", itemPedido.getObsIpd());
                paramsItem.put("vlrDsc", PedidoUtils.formatValue(itemPedido.getVlrDsc()));
                paramsItem.put("perDsc", PedidoUtils.formatValue(itemPedido.getPerDsc()));
                paramsItem.put("perAcr", PedidoUtils.formatValue(itemPedido.getPerAcr()));
                paramsItem.put("codDep", itemPedido.getCodDep());
                paramsItem.put("tnsPro", definirTnsPedItem(pedido, tnsPed, itemPedido));
                paramsItem.put("resEst", "N");
                paramsItem.put("pedPrv", "N");
                if (itemPedido.getSeqIpd().equals("0")) {
                    paramsItem.put("opeExe", "I");
                } else {
                    paramsItem.put("seqIpd", itemPedido.getSeqIpd());
                    paramsItem.put("opeExe", "A");
                }
            }
            listaItens.add(paramsItem);
        });

        return listaItens;
    }

    private String definirTnsPedItem(PayloadPedido pedido, String tnsPedCab, PayloadItemPedido item) {
        if (pedido.isFechar() || pedido.isGerar()) {
            if (item.getTnsPed() != null && !item.getTnsPed().isEmpty())
                return item.getTnsPed();
            else
                return tnsPedCab;
        } else {
            return tnsPedCab;
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
                    paramsParcela.put("opeExe", "I");
                    paramsParcela.put("seqPar", String.valueOf(seqPar));
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

    private RetornoPedido getRetornoPedidoFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "respostaPedido", StandardCharsets.ISO_8859_1);

        if (nList.getLength() == 1) {
            return RetornoPedido.fromXml(nList.item(0));
        } else {
            throw new WebServiceRuntimeException("Erro ao extrair retorno de pedido da resposta do servidor.");
        }
    }

    private void validateRetornoPedido(RetornoPedido retornoPedido) {
        StringBuilder message = new StringBuilder();
        boolean hasErrors = false;
        if (pedidoTemErro(retornoPedido)) {
            hasErrors = true;
            message.append(retornoPedido.getMsgRet()).append("\n");
        }

        for (RetornoItemPedido item : retornoPedido.getItens()) {
            if (!item.getRetorno().equals("OK")) {
                hasErrors = true;
                message.append(item.getRetorno()).append("\n");
            }
        }

        for (RetornoParcela parcela : retornoPedido.getParcelas()) {
            if (!parcela.getRetorno().equals("OK")) {
                hasErrors = true;
                message.append(parcela.getRetorno()).append("\n");
            }
        }

        if (hasErrors) {
            throw new OrderException(message.toString());
        }
    }

    private static boolean pedidoTemErro(RetornoPedido retornoPedido) {
        return !retornoPedido.getMsgRet().equals("OK") ||
                retornoPedido.getNumPed().equals("0");
    }

    private RetornoPedido fecharPedido(PayloadPedido pedido, String token, boolean alterarTransacao, String clientIP) throws ParserConfigurationException, IOException, SAXException, SOAPClientException, TransformerException {
        if (alterarTransacao) {
            alterarTransacao(pedido, token, clientIP);
        }
        return fecharPedido(pedido, token, clientIP);
    }

    private RetornoPedido fecharPedido(PayloadPedido pedido, String token, String clientIP) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        HashMap<String, Object> paramsFecharPedido = prepareParamsForFecharPedido(pedido, clientIP);
        String xml = makeRequest(token, paramsFecharPedido);
        XmlUtils.validateXmlResponse(xml);
        RetornoPedido retornoFecharPedido = getRetornoPedidoFromXml(xml);
        validateRetornoPedido(retornoFecharPedido);
        return retornoFecharPedido;
    }

    private void alterarTransacao(PayloadPedido pedido, String token, String clientIP) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        HashMap<String, Object> paramsAlterarTransacao = prepareParamsForAlterarTransacao(pedido, token, clientIP);
        String xml = makeRequest(token, paramsAlterarTransacao);
        XmlUtils.validateXmlResponse(xml);
        RetornoPedido retornoFecharPedido = getRetornoPedidoFromXml(xml);
        validateRetornoPedido(retornoFecharPedido);
    }

    private HashMap<String, Object> prepareParamsForFecharPedido(PayloadPedido pedido, String clientIP) {
        HashMap<String, Object> paramsPedido = new HashMap<>();

        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedido.getCodEmp());
        params.put("codFil", pedido.getCodFil());
        params.put("numPed", pedido.getNumPed());
        params.put("opeExe", "A");
        params.put("temPar", "S");
        if (pedido.isFechar()) {
            params.put("fecPed", "S");
        }
        params.put("usuario", PedidoUtils.getCamposUsuario(pedido, clientIP));

        List<HashMap<String, Object>> parcelas = definirParamsParcelas(pedido);
        params.put("parcelas", parcelas);

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    private HashMap<String, Object> prepareParamsForAlterarTransacao(PayloadPedido pedido, String token, String clientIP) {
        HashMap<String, Object> paramsPedido = new HashMap<>();

        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedido.getCodEmp());
        params.put("codFil", pedido.getCodFil());
        params.put("opeExe", "A");
        params.put("numPed", pedido.getNumPed());
        params.put("tnsPro", definirTnsPro(pedido, token));
        params.put("usuario", getCampoUsuario("USU_CodIp", clientIP));

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    public List<PedidoConsultavel> getPedidos(String token, BuscaPedidosTipo tipo, BuscaPedidosSituacao situacao,
                                           String order, String numPed, String datIni, String datFim, String codCli,
                                           String codRep, boolean detalhado)
            throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        HashMap<String, Object> paramsPedido = prepareParamsForConsultaPedido(token, tipo, situacao, numPed, datIni, datFim, codCli, codRep);

        String ws = detalhado ? "PDV_DS_ConsultaPedidoDetalhes" : "PDV_DS_ConsultaPedido";
        String xml = soapClient.requestFromSeniorWS(ws, "Consultar", token, "0", paramsPedido, false);
        XmlUtils.validateXmlResponse(xml);

        String tnsOrc = TokensManager.getInstance().getParamsPDVFromToken(token).getTnsOrc();
        List<PedidoConsultavel> pedidos = getConsultaPedidosFromXml(xml, tnsOrc, detalhado);

        if(order.equals("DESC")) pedidos.sort(Comparator.comparing(PedidoConsultavel::getNumPedInt).reversed());
        return pedidos.stream().filter(ped -> !ped.getSitPed().equals("4")).collect(Collectors.toList());
    }

    public ConsultaPedidoDetalhes getPedido(String token, String numPed)
            throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        HashMap<String, Object> paramsPedido = prepareParamsForConsultaPedido(token, numPed);
        String xml = soapClient.requestFromSeniorWS("PDV_DS_ConsultaPedidoDetalhes", "Consultar", token, "0", paramsPedido, false);
        XmlUtils.validateXmlResponse(xml);

        String tnsOrc = TokensManager.getInstance().getParamsPDVFromToken(token).getTnsOrc();
        return getConsultaPedidoDetalhesFromXml(xml, tnsOrc);
    }

    private HashMap<String, Object> prepareParamsForConsultaPedido(String token, BuscaPedidosTipo tipo, BuscaPedidosSituacao situacao,
                                                                   String numPed, String datIni, String datFim, String codCli, String codRep) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", TokensManager.getInstance().getCodEmpFromToken(token));
        params.put("codFil", TokensManager.getInstance().getCodFilFromToken(token));
        params.put("codTns", getTnsPed(token, tipo));
        params.put("sitPed", getSitPed(situacao));
        params.put("numPed", numPed == null ? "" : numPed);
        params.put("datIni", datIni == null ? "" : "'" + datIni + "'");
        params.put("datFim", datFim == null ? "" : "'" + datFim + "'");
        params.put("codCli", codCli == null ? "" : codCli);
        params.put("codRep", codRep == null ? "" : codRep);
        return params;
    }

    private HashMap<String, Object> prepareParamsForConsultaPedido(String token, String numPed) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", TokensManager.getInstance().getCodEmpFromToken(token));
        params.put("codFil", TokensManager.getInstance().getCodFilFromToken(token));
        params.put("numPed", numPed);
        return params;
    }

    private static String getSitPed(BuscaPedidosSituacao situacao) {
        return switch (situacao) {
            case FECHADOS -> "1";
            case CANCELADOS -> "5";
            case ABERTOS -> "9";
            case TODOS -> "";
            case ABERTOS_FECHADOS -> "1,9";
        };
    }

    private static String getTnsPed(String token, BuscaPedidosTipo tipo) {
        return switch (tipo) {
            case ORÇAMENTO -> TokensManager.getInstance().getParamsPDVFromToken(token).getTnsOrc();
            case NORMAL -> TokensManager.getInstance().getParamsPDVFromToken(token).getTnsPed();
            case TODOS -> TokensManager.getInstance().getParamsPDVFromToken(token).getTnsOrc() + "," + TokensManager.getInstance().getParamsPDVFromToken(token).getTnsPed();
        };
    }

    private List<PedidoConsultavel> getConsultaPedidosFromXml(String xml, String tnsOrc, boolean detalhado) throws ParserConfigurationException, IOException, SAXException {
        List<PedidoConsultavel> pedidos = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "dadosGerais");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                if (detalhado)
                    pedidos.add(ConsultaPedidoDetalhes.fromXml(nNode, tnsOrc));
                else
                    pedidos.add(ConsultaPedido.fromXml(nNode, tnsOrc));
            }
        }
        return pedidos;
    }

    private ConsultaPedidoDetalhes getConsultaPedidoDetalhesFromXml(String xml, String tnsOrc) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "dadosGerais");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                return ConsultaPedidoDetalhes.fromXml(nNode, tnsOrc);
            }
        }
        return null;
    }

    public RetornoPedido cancelarPedido(String token, String numPed, String sitPed, String clientIp) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException, ParseException {
        String sitPedCancelado = "5";
        if (sitPed.equals("9")) {
            PayloadPedido pedido = new PayloadPedido();
            pedido.setNumPed(numPed);
            pedido.setCodEmp(TokensManager.getInstance().getCodEmpFromToken(token));
            pedido.setCodFil(TokensManager.getInstance().getCodFilFromToken(token));
            pedido.setFechar(true);
            alterarTransacao(pedido, token, clientIp);
            fecharOrcamentoComObs(pedido, token, clientIp);
        }
        verificarEAjustarParcelas(token, numPed, clientIp);
        return altSituacaoPedido(token, numPed, sitPedCancelado, clientIp);
    }

    public void verificarEAjustarParcelas(String token, String numPed, String clientIP) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException, ParseException {
        ConsultaPedidoDetalhes pedidoDetalhes = getPedido(token, numPed);
        var parcelasAbertas = pedidoDetalhes.getParcelas().stream().filter(parc -> parc.getIndPag().equals("0") || parc.getIndPag().equals(" ")).toList();
        if (!parcelasAbertas.isEmpty()) {
            List<HashMap<String, Object>> parcelasAjustadas = ajustarParcelasAntigas(parcelasAbertas);
            if (!parcelasAjustadas.isEmpty()) {
                atualizarParcelas(pedidoDetalhes, clientIP, parcelasAjustadas, token);
            }
        }
    }

    private void atualizarParcelas(ConsultaPedidoDetalhes pedido, String clientIP, List<HashMap<String, Object>> parcelas, String token) throws SOAPClientException, ParserConfigurationException, TransformerException, IOException, SAXException {
        HashMap<String, Object> paramsAtualizarParcelas = prepareParamsForAtualizarParcelas(pedido, clientIP, parcelas);
        String xml = makeRequest(token, paramsAtualizarParcelas);
        XmlUtils.validateXmlResponse(xml);
        RetornoPedido retornoFecharPedido = getRetornoPedidoFromXml(xml);
        validateRetornoPedido(retornoFecharPedido);
    }

    private HashMap<String, Object> prepareParamsForAtualizarParcelas(ConsultaPedidoDetalhes pedido, String clientIP, List<HashMap<String, Object>> parcelas) {
        HashMap<String, Object> paramsPedido = new HashMap<>();

        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedido.getCodEmp());
        params.put("codFil", pedido.getCodFil());
        params.put("numPed", pedido.getNumPed());
        params.put("opeExe", "A");
        params.put("usuario", getCampoUsuario("USU_CodIp", clientIP));
        params.put("parcelas", parcelas);

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    private List<HashMap<String, Object>> ajustarParcelasAntigas(List<ConsultaParcelaPedido> parcelasAbertas) throws ParseException {
        List<HashMap<String, Object>> parcelas = new ArrayList<>();
        for(var parcela : parcelasAbertas) {
            Date vct = dateFormat.parse(parcela.getVctPar());
            if (vct.before(new Date())) {
                HashMap<String, Object> paramsParcela = new HashMap<>();
                paramsParcela.put("opeExe", "A");
                paramsParcela.put("seqPar", parcela.getSeqPar());
                paramsParcela.put("vctPar", dateFormat.format(new Date()));
                parcelas.add(paramsParcela);
            }
        }
        return parcelas;
    }

    private void fecharOrcamentoComObs(PayloadPedido pedido, String token, String clientIP) throws ParserConfigurationException, IOException, SAXException, SOAPClientException, TransformerException {
        HashMap<String, Object> paramsFecharPedido = prepareParamsForFecharPedidoComObs(pedido, clientIP);
        String xml = makeRequest(token, paramsFecharPedido);
        XmlUtils.validateXmlResponse(xml);
        RetornoPedido retornoFecharPedido = getRetornoPedidoFromXml(xml);
        validateRetornoPedido(retornoFecharPedido);
    }

    private RetornoPedido altSituacaoPedido(String token, String numPed, String sitPedDest, String clientIP) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        HashMap<String, Object> paramsFecharPedido = prepareParamsForAltSituacaoPedido(token, numPed, sitPedDest, clientIP);
        String xml = makeRequest(token, paramsFecharPedido);
        XmlUtils.validateXmlResponse(xml);
        RetornoPedido retornoFecharPedido = getRetornoPedidoFromXml(xml);
        validateRetornoPedido(retornoFecharPedido);
        return retornoFecharPedido;
    }

    private HashMap<String, Object> prepareParamsForFecharPedidoComObs(PayloadPedido pedido, String clientIP) {
        HashMap<String, Object> paramsPedido = new HashMap<>();

        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedido.getCodEmp());
        params.put("codFil", pedido.getCodFil());
        params.put("numPed", pedido.getNumPed());
        params.put("opeExe", "A");
        params.put("fecPed", "S");
        params.put("obsPed", "Pedido de Orçamento Cancelado");
        params.put("usuario", getCampoUsuario("USU_CodIp", clientIP));

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    private HashMap<String, Object> prepareParamsForAltSituacaoPedido(String token, String numPed, String sitPedDest, String clientIP) {
        HashMap<String, Object> paramsPedido = new HashMap<>();

        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", TokensManager.getInstance().getCodEmpFromToken(token));
        params.put("codFil", TokensManager.getInstance().getCodFilFromToken(token));
        params.put("numPed", numPed);
        params.put("opeExe", "A");
        params.put("sitPed", sitPedDest);
        params.put("usuario", getCampoUsuario("USU_CodIp", clientIP));

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    public String calcularDesconto(double vlrPro, double vlrDsc) {
        double valor = vlrPro * vlrDsc;
        BigDecimal bdPrc = BigDecimal.valueOf(valor).setScale(4, RoundingMode.HALF_UP);
        return String.format("%.4f", bdPrc).replace(",", ".");
    }

    public String calcularItemComDesconto(double vlrPro, double vlrDsc) {
        double valor = vlrPro * vlrDsc;
        BigDecimal bdPrc = BigDecimal.valueOf(valor).setScale(4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newValue = BigDecimal.valueOf(vlrPro).subtract(bdPrc).setScale(2, RoundingMode.HALF_UP);
        return String.format("%.2f", newValue).replace(",", ".");
    }
}
