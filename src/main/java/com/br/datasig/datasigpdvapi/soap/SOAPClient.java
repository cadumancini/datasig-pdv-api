package com.br.datasig.datasigpdvapi.soap;

import com.br.datasig.datasigpdvapi.exceptions.WebServiceNotFoundException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class SOAPClient {
    private static final Logger logger = LoggerFactory.getLogger(SOAPClient.class);
    private final String wsUrl;
    private static final String WS_URL_SUFFIX = "?wsdl";
    private static final String REQUEST_LOG_MESSAGE = "Requisição para URL {}\nParâmetros: {}";

    public SOAPClient(Environment env) {
        wsUrl = String.format("%sg5-senior-services/sapiens_Sync", env.getProperty("webservicesUrl"));
    }

    public String requestFromSeniorWS(String wsPath, String service, String user, String pswd, String encryption, Map<String, Object> params) throws SOAPClientException, ParserConfigurationException, TransformerException {
        String xmlBody = prepareXmlBody(service, user, pswd, encryption, params, "");
        String url = wsUrl + wsPath + WS_URL_SUFFIX;
        logger.info(REQUEST_LOG_MESSAGE, url, params);
        return makeRequest(url, xmlBody);
    }

    public String requestFromSeniorWS(String wsPath, String service, String token, String encryption, Map<String, Object> params, boolean includeIdentificador) throws SOAPClientException, ParserConfigurationException, TransformerException {
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String xmlBody = prepareXmlBody(service, user, pswd, encryption, params, getIdentificadorSistema(includeIdentificador, token));
        String url = wsUrl + wsPath + WS_URL_SUFFIX;
        logger.info(REQUEST_LOG_MESSAGE, url, params);
        return makeRequest(url, xmlBody);
    }

    public String requestFromSdeWS(String wsUrl, String service, Map<String, Object> params) throws SOAPClientException, ParserConfigurationException, TransformerException {
        String xmlBody = prepareXmlBodyNFE(service, params);
        logger.info(REQUEST_LOG_MESSAGE, wsUrl, params);
        // return makeRequest(wsUrl, xmlBody, "http://www.senior.com.br/nfe/IDownloadServico/BaixarPdf"); TODO: voltar
        return makeRequest(wsUrl, xmlBody, "http://www.senior.com.br/nfe/IImpressaoRemotaServico/Imprimir");
    }

    private String getIdentificadorSistema(boolean includeIdentificador, String token) {
        return includeIdentificador ? getIdentificadorSistema(token) : "";
    }

    private String getIdentificadorSistema(String token) {
        return TokensManager.getInstance().getParamsPDVFromToken(token).getSigInt();
    }

    public String requestFromSeniorWSSID(String wsPath, String service, String token, String encryption, Map<String, Object> params) throws SOAPClientException, ParserConfigurationException, TransformerException {
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String xmlBody = prepareXmlBodySID(service, user, pswd, encryption, params);
        String url = wsUrl + wsPath + WS_URL_SUFFIX;
        logger.info(REQUEST_LOG_MESSAGE, url, params);
        return makeRequest(url, xmlBody);
    }

    private String makeRequest(String url, String xmlBody) throws SOAPClientException {
        try {
            String header = "text/xml;charset=UTF-8";
            return postRequest(url, xmlBody, header);
        } catch (Exception e) {
            String msg = String.format("Erro na requisição: %s".formatted(e.getMessage()));
            logger.error(msg);
            throw new SOAPClientException(msg);
        }
    }

    private String makeRequest(String url, String xmlBody, String soapAction) throws SOAPClientException {
        try {
            String contentTypeHeader = "text/xml";
            return postRequestSDE(url, xmlBody, contentTypeHeader, soapAction);
        } catch (Exception e) {
            String msg = String.format("Erro na requisição: %s".formatted(e.getMessage()));
            logger.error(msg);
            throw new SOAPClientException(msg);
        }
    }

    String prepareXmlBody(String service, String usr, String pswd, String encryption, Map<String, Object> params, String identificador) throws ParserConfigurationException, TransformerException {
        return prepareXmlBodyCommon(service, usr, pswd, encryption, params, identificador, false);
    }

    String prepareXmlBodyNFE(String service, Map<String, Object> params) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element envelope = doc.createElement("soapenv:Envelope");
        envelope.setAttribute("xmlns:soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        envelope.setAttribute("xmlns:nfe", "http://www.senior.com.br/nfe");
        doc.appendChild(envelope);

        Element header = doc.createElement("soapenv:Header");
        envelope.appendChild(header);

        Element body = doc.createElement("soapenv:Body");
        envelope.appendChild(body);

        Element serviceElement = doc.createElement("nfe:" + service);
        body.appendChild(serviceElement);

        appendElementWithText(doc, serviceElement, "nfe:usuario", params.get("nfe:usuario").toString());
        appendElementWithText(doc, serviceElement, "nfe:senha", params.get("nfe:senha").toString());
        appendElementWithText(doc, serviceElement, "nfe:tipoDocumento", params.get("nfe:tipoDocumento").toString());
        // appendElementWithText(doc, serviceElement, "nfe:chave", params.get("nfe:chave").toString()); TODO: voltar
        appendElementWithText(doc, serviceElement, "nfe:chaveDocumento", params.get("nfe:chaveDocumento").toString());
        return transformDocumentToString(doc);
    }

    String prepareXmlBodySID(String service, String usr, String pswd, String encryption, Map<String, Object> params) throws ParserConfigurationException, TransformerException {
        return prepareXmlBodyCommon(service, usr, pswd, encryption, params, null, true);
    }

    private String prepareXmlBodyCommon(String service, String usr, String pswd, String encryption, Map<String, Object> params, String identificador, boolean isSID) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element envelope = doc.createElement("soapenv:Envelope");
        envelope.setAttribute("xmlns:soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        envelope.setAttribute("xmlns:ser", "http://services.senior.com.br");
        doc.appendChild(envelope);

        Element body = doc.createElement("soapenv:Body");
        envelope.appendChild(body);

        Element serviceElement = doc.createElement("ser:" + service);
        body.appendChild(serviceElement);

        // add user, password, and encryption
        appendElementWithText(doc, serviceElement, "user", usr);
        appendElementWithText(doc, serviceElement, "password", pswd);
        appendElementWithText(doc, serviceElement, "encryption", encryption);

        Element parameters = doc.createElement("parameters");
        serviceElement.appendChild(parameters);

        if (isSID) {
            params.forEach((key, value) -> {
                Element sidElement = doc.createElement("SID");
                appendElementWithText(doc, sidElement, "param", key + "=" + value);
                parameters.appendChild(sidElement);
            });
        } else {
            buildXmlParameters(doc, parameters, params);
            if (!identificador.isEmpty()) {
                appendElementWithText(doc, parameters, "identificadorSistema", identificador);
            }
        }

        // transform to String
        return transformDocumentToString(doc);
    }

    private void buildXmlParameters(Document doc, Element parent, Map<String, Object> params) {
        params.forEach((key, value) -> {
            if (value instanceof HashMap) {
                Element child = doc.createElement(key);
                buildXmlParameters(doc, child, (HashMap<String, Object>) value);
                parent.appendChild(child);
            } else if (value instanceof ArrayList) {
                ((ArrayList<?>) value).forEach(item -> {
                    Element child = doc.createElement(key);
                    if (item instanceof HashMap) {
                        buildXmlParameters(doc, child, (HashMap<String, Object>) item);
                    } else {
                        child.appendChild(doc.createTextNode(item.toString()));
                    }
                    parent.appendChild(child);
                });
            } else {
                appendElementWithText(doc, parent, key, value.toString());
            }
        });
    }

    private void appendElementWithText(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(textContent));
        parent.appendChild(element);
    }

    private String transformDocumentToString(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // Disable external entity processing to prevent XXE
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        // Optional: Disable other features for further security (depends on implementation)
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter writer = new StringWriter();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(writer);

        transformer.transform(source, result);

        return writer.toString();
    }

    private static String postRequest(String url, String xmlBody, String header) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpRequest = new HttpPost(url);
        httpRequest.setHeader("Content-Type", header);
        StringEntity xmlEntity = new StringEntity(xmlBody, "UTF-8");
        httpRequest.setEntity(xmlEntity);
        HttpResponse httpResponse = client.execute(httpRequest);
        validateStatusCode(httpResponse.getStatusLine().getStatusCode());
        return EntityUtils.toString(httpResponse.getEntity());
    }

    private static String postRequestSDE(String url, String xmlBody, String contentType, String soapAction) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpRequest = new HttpPost(url);
        httpRequest.addHeader("Content-Type", contentType);
        httpRequest.addHeader("SOAPAction", soapAction);
        StringEntity xmlEntity = new StringEntity(xmlBody);
        httpRequest.setEntity(xmlEntity);
        HttpResponse httpResponse = client.execute(httpRequest);
        validateStatusCode(httpResponse.getStatusLine().getStatusCode());
        return EntityUtils.toString(httpResponse.getEntity());
    }

    private static void validateStatusCode(int code) {
        if (code == HttpStatus.SC_NOT_FOUND)
            throw new WebServiceNotFoundException("Serviço não encontrado");
    }
}
