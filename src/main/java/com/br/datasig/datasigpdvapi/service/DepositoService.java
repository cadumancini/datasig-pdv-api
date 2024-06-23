package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.Deposito;
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
public class DepositoService extends WebServiceRequestsService {
    public List<Deposito> getDepositos(String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, Object> params = prepareBaseParams(codEmp, codFil); //TODO: verificar parametros

        String xml = soapClient.requestFromSeniorWS("PDV_DS_ConsultaDepositos", "Deposito", token, "0", params, false); //TODO: ver dados do serviço

        XmlUtils.validateXmlResponse(xml);
        return getDepositosFromXml(xml);
    }

    private List<Deposito> getDepositosFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<Deposito> depositos = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "tabela"); //TODO: ver elementName
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                depositos.add(Deposito.fromXml(nNode));
            }
        }
        return depositos;
    }
}
