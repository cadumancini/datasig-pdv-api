package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.Cliente;
import com.br.datasig.datasigpdvapi.entity.ProdutoDerivacao;
import com.br.datasig.datasigpdvapi.entity.Representante;
import com.br.datasig.datasigpdvapi.soap.SOAPClient;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
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
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_representante", "ConsultarCadastro", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getRepresentantesFromXml(xml);
    }

    private List<Representante> getRepresentantesFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<Representante> representantes = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "representante");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                representantes.add(Representante.fromXml(nNode));
            }
        }
        return representantes;
    }

    public List<Cliente> getClientes(String codEmp, String codFil, String token) throws Exception {
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_clientes", "ConsultarGeral_2", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getClientesFromXml(xml);
    }

    private List<Cliente> getClientesFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<Cliente> clientes = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "cliente");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                clientes.add(Cliente.fromXml(nNode));
            }
        }
        return clientes;
    }

    public List<ProdutoDerivacao> getProdutos(String codEmp, String codFil, String token) throws IOException, ParserConfigurationException, SAXException {
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        addParamsForProdutos(params);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_produtos", "ConsultarGeral_4", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getProdutosFromXml(xml);
    }

    private List<ProdutoDerivacao> getProdutosFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<ProdutoDerivacao> produtos = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "produto");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                produtos.addAll(ProdutoDerivacao.fromXml(nNode));
            }
        }
        return produtos;
    }

    private HashMap<String, String> prepareBaseParams(String codEmp, String codFil) {
        HashMap<String, String> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        return params;
    }

    private void addParamsForProdutos(HashMap<String, String> params) {
        params.put("sitPro", "A");
        params.put("sitDer", "A");
    }
}

