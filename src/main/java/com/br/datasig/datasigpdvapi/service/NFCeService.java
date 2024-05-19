package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.ConsultaNotaFiscal;
import com.br.datasig.datasigpdvapi.entity.SitEdocsResponse;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Component
public class NFCeService extends WebServiceRequestsService {
    private final String numRegGeracaoNFC;
    private final String numRegCancelamentoNFC;
    private final String numRegInutilizacaoNFC;
    private final String numRegSituacaoEDocs;

    public NFCeService(Environment env) {
        numRegGeracaoNFC = env.getProperty("numRegGeracaoNFC");
        numRegCancelamentoNFC = env.getProperty("numRegCancelamentoNFC");
        numRegInutilizacaoNFC = env.getProperty("numRegInutilizacaoNFC");
        numRegSituacaoEDocs = env.getProperty("numRegSituacaoEDocs");
    }

    public String createNFCe(String token, String numPed) throws ParserConfigurationException, IOException, SAXException, SOAPClientException {
        String paramsNFCe = prepareParamsForGeracaoNFCe(token, numPed, numRegGeracaoNFC);
        return extractNfceNumberFromResponse(exeRegra(token, paramsNFCe));
    }

    private String prepareParamsForGeracaoNFCe(String token, String numPed, String numReg) {
        StringBuilder paramsBuilder = getBaseParams(token, numReg);
        appendSIDParam(paramsBuilder, "aNumPedPdv", numPed);

        return paramsBuilder.toString();
    }

    private String extractNfceNumberFromResponse(String response) {
        String[] terms = response.split(" ");
        return terms[1];
    }

    private String getResponseNFCeFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "result");
        if (nList.getLength() == 1) {
            Element element = (Element) nList.item(0);
            return element.getElementsByTagName("resultado").item(0).getTextContent();
        } else {
            throw new WebServiceRuntimeException("Erro na operação com NFC-e");
        }
    }

    public List<ConsultaNotaFiscal> getNFCes(String token, String numNfv, String sitNfv, String datIni, String datFim) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, ParseException {
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

    private List<ConsultaNotaFiscal> getNotasFromXml(String xml) throws ParserConfigurationException, IOException, SAXException, ParseException {
        List<ConsultaNotaFiscal> notas = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "notaFiscal");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                notas.add(ConsultaNotaFiscal.fromXml(nNode));
            }
        }
        notas.sort(Comparator.comparing(ConsultaNotaFiscal::getNumNfvInt).reversed());
        return notas;
    }

    public String cancelarNFCe(String token, String codSnf, String numNfv, String jusCan) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        String paramsCancelamento = preparaParamsForCancelarNFCe(token, codSnf, numNfv, jusCan, numRegCancelamentoNFC);
        return exeRegra(token, paramsCancelamento);
    }

    private String preparaParamsForCancelarNFCe(String token, String codSnf, String numNfv, String jusCan, String numReg) {
        StringBuilder paramsBuilder = getBaseParams(token, numReg);
        appendSIDParam(paramsBuilder, "aCodSnfPDV", codSnf);
        appendSIDParam(paramsBuilder, "aNumNfvPDV", numNfv);
        appendSIDParam(paramsBuilder, "aJusCanPDV", jusCan);

        return paramsBuilder.toString();
    }

    public String inutilizarNFCe(String token, String codSnf, String numNfv, String jusCan) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        String paramsInutilizacao = preparaParamsForCancelarNFCe(token, codSnf, numNfv, jusCan, numRegInutilizacaoNFC);
        return exeRegra(token, paramsInutilizacao);
    }

    private String exeRegra(String token, String params) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", token, "0", params);
        XmlUtils.validateXmlResponse(xml);
        return getResponseNFCeFromXml(xml);
    }

    private StringBuilder getBaseParams(String token, String numReg) {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);

        StringBuilder paramsBuilder = new StringBuilder();
        appendSIDParam(paramsBuilder, "acao", "sid.srv.regra");
        appendSIDParam(paramsBuilder, "numreg", numReg);
        appendSIDParam(paramsBuilder, "aCodEmpPdv", codEmp);
        appendSIDParam(paramsBuilder, "aCodFilPdv", codFil);

        return paramsBuilder;
    }

    public SitEdocsResponse getSitEDocs(String token, String codSnf, String numNfv) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, ParseException {
        String paramsNFCe = prepareParamsForConsultaEDocs(token, codSnf, numNfv, numRegSituacaoEDocs);
        String response = exeRegra(token, paramsNFCe);
        ConsultaNotaFiscal notaFiscal = getNFCes(token, numNfv, null, null, null).get(0);
        return new SitEdocsResponse(response, notaFiscal);
    }

    private String prepareParamsForConsultaEDocs(String token, String codSnf, String numNfv, String numReg) {
        StringBuilder paramsBuilder = getBaseParams(token, numReg);
        appendSIDParam(paramsBuilder, "aCodSnfPDV", codSnf);
        appendSIDParam(paramsBuilder, "aNumNfvPDV", numNfv);

        return paramsBuilder.toString();
    }
}
