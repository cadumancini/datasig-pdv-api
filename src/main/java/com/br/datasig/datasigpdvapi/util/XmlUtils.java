package com.br.datasig.datasigpdvapi.util;

import com.br.datasig.datasigpdvapi.exceptions.ResourceNotFoundException;
import com.br.datasig.datasigpdvapi.exceptions.WebServiceRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class XmlUtils {
    private static final Logger logger = LoggerFactory.getLogger(XmlUtils.class);

    public static void validateXmlResponse(String xml) throws ParserConfigurationException, IOException, SAXException, ResourceNotFoundException, WebServiceRuntimeException {
        if (xml.contains("<erroExecucao>")) {
            String executionError = getTextFromXmlElement(xml, "result", "erroExecucao");
            logger.error(executionError);
            throw new WebServiceRuntimeException(executionError);
        } else if (xml.contains("<mensagemErro>")) {
            String executionError = getTextFromXmlElement(xml, "erros", "mensagemErro");
            logger.error(executionError);
            if (executionError.contains("não encontrado")) {
                throw new ResourceNotFoundException(executionError);
            } else {
                throw new WebServiceRuntimeException(executionError);
            }
        } else if (xml.contains("<erro>true</erro>")) {
            throw new WebServiceRuntimeException("Ocorreu um erro ao validar o retorno.");
        } else if (xml.contains("ERRO:")) {
            try {
                String executionError = getTextFromXmlElement(xml, "result", "resultado");
                logger.error(executionError);
                throw new WebServiceRuntimeException(executionError);
            } catch (Exception e) {
                // do nothing
            }
        } else if (xml.contains("<ImprimirResult")) { // Retorno de WS de impressão do SDE
            if (xml.contains("<Sucesso>false</Sucesso>")) {
                String codigo = getTextFromXmlElement(xml, "ImprimirResult", "Codigo");
                String mensagem = getTextFromXmlElement(xml, "ImprimirResult", "Mensagem");
                String executionError = "Erro código: " + codigo + " - mensagem: " + mensagem;
                logger.error(executionError);
                throw new WebServiceRuntimeException(executionError);
            }
        }
    }

    public static String getTextFromXmlElement(String xml, String parentElement, String desiredElement) throws ParserConfigurationException, IOException, SAXException {
        NodeList nListError = getNodeListByElementName(xml, parentElement);
        Element element = (Element) nListError.item(0);
        return element.getElementsByTagName(desiredElement).item(0).getTextContent();
    }

    public static NodeList getNodeListByElementName(String xml, String elementName) throws ParserConfigurationException, IOException, SAXException {
        return getNodeListByElementName(xml, elementName, StandardCharsets.UTF_8);
    }

    public static NodeList getNodeListByElementName(String xml, String elementName, Charset charset) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = factory.newDocumentBuilder();

        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(charset));
        Document doc;
        try {
            doc = builder.parse(input);
        } catch(Exception e) {
            input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            doc = builder.parse(input);
        }
        doc.getDocumentElement().normalize();
        return doc.getElementsByTagName(elementName);
    }
}
