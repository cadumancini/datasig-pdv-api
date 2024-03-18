package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.ParamsEmpresa;
import com.br.datasig.datasigpdvapi.entity.ParamsPDV;
import com.br.datasig.datasigpdvapi.entity.TokenResponse;
import com.br.datasig.datasigpdvapi.exceptions.ResourceNotFoundException;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.Token;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@Component
public class UserService extends WebServiceRequestsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final boolean usaTEF;

    public UserService(Environment env) {
        this.usaTEF = env.getProperty("usaTEF").equals("S");
    }

    public String performLogin(String user, String pswd) throws IOException, ParserConfigurationException, SAXException, SOAPClientException {
        HashMap<String, Object> emptyParams = new HashMap<>();
        logger.info("Tentativa de login para usuário {}", user);
        String response = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", user, pswd, "0", emptyParams);

        if(response.contains("Credenciais inválidas")) {
            logger.error("Credenciais inválidas para usuário {}", user);
            return "Credenciais inválidas";
        }
        else {
            logger.info("Login bem sucedido para usuário {}", user);
            Date currentDateTime = Calendar.getInstance().getTime();
            String hash = DigestUtils.sha256Hex(user + pswd + currentDateTime);

            ParamsEmpresa paramsEmpFil = defineCodEmpCodFil(user, pswd);
            ParamsPDV paramsPDV = defineParamsPDV(user, pswd, paramsEmpFil.getCodEmp(), paramsEmpFil.getCodFil());
            TokensManager.getInstance().addToken(hash, user, pswd, paramsEmpFil.getCodEmp(), paramsEmpFil.getCodFil(), paramsEmpFil.isUsaTEF(), paramsPDV);

            return hash;
        }
    }

    private ParamsEmpresa defineCodEmpCodFil(String user, String pswd) throws IOException, ParserConfigurationException, SAXException, SOAPClientException {
        logger.info("Buscando empresa e filial ativas para usuário {}", user);
        HashMap<String, Object> params = prepareParamsForEmpresaAtiva(user);
        String xml = soapClient.requestFromSeniorWS("ConsultaEmpresaAtiva", "Usuario", user, pswd, "0", params);

        XmlUtils.validateXmlResponse(xml);
        return getParamsEmpresaFromXml(xml);
    }

    private ParamsEmpresa getParamsEmpresaFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "result");

        if (nList.getLength() == 1) {
            return ParamsEmpresa.fromXml(nList.item(0), usaTEF);
        } else {
            throw new ResourceNotFoundException("Parâmetros não encontrados para o usuário");
        }
    }

    private HashMap<String, Object> prepareParamsForEmpresaAtiva(String user) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("NOMUSU", user);
        return params;
    }

    private ParamsPDV defineParamsPDV(String user, String pswd, String codEmp, String codFil) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        logger.info("Buscando parâmetros PDV para usuário {}", user);
        HashMap<String, Object> params = prepareParamsForParamsPDV(codEmp, codFil);
        String xml = soapClient.requestFromSeniorWS("ConsultaParametrosIntegracao", "PDV", user, pswd, "0", params);

        XmlUtils.validateXmlResponse(xml);
        return getParamsPDVFromXml(xml);
    }

    private ParamsPDV getParamsPDVFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "result");

        if (nList.getLength() == 1) {
            return ParamsPDV.fromXml(nList.item(0));
        } else {
            throw new ResourceNotFoundException("Parâmetros não encontrados para o usuário");
        }
    }

    private HashMap<String, Object> prepareParamsForParamsPDV(String codEmp, String codFil) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        return params;
    }

    public TokenResponse getParamsFromToken(String tokenValue) {
        Token token = TokensManager.getInstance().getTokenByValue(tokenValue);
        ParamsPDV paramsPDV = TokensManager.getInstance().getParamsPDVFromToken(tokenValue);
        return new TokenResponse(token.getUserName(), token.getCodEmp(), token.getCodFil(), token.isUsaTEF(), paramsPDV);
    }
}
