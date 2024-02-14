package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.Pedido;
import com.br.datasig.datasigpdvapi.entity.RetornoPedido;
import com.br.datasig.datasigpdvapi.exceptions.WebServiceRuntimeException;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class PedidoService extends WebServiceRequestsService {
    private final String numRegNFC;

    public PedidoService(Environment env) {
        numRegNFC = env.getProperty("numRegNFC");
    }

    public RetornoPedido createPedido(String token, Pedido pedido) throws ParserConfigurationException, IOException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        pedido.setCodEmp(codEmp);
        pedido.setCodFil(codFil);

        HashMap<String, Object> params = prepareParamsForPedido(pedido);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_pedidos", "GravarPedidos_13", token, "0", params, false);
        XmlUtils.validateXmlResponse(xml);
        return getRetornoPedidoFromXml(xml);
    }

    private HashMap<String, Object> prepareParamsForPedido(Pedido pedido) {
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
        params.put("codCli", pedido.getCodCli());
        params.put("codCpg", pedido.getCodCpg());
        params.put("codFpg", pedido.getCodFpg());
        params.put("codRep", pedido.getCodRep());
        params.put("cifFob", "X");
        params.put("indPre", "1");
        params.put("opeExe", "I");
        params.put("tnsPro", "90199");

        List<HashMap<String, Object>> listaItens = new ArrayList<>();
        pedido.getItens().forEach(itemPedido -> {
            HashMap<String, Object> paramsItem = new HashMap<>();
            paramsItem.put("codPro", itemPedido.getCodPro());
            paramsItem.put("codDer", itemPedido.getCodDer());
            paramsItem.put("qtdPed", itemPedido.getQtdPed());
            paramsItem.put("codTpr", itemPedido.getCodTpr());
            paramsItem.put("codDep", "1000");
            paramsItem.put("tnsPro", "90199");
            paramsItem.put("resEst", "N");
            paramsItem.put("pedPrv", "N");
            paramsItem.put("opeExe", "I");
            listaItens.add(paramsItem);
        });
        params.put("produto", listaItens);

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    private RetornoPedido getRetornoPedidoFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "respostaPedido");

        if (nList.getLength() == 1) {
            return RetornoPedido.fromXml(nList.item(0));
        } else {
            throw new WebServiceRuntimeException("Erro ao extrair retorno de pedido da resposta do servidor.");
        }
    }

    public String createNFCe(String token, String numPed) throws ParserConfigurationException, IOException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        String paramsNFCe = prepareParamsForGeracaoNFCe(codEmp, codFil, numPed);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", token, "0", paramsNFCe);
        XmlUtils.validateXmlResponse(xml);
        return "NFC-e criada com sucesso!";
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
}
