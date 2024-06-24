package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ParamsPDV {
    private String codCli;
    private String codDep;
    private String codInt;
    private String codSnf;
    private String codTpr;
    private String dscTot;
    private String ideCsc;
    private String logSis;
    private String numCsc;
    private String senSis;
    private String sigInt;
    private String snfNfc;
    private String tnsNfv;
    private String tnsPed;
    private String tnsOrc;
    private String codCpg;
    private String regCan;
    private String regFat;
    private String regInu;
    private String regRet;
    private List<Deposito> depositos;

    public static ParamsPDV fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codCli = element.getElementsByTagName("codCli").item(0).getTextContent();
        String codDep = element.getElementsByTagName("codDep").item(0).getTextContent();
        String codInt = element.getElementsByTagName("codInt").item(0).getTextContent();
        String codSnf = element.getElementsByTagName("codSnf").item(0).getTextContent();
        String codTpr = element.getElementsByTagName("codTpr").item(0).getTextContent();
        String dscTot = element.getElementsByTagName("dscTot").item(0).getTextContent();
        String ideCsc = element.getElementsByTagName("ideCsc").item(0).getTextContent();
        String logNfc = element.getElementsByTagName("logSis").item(0).getTextContent();
        String numCsc = element.getElementsByTagName("numCsc").item(0).getTextContent();
        String senNfc = element.getElementsByTagName("senSis").item(0).getTextContent();
        String sigInt = element.getElementsByTagName("sigInt").item(0).getTextContent();
        String snfNfc = element.getElementsByTagName("snfNfc").item(0).getTextContent();
        String tnsNfv = element.getElementsByTagName("tnsNfv").item(0).getTextContent();
        String tnsPed = element.getElementsByTagName("tnsPed").item(0).getTextContent();
        String tnsOrc = element.getElementsByTagName("tnsOrc").item(0).getTextContent();
        String codCpg = element.getElementsByTagName("codCpg").item(0).getTextContent();
        String regCan = element.getElementsByTagName("regCan").item(0).getTextContent();
        String regFat = element.getElementsByTagName("regFat").item(0).getTextContent();
        String regInu = element.getElementsByTagName("regInu").item(0).getTextContent();
        String regRet = element.getElementsByTagName("regRet").item(0).getTextContent();

        return new ParamsPDV(codCli, codDep, codInt, codSnf, codTpr, dscTot, ideCsc, logNfc,
                numCsc, senNfc, sigInt, snfNfc, tnsNfv, tnsPed, tnsOrc, codCpg, regCan, regFat, regInu, regRet,
                getDepositos(element));
    }

    private static List<Deposito> getDepositos(Element element) {
        List<Deposito> depositos = new ArrayList<>();
        NodeList depositosList = element.getElementsByTagName("deposito");
        for (int i = 0; i < depositosList.getLength(); i++) {
            Node nNodeDep = depositosList.item(i);
            if (nNodeDep.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) nNodeDep;
                String codDep = el.getElementsByTagName("codDep").item(0).getTextContent();
                String desDep = el.getElementsByTagName("desDep").item(0).getTextContent();
                depositos.add(new Deposito(codDep, desDep));
            }
        }
        return depositos;
    }
}
