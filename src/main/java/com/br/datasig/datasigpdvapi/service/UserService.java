package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.*;
import com.br.datasig.datasigpdvapi.exceptions.NotAllowedUserException;
import com.br.datasig.datasigpdvapi.exceptions.ResourceNotFoundException;
import com.br.datasig.datasigpdvapi.exceptions.WebServiceRuntimeException;
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
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@Component
public class UserService extends WebServiceRequestsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final boolean isLive;

    public UserService(Environment env) {
        isLive = env.getProperty("environment").equals("live");
    }

    public String login(String user, String pswd, String clientIp) throws IOException, ParserConfigurationException, SAXException, SOAPClientException, NotAllowedUserException, TransformerException {
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
            if (isLive) compareLoginIPWithParams(clientIp, paramsPDV);
            ParamsImpressao paramsImpressao = defineParamsImpressao(user, pswd, paramsEmpFil.getCodEmp(), paramsEmpFil.getCodFil(), clientIp);
            if (paramsImpressao == null && isLive) {
                logger.error("Parâmetros de impressão não definidos para o IP {}", clientIp);
                throw new WebServiceRuntimeException("Parâmetros de impressão não definidos para o IP" + clientIp);
            }
            TokensManager.getInstance().addToken(hash, user, pswd, paramsEmpFil.getCodEmp(), paramsEmpFil.getCodFil(), clientIp, paramsPDV, paramsImpressao);

            return hash;
        }
    }

    private void compareLoginIPWithParams(String codIp, ParamsPDV paramsPDV) throws NotAllowedUserException {
        if(paramsPDV.getCaixas().stream().noneMatch(caixa -> caixa.getCodIp().equals(codIp))) {
            throw new NotAllowedUserException("O IP usado no login (%s) não está presente na lista de IPs definidos nos parâmetros da filial para utilização nos caixas.".formatted(codIp));
        }
    }

    private ParamsEmpresa defineCodEmpCodFil(String user, String pswd) throws IOException, ParserConfigurationException, SAXException, SOAPClientException, TransformerException {
        logger.info("Buscando empresa e filial ativas para usuário {}", user);
        HashMap<String, Object> params = prepareParamsForEmpresaAtiva(user);
        String xml = soapClient.requestFromSeniorWS("PDV_DS_ConsultaEmpresaAtiva", "Usuario", user, pswd, "0", params);

        XmlUtils.validateXmlResponse(xml);
        return getParamsEmpresaFromXml(xml);
    }

    private ParamsEmpresa getParamsEmpresaFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "result");

        if (nList.getLength() == 1) {
            return ParamsEmpresa.fromXml(nList.item(0));
        } else {
            throw new ResourceNotFoundException("Parâmetros não encontrados para o usuário");
        }
    }

    private HashMap<String, Object> prepareParamsForEmpresaAtiva(String user) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("NOMUSU", user);
        return params;
    }

    private ParamsPDV defineParamsPDV(String user, String pswd, String codEmp, String codFil) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        logger.info("Buscando parâmetros PDV para usuário {}", user);
        HashMap<String, Object> params = prepareParamsForParamsPDV(codEmp, codFil);
        String xml = soapClient.requestFromSeniorWS("PDV_DS_ConsultaParametrosIntegracao", "PDV", user, pswd, "0", params);

        XmlUtils.validateXmlResponse(xml);
        return getParamsPDVFromXml(xml);
    }

    private ParamsImpressao defineParamsImpressao(String user, String pswd, String codEmp, String codFil, String clientIP) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        logger.info("Buscando parâmetros de impressão para o IP {}", clientIP);
        HashMap<String, Object> params = prepareParamsForParamsImpressao(codEmp, codFil, clientIP);
        String xml = soapClient.requestFromSeniorWS("PDV_DS_ConsultaImpressora", "Consulta", user, pswd, "0", params);

        if (xml.contains("<erroExecucao xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>") && (!isLive))
            return null;

        XmlUtils.validateXmlResponse(xml);
        return getParamsImpressaoFromXml(xml);
    }

    private ParamsPDV getParamsPDVFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "result");

        if (nList.getLength() == 1) {
            return ParamsPDV.fromXml(nList.item(0));
        } else {
            throw new ResourceNotFoundException("Parâmetros não encontrados para o usuário");
        }
    }

    private ParamsImpressao getParamsImpressaoFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "result");

        if (nList.getLength() == 1) {
            return ParamsImpressao.fromXml(nList.item(0));
        } else {
            throw new ResourceNotFoundException("Parâmetros de impressão não encontrados para o usuário");
        }
    }

    private HashMap<String, Object> prepareParamsForParamsPDV(String codEmp, String codFil) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        return params;
    }

    private HashMap<String, Object> prepareParamsForParamsImpressao(String codEmp, String codFil, String clientIP) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        params.put("codIp", clientIP);
        return params;
    }

    public TokenResponse getParamsFromToken(String tokenValue) {
        Token token = TokensManager.getInstance().getTokenByValue(tokenValue);
        ParamsPDV paramsPDV = TokensManager.getInstance().getParamsPDVFromToken(tokenValue);
        ParamsImpressao paramsImpressao = TokensManager.getInstance().getParamsImpressaoFromToken(tokenValue);
        ParamsPDVResponse paramsPDVResponse = new ParamsPDVResponse(
                paramsPDV.getCodTpr(),
                paramsPDV.getDscTot(),
                paramsPDV.getDepositos(),
                paramsPDV.getRamos(),
                paramsPDV.getCodDep(),
                token.getCodEmp(),
                token.getCodFil(),
                paramsPDV.getNomEmp(),
                paramsPDV.getNomFil(),
                token.getUserName(),
                token.getCodIp(),
                paramsImpressao == null ? "N" : paramsImpressao.getIndImp(),
                paramsImpressao == null ? "1" : paramsImpressao.getQtdImp());
        return new TokenResponse(token.getUserName(), token.getCodEmp(), token.getCodFil(), paramsPDVResponse);
    }

    public String logout(String token) {
        TokensManager.getInstance().removeToken(token);
        return "OK";
    }
}
