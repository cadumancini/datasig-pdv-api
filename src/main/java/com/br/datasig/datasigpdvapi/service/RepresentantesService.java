package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.Representante;
import com.br.datasig.datasigpdvapi.entity.TabelaPreco;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class RepresentantesService extends WebServiceRequestsService {
    public List<Representante> getRepresentantes(String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, Object> params = prepareBaseParams(codEmp, codFil);
        String xml = soapClient.requestFromSeniorWS("PDV_DS_ConsultaRepresentante", "Consulta", token, "0", params, false);

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

    public List<TabelaPreco> getTabelasPrecoPorRepresentantes(String token, String codRep) throws IOException, ParserConfigurationException, SAXException, SOAPClientException, TransformerException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, Object> params = prepareBaseParams(codEmp, codFil);
        addParamsForTabelaPreco(params, codRep);
        String xml = soapClient.requestFromSeniorWS("PDV_DS_ConsultaRepresentanteXTabelaPreco", "Representante", token, "0", params, false);

        XmlUtils.validateXmlResponse(xml);
        return getTabelasPrecoFromXml(xml);
    }

    private List<TabelaPreco> getTabelasPrecoFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<TabelaPreco> tabelas = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "TABELA");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                tabelas.add(TabelaPreco.fromXml(nNode));
            }
        }
        if (tabelas.isEmpty()) throw new ResourceNotFoundException("Nenhuma tabela de preço encontrada para o representante!");
        return tabelas;
    }

    private void addParamsForTabelaPreco(HashMap<String, Object> params, String codRep) {
        params.put("CODREP", codRep);
    }

    public Representante getRepresentante(String token, String codRep) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        List<Representante> representantes = getRepresentantes(token);
        Optional<Representante> representante = representantes.stream().filter(rep -> rep.getCodRep().equals(codRep)).findFirst();
        return representante.orElse(null);
    }
}
