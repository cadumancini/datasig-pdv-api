package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.ConsultaTitulo;
import com.br.datasig.datasigpdvapi.exceptions.ResourceNotFoundException;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@Component
public class TituloService extends WebServiceRequestsService {
    public List<ConsultaTitulo> getTitulos(String token, String codFpg, String codRep, String datIni, String datFim,
                                           String numNfv, String sitDoe, String sitTit)
            throws SOAPClientException, ParserConfigurationException, IOException, SAXException, ParseException, TransformerException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, Object> params = prepareBaseParams(codEmp, codFil);
        addParamsForConsultaTitulos(params, codFpg, codRep, datIni, datFim, numNfv, sitDoe, sitTit);

        String xml = soapClient.requestFromSeniorWS("PDV_DS_ConsultaTitulos", "Consultar", token, "0", params, false);
        XmlUtils.validateXmlResponse(xml);

        List<ConsultaTitulo> titulos = getTitutlosFromXml(xml);
        if(!titulos.isEmpty()) return titulos;

        throw new ResourceNotFoundException("Nenhum título encontrado!");
    }

    private void addParamsForConsultaTitulos(HashMap<String, Object> params, String codFpg, String codRep, String datIni,
                                             String datFim, String numNfv, String sitDoe, String sitTit) {
        params.put("codFpg", codFpg == null ? "" : codFpg);
        params.put("codRep", codRep == null ? "" : codRep);
        params.put("datIni", datIni == null ? "" : datIni);
        params.put("datFim", datFim == null ? "" : datFim);
        params.put("numNfv", numNfv == null ? "" : numNfv);
        params.put("sitDoe", sitDoe == null ? "" : sitDoe);
        params.put("sitTit", sitTit == null ? "" : sitTit);
    }

    private List<ConsultaTitulo> getTitutlosFromXml(String xml) throws ParserConfigurationException, IOException, SAXException, ParseException {
        List<ConsultaTitulo> titulos = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "dados");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                titulos.add(ConsultaTitulo.fromXml(nNode));
            }
        }
        titulos.sort(Comparator.comparing(ConsultaTitulo::getNumNfvInt).reversed()
                .thenComparing(ConsultaTitulo::getNumTit));
        return titulos;
    }
}
