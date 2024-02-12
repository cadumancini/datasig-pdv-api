package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.CondicaoPagamento;
import com.br.datasig.datasigpdvapi.entity.FormaPagamento;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class PagamentoService extends WebServiceRequestsService {
    public List<CondicaoPagamento> getCondicoesPagamento(String token) throws IOException, ParserConfigurationException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        addParamsForCondicao(params);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_condicaopagamento", "ConsultarGeral", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getCondicoesPagamentoFromXml(xml);
    }

    private List<CondicaoPagamento> getCondicoesPagamentoFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<CondicaoPagamento> condicoes = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "condicaoDePagamento");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                condicoes.add(CondicaoPagamento.fromXml(nNode));
            }
        }
        return condicoes;
    }

    public List<FormaPagamento> getFormasPagamento(String token) throws IOException, ParserConfigurationException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        addParamsForFormas(params);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_formapagamento", "ConsultarGeral", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getFormasPagamentoFromXml(xml);
    }

    private List<FormaPagamento> getFormasPagamentoFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<FormaPagamento> formas = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "formaDePagamento");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                formas.add(FormaPagamento.fromXml(nNode));
            }
        }
        return formas;
    }

    private void addParamsForCondicao(HashMap<String, String> params) {
        params.put("sitCpg", "A");
    }

    private void addParamsForFormas(HashMap<String, String> params) {
        params.put("sitCpg", "A");
    }
}
