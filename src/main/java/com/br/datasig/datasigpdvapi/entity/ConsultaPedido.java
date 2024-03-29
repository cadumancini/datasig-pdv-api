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
public class ConsultaPedido {
    private String codCli;
    private String codCpg;
    private String codEmp;
    private String codFil;
    private String codFpg;
    private String codRep;
    private String codTns;
    private String datEmi;
    private String numPed;
    private String sitPed;
    private List<ConsultaItemPedido> itens;

    public static ConsultaPedido fromXml(Node node) {
        Element element = (Element) node;
        String codCli = element.getElementsByTagName("codCli").item(0).getTextContent();
        String codCpg = element.getElementsByTagName("codCpg").item(0).getTextContent();
        String codEmp = element.getElementsByTagName("codEmp").item(0).getTextContent();
        String codFil = element.getElementsByTagName("codFil").item(0).getTextContent();
        String codFpg = element.getElementsByTagName("codFpg").item(0).getTextContent();
        String codRep = element.getElementsByTagName("codRep").item(0).getTextContent();
        String codTns = element.getElementsByTagName("codTns").item(0).getTextContent();
        String datEmi = element.getElementsByTagName("datEmi").item(0).getTextContent();
        String numPed = element.getElementsByTagName("numPed").item(0).getTextContent();
        String sitPed = element.getElementsByTagName("sitPed").item(0).getTextContent();
        List<ConsultaItemPedido> itens = getItensPedido(element);

        return new ConsultaPedido(codCli, codCpg, codEmp, codFil, codFpg, codRep, codTns, datEmi, numPed, sitPed, itens);
    }

    private static List<ConsultaItemPedido> getItensPedido(Element element) {
        List<ConsultaItemPedido> itens = new ArrayList<>();
        NodeList itensList = element.getElementsByTagName("itens");
        for (int i = 0; i < itensList.getLength(); i++) {
            Node nNodeItem = itensList.item(i);
            if (nNodeItem.getNodeType() == Node.ELEMENT_NODE) {
                Element elItem = (Element) nNodeItem;
                String codDer = elItem.getElementsByTagName("codDer").item(0).getTextContent();
                String codPro = elItem.getElementsByTagName("codPro").item(0).getTextContent();
                String codTpr = elItem.getElementsByTagName("codTpr").item(0).getTextContent();
                String preUni = elItem.getElementsByTagName("preUni").item(0).getTextContent();
                String qtdAbe = elItem.getElementsByTagName("qtdAbe").item(0).getTextContent();
                String qtdCan = elItem.getElementsByTagName("qtdCan").item(0).getTextContent();
                String qtdFat = elItem.getElementsByTagName("qtdFat").item(0).getTextContent();
                String qtdPed = elItem.getElementsByTagName("qtdPed").item(0).getTextContent();
                String seqIpd = elItem.getElementsByTagName("seqIpd").item(0).getTextContent();
                String sitIpd = elItem.getElementsByTagName("sitIpd").item(0).getTextContent();

                itens.add(new ConsultaItemPedido(codDer, codPro, codTpr, preUni, qtdAbe, qtdCan, qtdFat, qtdPed, seqIpd, sitIpd));
            }
        }
        return itens;
    }
}
