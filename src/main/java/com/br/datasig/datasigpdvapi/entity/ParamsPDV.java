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
    private String numCsc;
    private String sigInt;
    private String snfNfc;
    private String tnsNfv;
    private String tnsPed;
    private String tnsOrc;
    private String codCpg;
    private String codFpg;
    private String regCan;
    private String regFat;
    private String regInu;
    private String regRet;
    private String nomEmp;
    private String nomFil;
    private String cofAbr;
    private String cofFec;
    private String cofSan;
    private String cxaAbr;
    private String cxaFec;
    private String cxaSan;
    private List<Deposito> depositos;
    private List<Caixa> caixas;
    private List<RamoAtividade> ramos;

    public static ParamsPDV fromXml(Node nNode) {
        Element element = (Element) nNode;

        NodeList paramList = element.getElementsByTagName("parametros");
        Node nNodeParams = paramList.item(0);

        Element el = (Element) nNodeParams;
        String codCli = el.getElementsByTagName("codCli").item(0).getTextContent();
        String codCpg = el.getElementsByTagName("codCpg").item(0).getTextContent();
        String codFpg = el.getElementsByTagName("codFpg").item(0).getTextContent();
        String codDep = el.getElementsByTagName("codDep").item(0).getTextContent();
        String codInt = el.getElementsByTagName("codInt").item(0).getTextContent();
        String codSnf = el.getElementsByTagName("codSnf").item(0).getTextContent();
        String codTpr = el.getElementsByTagName("codTpr").item(0).getTextContent();
        String dscTot = el.getElementsByTagName("dscTot").item(0).getTextContent();
        String ideCsc = el.getElementsByTagName("ideCsc").item(0).getTextContent();
        String nomEmp = el.getElementsByTagName("nomEmp").item(0).getTextContent();
        String nomFil = el.getElementsByTagName("nomFil").item(0).getTextContent();
        String numCsc = el.getElementsByTagName("numCsc").item(0).getTextContent();
        String regCan = el.getElementsByTagName("regCan").item(0).getTextContent();
        String regFat = el.getElementsByTagName("regFat").item(0).getTextContent();
        String regInu = el.getElementsByTagName("regInu").item(0).getTextContent();
        String regRet = el.getElementsByTagName("regRet").item(0).getTextContent();
        String sigInt = el.getElementsByTagName("sigInt").item(0).getTextContent();
        String snfNfc = el.getElementsByTagName("snfNfc").item(0).getTextContent();
        String tnsNfv = el.getElementsByTagName("tnsNfv").item(0).getTextContent();
        String tnsOrc = el.getElementsByTagName("tnsOrc").item(0).getTextContent();
        String tnsPed = el.getElementsByTagName("tnsPed").item(0).getTextContent();
        String cofAbr = el.getElementsByTagName("cofAbr").item(0).getTextContent();
        String cofFec = el.getElementsByTagName("cofFec").item(0).getTextContent();
        String cofSan = el.getElementsByTagName("cofSan").item(0).getTextContent();
        String cxaAbr = el.getElementsByTagName("cxaAbr").item(0).getTextContent();
        String cxaFec = el.getElementsByTagName("cxaFec").item(0).getTextContent();
        String cxaSan = el.getElementsByTagName("cxaSan").item(0).getTextContent();

        return new ParamsPDV(codCli, codDep, codInt, codSnf, codTpr, dscTot, ideCsc,
                numCsc, sigInt, snfNfc, tnsNfv, tnsPed, tnsOrc, codCpg, codFpg, regCan, regFat,
                regInu, regRet, nomEmp, nomFil, cofAbr, cofFec, cofSan, cxaAbr, cxaFec, cxaSan,
                getDepositos(element), getCaixas(element), getRamos(element));
    }

    private static List<RamoAtividade> getRamos(Element element) {
        List<RamoAtividade> ramos = new ArrayList<>();
        NodeList ramosList = element.getElementsByTagName("ramo");
        for (int i = 0; i < ramosList.getLength(); i++) {
            Node nNode = ramosList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) nNode;
                String codRam = el.getElementsByTagName("codRam").item(0).getTextContent();
                String desRam = el.getElementsByTagName("desRam").item(0).getTextContent();
                ramos.add(new RamoAtividade(codRam, desRam));
            }
        }
        return ramos;
    }

    private static List<Deposito> getDepositos(Element element) {
        List<Deposito> depositos = new ArrayList<>();
        NodeList depositosList = element.getElementsByTagName("deposito");
        for (int i = 0; i < depositosList.getLength(); i++) {
            Node nNode = depositosList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) nNode;
                String codDep = el.getElementsByTagName("codDep").item(0).getTextContent();
                String desDep = el.getElementsByTagName("desDep").item(0).getTextContent();
                depositos.add(new Deposito(codDep, desDep));
            }
        }
        return depositos;
    }

    private static List<Caixa> getCaixas(Element element) {
        List<Caixa> caixas = new ArrayList<>();
        NodeList caixasList = element.getElementsByTagName("caixa");
        for (int i = 0; i < caixasList.getLength(); i++) {
            Node nNode = caixasList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) nNode;
                String logSis = el.getElementsByTagName("logSis").item(0).getTextContent();
                String numCco = el.getElementsByTagName("numCco").item(0).getTextContent();
                String numCxa = el.getElementsByTagName("numCxa").item(0).getTextContent();
                caixas.add(new Caixa(logSis, numCco, numCxa));
            }
        }
        return caixas;
    }
}
