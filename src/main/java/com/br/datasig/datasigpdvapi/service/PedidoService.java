package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.*;
import com.br.datasig.datasigpdvapi.exceptions.OrderException;
import com.br.datasig.datasigpdvapi.exceptions.WebServiceRuntimeException;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
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
    private final String numRegNFC;
    private final boolean usaTEF;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public PedidoService(Environment env) {
        numRegNFC = env.getProperty("numRegNFC");
        usaTEF = env.getProperty("usaTEF").equals("S");
    }

    public RetornoPedido createPedido(String token, Pedido pedido) throws ParserConfigurationException, IOException, SAXException, SOAPClientException {
        setEmpFilToPedido(pedido, token);

        HashMap<String, Object> params = prepareParamsForPedido(pedido, token);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_pedidos", "GravarPedidos_13", token, "0", params, false);
        XmlUtils.validateXmlResponse(xml);
        RetornoPedido retornoPedido = getRetornoPedidoFromXml(xml);
        validateRetornoPedido(retornoPedido);
        fecharPedido(retornoPedido, token);
        return retornoPedido;
    }

    private void setEmpFilToPedido(Pedido pedido, String token) {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        pedido.setCodEmp(codEmp);
        pedido.setCodFil(codFil);
    }

    private HashMap<String, Object> prepareParamsForPedido(Pedido pedido, String token) {
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

        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedido.getCodEmp());
        params.put("codFil", pedido.getCodFil());
        params.put("codCli", definirCodCli(pedido.getCodCli(), token));
        params.put("codCpg", pedido.getCodCpg());
        params.put("codFpg", pedido.getCodFpg());
        params.put("codRep", pedido.getCodRep());
        params.put("cifFob", "X");
        params.put("indPre", "1");
        params.put("opeExe", "I");
        params.put("tnsPro", "90199");
        params.put("temPar", "S");
        params.put("acePar", "N");

        List<HashMap<String, Object>> itens = definirParamsItens(pedido);
        params.put("produto", itens);

        List<HashMap<String, Object>> parcelas = definirParamsParcelas(pedido);
        params.put("parcelas", parcelas);

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    private String definirCodCli(String codCli, String token) {
        if (codCli == null || codCli.isEmpty())
            codCli = TokensManager.getInstance().getParamsPDVFromToken(token).getCodCli();

        return codCli;
    }

    List<HashMap<String, Object>> definirParamsItens(Pedido pedido) {
        List<HashMap<String, Object>> listaItens = new ArrayList<>();
        pedido.getItens().forEach(itemPedido -> {
            HashMap<String, Object> paramsItem = new HashMap<>();
            paramsItem.put("codPro", itemPedido.getCodPro());
            paramsItem.put("codDer", itemPedido.getCodDer());
            paramsItem.put("qtdPed", itemPedido.getQtdPed());
            paramsItem.put("codTpr", itemPedido.getCodTpr());
            paramsItem.put("codDep", "1000");
            paramsItem.put("tnsPro", "90199");
            paramsItem.put("resEst", "S");
            paramsItem.put("pedPrv", "N");
            paramsItem.put("opeExe", "I");
            listaItens.add(paramsItem);
        });

        return listaItens;
    }

    List<HashMap<String, Object>> definirParamsParcelas(Pedido pedido) {
        Date dataParcela = new Date();
        String valorParcela = definirValorParcela(pedido);
        String cgcCre = !pedido.getBanOpe().isEmpty() ? definirCgcCre(pedido.getCodOpe()) : "";
        pedido.getParcelas().sort(Comparator.comparing(o -> o.getSeqIcp()));
        int seqPar = 0;
        List<HashMap<String, Object>> parcelas = new ArrayList<>();
        for (Parcela parcela : pedido.getParcelas()) {
            for (int i = 0; i < parcela.getQtdPar(); i++) {
                seqPar++;
                dataParcela = definirDataParcela(dataParcela, parcela.getDiaPar());
                HashMap<String, Object> paramsParcela = new HashMap<>();
                paramsParcela.put("opeExe", "I");
                paramsParcela.put("seqPar", String.valueOf(seqPar));
                paramsParcela.put("vctPar", dateFormat.format(dataParcela));
                paramsParcela.put("vlrPar", valorParcela);
                paramsParcela.put("tipInt", getTipInt(pedido));
                paramsParcela.put("banOpe", pedido.getBanOpe());
                paramsParcela.put("catTef", pedido.getCatTef());
                paramsParcela.put("nsuTef", pedido.getNsuTef());
                paramsParcela.put("cgcCre", cgcCre);
                parcelas.add(paramsParcela);
            }
        }
        return parcelas;
    }

    private String definirCgcCre(String codOpe) { //TODO: implementar
        return ""; // WS possível: sapiens_Synccom_senior_g5_co_int_varejo_operadorascartao (Exportar, Exportar_2, Exportar_3)
//        https://documentacao.senior.com.br/gestaoempresarialerp/5.10.3/index.htm#webservices/com_senior_g5_co_int_varejo_operadorascartao.htm?Highlight=operadoras%20financeiras
    }

    private String definirValorParcela(Pedido pedido) {
        double valorParcela = pedido.getVlrTot() / pedido.getQtdPar();

        BigDecimal bd = BigDecimal.valueOf(valorParcela);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return String.format("%.2f", bd.doubleValue()).replace(".", ",");
    }

    private Date definirDataParcela(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    private String getTipInt(Pedido pedido) {
        return usaTEF && pedido.getDesFpg().equals("OUTROS") ? "1" : "2"; //TODO: trocar 'OUTROS' para quando definir TEF
//        return usaTEF && List.of("6", "7", "8", "17", "18", "19", "20").contains(pedido.getTipFpg()) ? "1" : "2";
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
        } else if (retornoPedido.getNumPed().equals("0")) {
            throw new OrderException("Ocorreu um problema durante a criação do pedido. Contate o administrador do sistema");
        }
    }

    private void fecharPedido(RetornoPedido pedido, String token) throws ParserConfigurationException, IOException, SAXException, SOAPClientException {
        HashMap<String, Object> paramsFecharPedido = prepareParamsForFecharPedido(pedido);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_pedidos", "GravarPedidos_13", token, "0", paramsFecharPedido, false);
        XmlUtils.validateXmlResponse(xml);
        RetornoPedido retornoFecharPedido = getRetornoPedidoFromXml(xml);
        validateRetornoPedido(retornoFecharPedido);
    }

    private HashMap<String, Object> prepareParamsForFecharPedido(RetornoPedido pedido) {
        HashMap<String, Object> paramsPedido = new HashMap<>();

        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedido.getCodEmp());
        params.put("codFil", pedido.getCodFil());
        params.put("opeExe", "A");
        params.put("numPed", pedido.getNumPed());
        params.put("fecPed", "S");

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    public String createNFCe(String token, String numPed) throws ParserConfigurationException, IOException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        String paramsNFCe = prepareParamsForGeracaoNFCe(codEmp, codFil, numPed);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", token, "0", paramsNFCe);
        XmlUtils.validateXmlResponse(xml);
        return getResponseNFCeFromXml(xml);
    }

    private String prepareParamsForGeracaoNFCe(String codEmp, String codFil, String numPed) {
        StringBuilder paramsBuilder = new StringBuilder();

        appendSIDParam(paramsBuilder, "acao", "sid.srv.regra");
        appendSIDParam(paramsBuilder, "numreg", numRegNFC);
        appendSIDParam(paramsBuilder, "aCodEmpPdv", codEmp);
        appendSIDParam(paramsBuilder, "aCodFilPdv", codFil);
        appendSIDParam(paramsBuilder, "aNumPedPdv", numPed);

        return paramsBuilder.toString();
    }

    private String getResponseNFCeFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "result");
        if (nList.getLength() == 1) {
            Element element = (Element) nList.item(0);
            return element.getElementsByTagName("resultado").item(0).getTextContent();
        } else {
            throw new WebServiceRuntimeException("Erro na geração da NFC-e");
        }
    }
}
