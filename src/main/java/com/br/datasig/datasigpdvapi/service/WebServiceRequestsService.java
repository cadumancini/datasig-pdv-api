package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.Representante;
import com.br.datasig.datasigpdvapi.soap.SOAPClient;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.ResourceNotFoundException;
import com.br.datasig.datasigpdvapi.util.WebServiceRuntimeException;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class WebServiceRequestsService {
    private static final Logger logger = LoggerFactory.getLogger(WebServiceRequestsService.class);

    @Autowired
    private SOAPClient soapClient;

    public String performLogin(String user, String pswd) throws SOAPClientException {
        HashMap<String, String> emptyParams = new HashMap<>();
        logger.info("Tentativa de login para usuário {}", user);
        String response = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", user, pswd, "0", emptyParams, false);

        if(response.contains("Credenciais inválidas")) {
            logger.error("Credenciais inválidas para usuário {}", user);
            return "Credenciais inválidas";
        }
        else {
            logger.info("Login bem sucedido para usuário {}", user);
            Date currentDateTime = Calendar.getInstance().getTime();
            String hash = DigestUtils.sha256Hex(user + pswd + currentDateTime);
            TokensManager.getInstance().addToken(hash, user, pswd);

            return hash;
        }
    }

    public List<Representante> getRepresentantes(String codEmp, String codFil, String token) throws Exception {
        HashMap<String, String> params = prepareParamsForRepresentantesEClientes(codEmp, codFil);
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_representante", "ConsultarCadastro", user, pswd, "0", params, true);

        validateResult(xml);

        return getRepresentantesFromXml(xml);
    }

    // TODO: refatorar
    private List<Representante> getRepresentantesFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<Representante> representantes = new ArrayList<>();
        NodeList nList = getNodeListByElementName(xml, "representante");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) nNode;
                String codRep = element.getElementsByTagName("codRep").item(0).getTextContent();
                String nomRep = element.getElementsByTagName("nomRep").item(0).getTextContent();
                String apeRep = element.getElementsByTagName("apeRep").item(0).getTextContent();
                String tipRep = element.getElementsByTagName("tipRep").item(0).getTextContent();

                representantes.add(new Representante(codRep, nomRep, apeRep, tipRep));
            }
        }
        return representantes;
    }

    private HashMap<String, String> prepareParamsForRepresentantesEClientes(String codEmp, String codFil) {
        HashMap<String, String> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        return params;
    }

    private void validateResult(String xml) throws ParserConfigurationException, IOException, SAXException {
        if (xml.contains("<erroExecucao>")) {
            String executionError = getMessageFromXml(xml, "result", "erroExecucao");
            logger.error(executionError);
            throw new WebServiceRuntimeException(executionError);
        } else if (xml.contains("<mensagemErro>")) {
            String executionError = getMessageFromXml(xml, "erros", "mensagemErro");
            logger.error(executionError);
            if (executionError.contains("não encontrado")) {
                throw new ResourceNotFoundException(executionError);
            } else {
                throw new WebServiceRuntimeException(executionError);
            }
        }
    }

    private String getMessageFromXml(String xml, String parentElement, String desiredElement) throws ParserConfigurationException, IOException, SAXException {
        NodeList nListError = getNodeListByElementName(xml, parentElement);
        Element element = (Element) nListError.item(0);
        return element.getElementsByTagName(desiredElement).item(0).getTextContent();
    }

    private NodeList getNodeListByElementName(String xml, String elementName) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(input);
        doc.getDocumentElement().normalize();
        return doc.getElementsByTagName(elementName);
    }
}

