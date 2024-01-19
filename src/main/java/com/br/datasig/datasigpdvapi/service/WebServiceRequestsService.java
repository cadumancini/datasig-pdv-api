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
import org.w3c.dom.Element;
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
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_representante", "ConsultarCadastro", user, pswd, "0", params, true);

        XmlUtils.validateXmlResponse(xml);

        return getRepresentantesFromXml(xml);
    }

    // TODO: refatorar
    private List<Representante> getRepresentantesFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<Representante> representantes = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "representante");

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

    public List<Cliente> getClientes(String codEmp, String codFil, String token) throws Exception {
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_clientes", "ConsultarGeral_2", user, pswd, "0", params, true);

        XmlUtils.validateXmlResponse(xml);

        return getClientesFromXml(xml);
    }

    // TODO: refatorar
    private List<Cliente> getClientesFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<Cliente> clientes = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "cliente");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) nNode;
                String codCli = element.getElementsByTagName("codCli").item(0).getTextContent();
                String nomCli = element.getElementsByTagName("nomCli").item(0).getTextContent();
                String apeCli = element.getElementsByTagName("apeCli").item(0).getTextContent();
                String cgcCpf = element.getElementsByTagName("cgcCpf").item(0).getTextContent();

                clientes.add(new Cliente(codCli, nomCli, apeCli, cgcCpf));
            }
        }
        return clientes;
    }

    private HashMap<String, String> prepareBaseParams(String codEmp, String codFil) {
        HashMap<String, String> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        return params;
    }

    public List<ProdutoDerivacao> getProdutos(String codEmp, String codFil, String token) throws IOException, ParserConfigurationException, SAXException {
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        addParamsForProdutos(params);
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);

        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_produtos", "ConsultarGeral_4", user, pswd, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getProdutosFromXml(xml);
    }

    // TODO: refatorar
    private List<ProdutoDerivacao> getProdutosFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<ProdutoDerivacao> produtos = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "produto");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) nNode;
                String codPro = element.getElementsByTagName("codPro").item(0).getTextContent();
                String codMar = element.getElementsByTagName("codMar").item(0).getTextContent();
                String desPro = element.getElementsByTagName("desNfv").item(0).getTextContent();

                NodeList derivacoes = element.getElementsByTagName("derivacao");
                for (int y = 0; y < derivacoes.getLength(); y++) {
                    Node nNodeDer = derivacoes.item(i);
                    if (nNodeDer.getNodeType() == Node.ELEMENT_NODE) {
                        Element elDer = (Element) nNodeDer;
                        String codDer = elDer.getElementsByTagName("codDer").item(0).getTextContent();
                        String desDer = elDer.getElementsByTagName("desDer").item(0).getTextContent();
                        String codBa2 = elDer.getElementsByTagName("codBa2").item(0).getTextContent();
                        String desCpl = String.format("%s %s", desPro, desDer);

                        produtos.add(new ProdutoDerivacao(codPro, codDer, codMar, desCpl, codBa2));
                    }
                }
            }
        }
        return produtos;
    }

    private void addParamsForProdutos(HashMap<String, String> params) {
        params.put("sitPro", "A");
        params.put("sitDer", "A");
        params.put("sitDer", "A");
    }
}

