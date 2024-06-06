package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.*;
import com.br.datasig.datasigpdvapi.exceptions.OrderException;
import com.br.datasig.datasigpdvapi.exceptions.WebServiceRuntimeException;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class PedidoService extends WebServiceRequestsService {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    private RepresentantesService representantesService;
    @Autowired
    private ClientesService clientesService;

    public RetornoPedido createPedido(String token, PayloadPedido pedido) throws ParserConfigurationException, IOException, SAXException, SOAPClientException {
        setEmpFilToPedido(pedido, token);
        if(pedido.getNumPed().equals("0")) {
            RetornoPedido retornoPedido = sendPedidoRequest(pedido, token);
            if (pedido.isFechar()) {
                pedido.setNumPed(retornoPedido.getNumPed());
                fecharPedido(pedido, token);
            }
            return retornoPedido;
        } else {
            return handlePedidoExistente(pedido, token);
        }
    }

    private RetornoPedido sendPedidoRequest(PayloadPedido pedido, String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        HashMap<String, Object> params = prepareParamsForPedido(pedido, token);
        String xml = makeRequest(token, params);
        XmlUtils.validateXmlResponse(xml);
        RetornoPedido retornoPedido = getRetornoPedidoFromXml(xml);
        validateRetornoPedido(retornoPedido);
        return retornoPedido;
    }

    private String makeRequest(String token, HashMap<String, Object> params) throws SOAPClientException {
        return soapClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_pedidos", "GravarPedidos_13", token, "0", params, false);
    }

    private RetornoPedido handlePedidoExistente(PayloadPedido pedido, String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        if (pedido.isFechar()) {
            return fecharPedido(pedido, token);
        } else {
            return sendPedidoRequest(pedido, token);
        }
    }

    private void setEmpFilToPedido(PayloadPedido pedido, String token) {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        pedido.setCodEmp(codEmp);
        pedido.setCodFil(codFil);
    }

    private HashMap<String, Object> prepareParamsForPedido(PayloadPedido pedido, String token) {
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
        params.put("codCli", definirCodCli(pedido.getCodCli(), token));
        params.put("codCpg", pedido.getCodCpg());
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
        params.put("vlrDar", pedido.getVlrDar() > 0 ? getDesconto(pedido) : "0");

        if(!pedido.getItens().isEmpty()) {
            List<HashMap<String, Object>> itens = definirParamsItens(pedido, tnsPed, token);
            params.put("produto", itens);
        }

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    private String definirTnsPro(PayloadPedido pedido, String token) {
        return pedido.isFechar() ? TokensManager.getInstance().getParamsPDVFromToken(token).getTnsPed() :
                TokensManager.getInstance().getParamsPDVFromToken(token).getTnsOrc();
    }

    private String definirCodCli(String codCli, String token) {
        if (codCli == null || codCli.isEmpty())
            codCli = TokensManager.getInstance().getParamsPDVFromToken(token).getCodCli();

        return codCli;
    }

    List<HashMap<String, Object>> definirParamsItens(PayloadPedido pedido, String tnsPed, String token) {
        List<HashMap<String, Object>> listaItens = new ArrayList<>();
        pedido.getItens().forEach(itemPedido -> {
            HashMap<String, Object> paramsItem = new HashMap<>();
            if (itemPedido.isExcluir()) {
                paramsItem.put("seqIpd", itemPedido.getSeqIpd());
                paramsItem.put("opeExe", "E");
            } else {
                paramsItem.put("codPro", itemPedido.getCodPro());
                paramsItem.put("codDer", itemPedido.getCodDer());
                paramsItem.put("qtdPed", itemPedido.getQtdPed());
                paramsItem.put("codTpr", itemPedido.getCodTpr());
                paramsItem.put("obsIpd", itemPedido.getObsIpd());
                paramsItem.put("vlrDsc", getDsc(itemPedido.getVlrDsc()));
                paramsItem.put("perDsc", getDsc(itemPedido.getPerDsc()));
                paramsItem.put("codDep", definirCodDep(token));
                paramsItem.put("tnsPro", tnsPed);
                paramsItem.put("resEst", "S");
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

    private static String getDsc(String vlr) {
        if (vlr == null) return "0,00";
        return vlr.trim().isEmpty() ? "0,0" : vlr;
    }

    private Object definirCodDep(String token) {
        return TokensManager.getInstance().getParamsPDVFromToken(token).getCodDep();
    }

    private List<HashMap<String, Object>> definirParamsParcelas(PayloadPedido pedido) {
        List<HashMap<String, Object>> parcelas = new ArrayList<>();
        int seqPar = 0;
        int seqParCpg;
        for(PagamentoPedido pagto : pedido.getPagamentos()) {
            seqParCpg = 0;
            Date dataParcela = new Date();
            ParcelaParametro parcelaParametro = definirValorParcela(pedido, pagto);
            String cgcCre = !pagto.getBanOpe().isEmpty() ? definirCgcCre(pagto.getForma().getCodOpe()) : "";
            pagto.getCondicao().getParcelas().sort(Comparator.comparing(Parcela::getSeqIcp));
            for (Parcela parcela : pagto.getCondicao().getParcelas()) {
                for (int i = 0; i < parcela.getQtdPar(); i++) {
                    seqPar++;
                    seqParCpg++;
                    dataParcela = definirDataParcela(dataParcela, parcela.getDiaPar());
                    HashMap<String, Object> paramsParcela = new HashMap<>();
                    paramsParcela.put("opeExe", "I");
                    paramsParcela.put("seqPar", String.valueOf(seqPar));
                    paramsParcela.put("codFpg", pagto.getForma().getCodFpg());
                    paramsParcela.put("vctPar", dateFormat.format(dataParcela));
                    paramsParcela.put("perPar", getPerPar(parcelaParametro, pagto.getCondicao(), seqParCpg));
                    paramsParcela.put("tipInt", pagto.getForma().getTipInt());
                    paramsParcela.put("banOpe", pagto.getBanOpe());
                    paramsParcela.put("catTef", pagto.getCatTef());
                    paramsParcela.put("nsuTef", pagto.getNsuTef());
                    paramsParcela.put("cgcCre", cgcCre);
                    parcelas.add(paramsParcela);
                }
            }
        }
        return parcelas;
    }

    private static String getPerPar(ParcelaParametro parcelaParametro, CondicaoPagamento condicao, int seqPar) {
        if (condicao.getTipPar().equals("1")) {
            if (seqPar == 1) return parcelaParametro.perMaior;
            else return parcelaParametro.perPar;
        } else if (condicao.getTipPar().equals("2")) {
            if (seqPar == condicao.getQtdParCpg()) return parcelaParametro.perMaior;
            else return parcelaParametro.perPar;
        }
        return condicao.getParcelas().stream().filter(parcela -> parcela.getSeqIcp() == seqPar).findFirst().orElseThrow().getPerPar();
    }

    private static String getDesconto(PayloadPedido pedido) {
        return "-" + String.format("%.2f", pedido.getVlrDar()).replace(".", ",");
    }

    private String definirCgcCre(String codOpe) { //TODO: implementar
        return ""; // WS possível: sapiens_Synccom_senior_g5_co_int_varejo_operadorascartao (Exportar, Exportar_2, Exportar_3)
//        https://documentacao.senior.com.br/gestaoempresarialerp/5.10.3/index.htm#webservices/com_senior_g5_co_int_varejo_operadorascartao.htm?Highlight=operadoras%20financeiras
    }

    private ParcelaParametro definirValorParcela(PayloadPedido pedido, PagamentoPedido pagto) {
        double valorParcela = pagto.getValorTotalPago() / pagto.getCondicao().getQtdParCpg();

        double percentualParcela = valorParcela / pedido.getVlrTot() * 100;
        BigDecimal bdPrc = toRoundedBigDecimal(percentualParcela);

        double perMaior = calrPercentualMaior(pagto, bdPrc, percentualParcela, pedido.getVlrTot());
        BigDecimal bdPrcMaior = toRoundedBigDecimal(perMaior);

        String perParStr = toFormattedString(bdPrc);
        String perMaiorStr = toFormattedString(bdPrcMaior);
        return new ParcelaParametro(perParStr, perMaiorStr);
    }

    private static double calrPercentualMaior(PagamentoPedido pagto, BigDecimal bdPrc, double percentualParcela, double vlrTotalPedido) {
        double percentualTotal = pagto.getValorTotalPago() / vlrTotalPedido * 100;
        double perRestante = pagto.getCondicao().getQtdParCpg() == 1 ? 0 : percentualTotal - (bdPrc.doubleValue() * pagto.getCondicao().getQtdParCpg());
        return percentualParcela + Math.abs(perRestante);
    }

    private static String toFormattedString(BigDecimal bdPrc) {
        return String.format("%.4f", bdPrc).replace(".", ",");
    }

    private static BigDecimal toRoundedBigDecimal(double percentualParcela) {
        BigDecimal bdPrc = BigDecimal.valueOf(percentualParcela);
        bdPrc = bdPrc.setScale(4, RoundingMode.HALF_UP);
        return bdPrc;
    }

    private Date definirDataParcela(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    private RetornoPedido getRetornoPedidoFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "respostaPedido");

        if (nList.getLength() == 1) {
            return RetornoPedido.fromXml(nList.item(0));
        } else {
            throw new WebServiceRuntimeException("Erro ao extrair retorno de pedido da resposta do servidor.");
        }
    }

    private void validateRetornoPedido(RetornoPedido retornoPedido) {
        StringBuilder message = new StringBuilder();
        boolean hasErrors = false;
        if (retornoPedido.getMsgRet().startsWith("ERRO") || retornoPedido.getNumPed().equals("0")) {
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

    private RetornoPedido fecharPedido(PayloadPedido pedido, String token) throws ParserConfigurationException, IOException, SAXException, SOAPClientException {
        HashMap<String, Object> paramsAlterarTransacao = prepareParamsForAlterarTransacao(pedido, token);
        HashMap<String, Object> paramsFecharPedido = prepareParamsForFecharPedido(pedido);

        String xml = makeRequest(token, paramsAlterarTransacao);
        XmlUtils.validateXmlResponse(xml);
        RetornoPedido retornoFecharPedido = getRetornoPedidoFromXml(xml);
        validateRetornoPedido(retornoFecharPedido);

        xml = makeRequest(token, paramsFecharPedido);
        XmlUtils.validateXmlResponse(xml);
        retornoFecharPedido = getRetornoPedidoFromXml(xml);
        validateRetornoPedido(retornoFecharPedido);
        return retornoFecharPedido;
    }

    private HashMap<String, Object> prepareParamsForFecharPedido(PayloadPedido pedido) {
        HashMap<String, Object> paramsPedido = new HashMap<>();

        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedido.getCodEmp());
        params.put("codFil", pedido.getCodFil());
        params.put("numPed", pedido.getNumPed());
        params.put("opeExe", "A");
        params.put("temPar", "S");
        params.put("fecPed", "S");

        List<HashMap<String, Object>> parcelas = definirParamsParcelas(pedido);
        params.put("parcelas", parcelas);

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    private HashMap<String, Object> prepareParamsForAlterarTransacao(PayloadPedido pedido, String token) {
        HashMap<String, Object> paramsPedido = new HashMap<>();

        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedido.getCodEmp());
        params.put("codFil", pedido.getCodFil());
        params.put("opeExe", "A");
        params.put("numPed", pedido.getNumPed());
        params.put("tnsPro", definirTnsPro(pedido, token));

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    public List<ConsultaPedido> getPedidos(String token, TipoBuscaPedidos tipoBusca, String order, String numPed, String datIni, String datFim) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        HashMap<String, Object> paramsPedido = prepareParamsForConsultaPedido(token, tipoBusca, numPed, datIni, datFim);
        String xml = soapClient.requestFromSeniorWS("ConsultaPedido", "Consultar", token, "0", paramsPedido, false);
        XmlUtils.validateXmlResponse(xml);

        String tnsOrc = TokensManager.getInstance().getParamsPDVFromToken(token).getTnsOrc();
        List<ConsultaPedido> pedidos = getConsultaPedidosFromXml(xml, tnsOrc);

        if(order.equals("DESC")) pedidos.sort(Comparator.comparing(ConsultaPedido::getNumPedInt).reversed());
        return pedidos;
    }

    private HashMap<String, Object> prepareParamsForConsultaPedido(String token, TipoBuscaPedidos tipoBusca, String numPed, String datIni, String datFim) {
        String tnsPed = switch (tipoBusca) {
            case ABERTOS -> TokensManager.getInstance().getParamsPDVFromToken(token).getTnsOrc();
            case FECHADOS -> TokensManager.getInstance().getParamsPDVFromToken(token).getTnsPed();
            case TODOS -> "";
        };

        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", TokensManager.getInstance().getCodEmpFromToken(token));
        params.put("codFil", TokensManager.getInstance().getCodFilFromToken(token));
        params.put("codTns", tnsPed);
        params.put("numPed", numPed == null ? "" : numPed);
        params.put("datIni", datIni == null ? "" : "'" + datIni + "'");
        params.put("datFim", datFim == null ? "" : "'" + datFim + "'");
        return params;
    }

    private List<ConsultaPedido> getConsultaPedidosFromXml(String xml, String tnsOrc) throws ParserConfigurationException, IOException, SAXException {
        List<ConsultaPedido> pedidos = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "dadosGerais");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                pedidos.add(ConsultaPedido.fromXml(nNode, tnsOrc));
            }
        }
        return pedidos;
    }

    public ConsultaDetalhesPedido getPedido(String token, String numPed) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        HashMap<String, Object> paramsPedido = prepareParamsForConsultaPedido(token, TipoBuscaPedidos.TODOS, numPed, null, null);
        String xml = soapClient.requestFromSeniorWS("ConsultaPedido", "Consultar", token, "0", paramsPedido, false);
        XmlUtils.validateXmlResponse(xml);

        String tnsOrc = TokensManager.getInstance().getParamsPDVFromToken(token).getTnsOrc();
        List<ConsultaPedido> pedidos = getConsultaPedidosFromXml(xml, tnsOrc);
        if(!pedidos.isEmpty()) {
            ConsultaDetalhesPedido detalhesPedido = new ConsultaDetalhesPedido();
            ConsultaPedido consultaPedido = pedidos.get(0);
            detalhesPedido.setNumPed(consultaPedido.getNumPed());
            detalhesPedido.setDatEmi(consultaPedido.getDatEmi());
            detalhesPedido.setDesRep(defineDesRep(token, consultaPedido.getCodRep()));
            detalhesPedido.setDesCli(defineDesCli(token, consultaPedido.getCodCli()));
        }
        return null;
    }

    private String defineDesRep(String token, String codRep) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        Representante representante = representantesService.getRepresentante(token, codRep);
        if(representante != null) return representante.getNomRep();
        return "";
    }

    private String defineDesCli(String token, String codCli) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        Cliente cliente = clientesService.getCliente(token, codCli);
        if(cliente != null) return cliente.getNomCli();
        return "";
    }

    @AllArgsConstructor
    private static class ParcelaParametro {
        String perPar;
        String perMaior;
    }
}
