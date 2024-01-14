package com.br.datasig.datasigpdvapi.soap;

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
    private final String wsUrlEnd = "?wsdl";

    public SOAPClient(Environment env) {
        String webServicesUrl = env.getProperty("webservices.url");
        wsUrl = String.format("%sg5-senior-services/sapiens_Sync", webServicesUrl);
    }

    public String requestFromSeniorWS(String wsPath, String service, String usr, String pswd, String encryption, HashMap params) throws SOAPClientException {
        String xmlBody = prepareXmlBody(service, usr, pswd, encryption, params);
        String url = wsUrl + wsPath + wsUrlEnd;
        logger.info("Requisição para URL %s\nParâmetros: %s".formatted(url, params));
        return makeRequest(wsPath, xmlBody);
    }

    public String requestFromSeniorWS(String wsPath, String service, String usr, String pswd, String encryption, String params) throws SOAPClientException { //TODO: Precisamos?
        String xmlBody = prepareXmlBody(service, usr, pswd, encryption, params);
        String url = wsUrl + wsPath + wsUrlEnd;
        logger.info("Requisição para URL %s\nParâmetros: %s".formatted(url, params));
        return makeRequest(wsPath, xmlBody);
    }

    private String makeRequest(String url, String xmlBody) throws SOAPClientException {
        try {
            return postRequest(url, xmlBody);
        } catch (IOException e) {
            String msg = String.format("Erro na requisição: %s".formatted(e.getMessage()));
            logger.error(msg);
            throw new SOAPClientException(msg);
        }
    }

    private static String prepareXmlBody(String service, String usr, String pswd, String encryption, HashMap params) { //TODO: Refatorar
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
        return EntityUtils.toString(httpResponse.getEntity());
    }
}
