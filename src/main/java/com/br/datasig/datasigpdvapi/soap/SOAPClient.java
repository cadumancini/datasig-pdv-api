package com.br.datasig.datasigpdvapi.soap;

import com.br.datasig.datasigpdvapi.exceptions.WebServiceNotFoundException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Component
public class SOAPClient {
    private static final Logger logger = LoggerFactory.getLogger(SOAPClient.class);

    private final String wsUrl;
    private final static String WS_URL_SUFFIX = "?wsdl";
    private final String webservicesUrl;
    private final String identificadorSistema;
    private final String requestLogMessage = "Requisição para URL {}\nParâmetros: {}";

    public SOAPClient(Environment env) {
        webservicesUrl = env.getProperty("webservicesUrl");
        identificadorSistema = env.getProperty("identificadorSistema");
        wsUrl = String.format("%sg5-senior-services/sapiens_Sync", webservicesUrl);
    }

    public String requestFromSeniorWS(String wsPath, String service, String user, String pswd, String encryption, HashMap params, boolean includeIdentificador) throws SOAPClientException {
        String xmlBody = prepareXmlBody(service, user, pswd, encryption, params, includeIdentificador);
        String url = wsUrl + wsPath + WS_URL_SUFFIX;
        logger.info(requestLogMessage, url, params);
        return makeRequest(url, xmlBody);
    }
    public String requestFromSeniorWS(String wsPath, String service, String token, String encryption, HashMap params, boolean includeIdentificador) throws SOAPClientException {
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String xmlBody = prepareXmlBody(service, user, pswd, encryption, params, includeIdentificador);
        String url = wsUrl + wsPath + WS_URL_SUFFIX;
        logger.info(requestLogMessage, url, params);
        return makeRequest(url, xmlBody);
    }

    public String requestFromSeniorWS(String wsPath, String service, String token, String encryption, String params) throws SOAPClientException { //TODO: Precisamos?
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String xmlBody = prepareXmlBody(service, user, pswd, encryption, params);
        String url = wsUrl + wsPath + WS_URL_SUFFIX;
        logger.info(requestLogMessage, url, params);
        return makeRequest(url, xmlBody);
    }

    private String makeRequest(String url, String xmlBody) throws SOAPClientException {
        try {
            return postRequest(url, xmlBody);
        } catch (Exception e) {
            String msg = String.format("Erro na requisição: %s".formatted(e.getMessage()));
            logger.error(msg);
            throw new SOAPClientException(msg);
        }
    }

    private String prepareXmlBody(String service, String usr, String pswd, String encryption, HashMap params, boolean includeIdentificador) { //TODO: Refatorar
        StringBuilder xmlBuilder = new StringBuilder("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.senior.com.br\">");
        xmlBuilder.append("<soapenv:Body>");
        xmlBuilder.append("<ser:" + service + ">");
        xmlBuilder.append("<user>" + usr + "</user>");
        xmlBuilder.append("<password>" + pswd + "</password>");
        xmlBuilder.append("<encryption>" + encryption + "</encryption>");
        if(params.isEmpty()) {
            xmlBuilder.append("<parameters/>");
        } else {
            xmlBuilder.append("<parameters>");
            params.forEach((key, value) -> {
                if(value instanceof HashMap) {
                    xmlBuilder.append("<" + key + ">");
                    ((HashMap<?, ?>) value).forEach((key1, value1) -> {
                        if(value1 instanceof ArrayList) {
                            ((ArrayList) value1).forEach(produto -> {
                                xmlBuilder.append("<" + key1 + ">");
                                ((HashMap<?, ?>) produto).forEach((key2, value2) -> {
                                    if(value2 instanceof  ArrayList) {
                                        ((ArrayList) value2).forEach(campo -> {
                                            xmlBuilder.append("<" + key2 + ">");
                                            ((HashMap<?, ?>) campo).forEach((key3, value3) -> {
                                                xmlBuilder.append("<" + key3 + ">" + value3 + "</" + key3 + ">");
                                            });
                                            xmlBuilder.append("</" + key2 + ">");
                                        });
                                    } else {
                                        xmlBuilder.append("<" + key2 + ">" + value2 + "</" + key2 + ">");
                                    }
                                });
                                xmlBuilder.append("</" + key1 + ">");
                            });
                        } else {
                            xmlBuilder.append("<" + key1 + ">" + value1 + "</" + key1+ ">");
                        }
                    });
                    xmlBuilder.append("</" + key + ">");
                }
                else {
                    xmlBuilder.append("<" + key + ">" + value + "</" + key + ">");
                }
            });
            if (includeIdentificador) {
                xmlBuilder.append("<identificadorSistema>" + identificadorSistema + "</identificadorSistema>");
            }
            xmlBuilder.append("</parameters>");
        }
        xmlBuilder.append("</ser:" + service + ">");
        xmlBuilder.append("</soapenv:Body>");
        xmlBuilder.append("</soapenv:Envelope>");

        return xmlBuilder.toString();
    }

    private static String prepareXmlBody(String service, String usr, String pswd, String encryption, String params) {
        StringBuilder xmlBuilder = new StringBuilder("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.senior.com.br\">");
        xmlBuilder.append("<soapenv:Body>");
        xmlBuilder.append("<ser:" + service + ">");
        xmlBuilder.append("<user>" + usr + "</user>");
        xmlBuilder.append("<password>" + pswd + "</password>");
        xmlBuilder.append("<encryption>" + encryption + "</encryption>");
        xmlBuilder.append("<parameters>");
        xmlBuilder.append(params);
        xmlBuilder.append("</parameters>");
        xmlBuilder.append("</ser:" + service + ">");
        xmlBuilder.append("</soapenv:Body>");
        xmlBuilder.append("</soapenv:Envelope>");

        return xmlBuilder.toString();
    }

    private static String postRequest(String url, String xmlBody) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpRequest = new HttpPost(url);
        String header = xmlBody.contains("GravarPedido") ?  "text/xml;charset=ISO-8859-1" :  "text/xml";
        httpRequest.setHeader("Content-Type", header);
        StringEntity xmlEntity = new StringEntity(xmlBody);
        httpRequest.setEntity(xmlEntity);
        HttpResponse httpResponse = client.execute(httpRequest);
        if (httpResponse.getStatusLine().getStatusCode() == 404) {
            throw new WebServiceNotFoundException("Serviço não encontrado");
        }
        return EntityUtils.toString(httpResponse.getEntity());
    }
}
