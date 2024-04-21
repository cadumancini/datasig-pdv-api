package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.ConsultaNotaFiscal;
import com.br.datasig.datasigpdvapi.exceptions.WebServiceRuntimeException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class NFCeService extends WebServiceRequestsService {
    private final String numRegNFC;

    public NFCeService(Environment env) {
        numRegNFC = env.getProperty("numRegNFC");
    }

    public String createNFCe(String token, String numPed) throws ParserConfigurationException, IOException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        String paramsNFCe = prepareParamsForGeracaoNFCe(codEmp, codFil, numPed);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", token, "0", paramsNFCe);
        XmlUtils.validateXmlResponse(xml);
        return getResponseNFCeFromXml(xml);
    }

    private String prepareParamsForGeracaoNFCe(String codEmp, String codFil, String numPed) {
        StringBuilder paramsBuilder = new StringBuilder();

        appendSIDParam(paramsBuilder, "acao", "sid.srv.regra");
        appendSIDParam(paramsBuilder, "numreg", numRegNFC);
        appendSIDParam(paramsBuilder, "aCodEmpPdv", codEmp);
        appendSIDParam(paramsBuilder, "aCodFilPdv", codFil);
        appendSIDParam(paramsBuilder, "aNumPedPdv", numPed);

        return paramsBuilder.toString();
    }

    private String getResponseNFCeFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "result");
        if (nList.getLength() == 1) {
            Element element = (Element) nList.item(0);
            return element.getElementsByTagName("resultado").item(0).getTextContent();
        } else {
            throw new WebServiceRuntimeException("Erro na geração da NFC-e");
        }
    }

    public List<ConsultaNotaFiscal> getNFCes(String token, String numNfv, String sitNfv, String datIni, String datFim) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, Object> params = prepareBaseParams(codEmp, codFil);
        addParamsForConsultaNFCes(params, token, numNfv, sitNfv, datIni, datFim);

        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_ven_notafiscalsaida", "ConsultarGeral_6", token, "0", params, true);
        XmlUtils.validateXmlResponse(xml);

        return getNotasFromXml(xml);
    }

    private void addParamsForConsultaNFCes(HashMap<String, Object> params, String token, String numNfv, String sitNfv, String datIni, String datFim) {
        params.put("codSnf", "<codSnf>" + TokensManager.getInstance().getParamsPDVFromToken(token).getSnfNfc() + "</codSnf>");
        params.put("numNfv", numNfv == null ? "" : "<numNfv>" + numNfv + "</numNfv>");
        params.put("sitNfv", sitNfv == null ? "" : sitNfv);
        params.put("datGerIni", datIni == null ? "" : datIni);
        params.put("datGerFim", datFim == null ? "" : datFim);
    }

    private List<ConsultaNotaFiscal> getNotasFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<ConsultaNotaFiscal> notas = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "notaFiscal");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                notas.add(ConsultaNotaFiscal.fromXml(nNode));
            }
        }
        return notas;
    }
}
