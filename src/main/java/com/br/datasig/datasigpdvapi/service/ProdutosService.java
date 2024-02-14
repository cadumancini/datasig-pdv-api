package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.ProdutoDerivacao;
import com.br.datasig.datasigpdvapi.exceptions.ResourceNotFoundException;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class ProdutosService extends WebServiceRequestsService{
    private final String numRegTpr;

    public ProdutosService(Environment env) {
        numRegTpr = env.getProperty("numRegTpr");
    }

    public List<ProdutoDerivacao> getProdutos(String token) throws IOException, ParserConfigurationException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, Object> params = prepareBaseParams(codEmp, codFil);
        addParamsForProdutos(params);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_produtos", "ConsultarGeral_4", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getProdutosFromXml(xml);
    }

    public String getPreco(String token, String codPro, String codDer, String codTpr, String qtdPdv) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String datIni = getCurrentDate();

        String params = prepareParamsForConsultaPrecos(codEmp, codTpr, codPro, codDer, datIni, qtdPdv);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", token, "0", params);
        XmlUtils.validateXmlResponse(xml);
        return getPriceFromXml(xml);
    }

    private String getCurrentDate() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(currentDate);
    }

    private String getPriceFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "result");
        if (nList.getLength() == 1) {
            Element element = (Element) nList.item(0);
            String preBas = element.getElementsByTagName("resultado").item(0).getTextContent();
            return preBas.replace(",", ".");
        } else {
            throw new ResourceNotFoundException("Preço não encontrado para o produto");
        }
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

    private String prepareParamsForConsultaPrecos(String codEmp, String codTpr, String codPro, String codDer, String datIni, String qtdPdv) {
        StringBuilder paramsBuilder = new StringBuilder();

        appendSIDParam(paramsBuilder, "acao", "sid.srv.regra");
        appendSIDParam(paramsBuilder, "numreg", numRegTpr);
        appendSIDParam(paramsBuilder, "aCodEmpPdv", codEmp);
        appendSIDParam(paramsBuilder, "aCodTprPdv", codTpr);
        appendSIDParam(paramsBuilder, "aCodProPdv", codPro);
        appendSIDParam(paramsBuilder, "aCodDerPdv", codDer);
        appendSIDParam(paramsBuilder, "aDatIniPdv", datIni);
        appendSIDParam(paramsBuilder, "aQtdMaxPdv", qtdPdv);

        return paramsBuilder.toString();
    }

    private void addParamsForProdutos(HashMap<String, Object> params) {
        params.put("sitPro", "A");
        params.put("sitDer", "A");
    }
}
