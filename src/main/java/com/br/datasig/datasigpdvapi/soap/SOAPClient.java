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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class SOAPClient {
    private static final Logger logger = LoggerFactory.getLogger(SOAPClient.class);
    private final String wsUrl;
    private static final String WS_URL_SUFFIX = "?wsdl";
    private final String identificadorSistema;
    private static final String REQUEST_LOG_MESSAGE = "Requisição para URL {}\nParâmetros: {}";

    public SOAPClient(Environment env) {
        identificadorSistema = env.getProperty("identificadorSistema");
        wsUrl = String.format("%sg5-senior-services/sapiens_Sync", env.getProperty("webservicesUrl"));
    }

    public String requestFromSeniorWS(String wsPath, String service, String user, String pswd, String encryption, Map<String, Object> params, boolean includeIdentificador) throws SOAPClientException {
        String xmlBody = prepareXmlBody(service, user, pswd, encryption, params, getIdentificadorSistema(includeIdentificador));
        String url = wsUrl + wsPath + WS_URL_SUFFIX;
        logger.info(REQUEST_LOG_MESSAGE, url, params);
        return makeRequest(url, xmlBody);
    }
    public String requestFromSeniorWS(String wsPath, String service, String token, String encryption, Map<String, Object> params, boolean includeIdentificador) throws SOAPClientException {
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String xmlBody = prepareXmlBody(service, user, pswd, encryption, params, getIdentificadorSistema(includeIdentificador));
        String url = wsUrl + wsPath + WS_URL_SUFFIX;
        logger.info(REQUEST_LOG_MESSAGE, url, params);
        return makeRequest(url, xmlBody);
    }

    private String getIdentificadorSistema(boolean includeIdentificador) {
        return includeIdentificador ? "<identificadorSistema>" + identificadorSistema + "</identificadorSistema>" : "";
    }

    public String requestFromSeniorWS(String wsPath, String service, String token, String encryption, String params) throws SOAPClientException {
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String xmlBody = prepareXmlBody(service, user, pswd, encryption, params);
        String url = wsUrl + wsPath + WS_URL_SUFFIX;
        logger.info(REQUEST_LOG_MESSAGE, url, params);
        return makeRequest(url, xmlBody);
    }

    private String makeRequest(String url, String xmlBody) throws SOAPClientException {
        try {
            String header = xmlBody.contains("GravarPedido") ? "text/xml;charset=ISO-8859-1" : "text/xml";
            return postRequest(url, xmlBody, header);
        } catch (Exception e) {
            String msg = String.format("Erro na requisição: %s".formatted(e.getMessage()));
            logger.error(msg);
            throw new SOAPClientException(msg);
        }
    }

    private String prepareXmlBody(String service, String usr, String pswd, String encryption, Map<String, Object> params, String identificador) {
        StringBuilder xmlBuilder = new StringBuilder("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.senior.com.br\">");
        xmlBuilder.append("<soapenv:Body>");
        xmlBuilder.append("<ser:").append(service).append(">");
        xmlBuilder.append("<user>").append(usr).append("</user>");
        xmlBuilder.append("<password>").append(pswd).append("</password>");
        xmlBuilder.append("<encryption>").append(encryption).append("</encryption>");

        xmlBuilder.append("<parameters>");
        buildXmlParameters(xmlBuilder, params);
        xmlBuilder.append(identificador);
        xmlBuilder.append("</parameters>");

        xmlBuilder.append("</ser:").append(service).append(">");
        xmlBuilder.append("</soapenv:Body>");
        xmlBuilder.append("</soapenv:Envelope>");

        return xmlBuilder.toString();
    }

    @SuppressWarnings("unchecked")
    private void buildXmlParameters(StringBuilder xmlBuilder, Map<String, Object> params) {
        params.forEach((key, value) -> {
            xmlBuilder.append("<").append(key).append(">");
            if (value instanceof HashMap) {
                buildXmlParameters(xmlBuilder, (HashMap<String, Object>) value);
            } else if (value instanceof ArrayList) {
                ((ArrayList<Object>) value).forEach(item -> buildXmlParameters(xmlBuilder, (HashMap<String, Object>) item));
            } else {
                xmlBuilder.append(value);
            }
            xmlBuilder.append("</").append(key).append(">");
        });
    }

    private static String prepareXmlBody(String service, String usr, String pswd, String encryption, String params) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.senior.com.br\">" + "<soapenv:Body>" +
                "<ser:" + service + ">" +
                "<user>" + usr + "</user>" +
                "<password>" + pswd + "</password>" +
                "<encryption>" + encryption + "</encryption>" +
                "<parameters>" +
                params +
                "</parameters>" +
                "</ser:" + service + ">" +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";
    }

    private static String postRequest(String url, String xmlBody, String header) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpRequest = new HttpPost(url);
        httpRequest.setHeader("Content-Type", header);
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
