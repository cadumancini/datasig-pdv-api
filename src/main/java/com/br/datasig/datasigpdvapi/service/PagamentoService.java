package com.br.datasig.datasigpdvapi.service;

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
    public List<FormaPagamento> getFormasPagamento(String token) throws IOException, ParserConfigurationException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, Object> params = prepareBaseParams(codEmp, codFil);
        String xml = soapClient.requestFromSeniorWS("ConsultaPagamento", "Consultar", token, "0", params, false);

        XmlUtils.validateXmlResponse(xml);
        return getFormasPagamentoFromXml(xml);
    }

    private List<FormaPagamento> getFormasPagamentoFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<FormaPagamento> formas = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "formaPagamento");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                formas.add(FormaPagamento.fromXml(nNode));
            }
        }
        return formas;
    }
}
