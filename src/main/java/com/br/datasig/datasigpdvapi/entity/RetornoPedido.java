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
public class RetornoPedido {
    private String cnpjFilial;
    private String codEmp;
    private String codFil;
    private String ideExt;
    private String msgRet;
    private String numPed;
    private String retorno;
    private String sitPed;
    private String tipPed;
    private String tipRet;
    private List<RetornoItemPedido> itens;

    public static RetornoPedido fromXml(Node nNode) {
        Element element = (Element) nNode;
        String cnpjFilial = element.getElementsByTagName("cnpjFilial").item(0).getTextContent();
        String codEmp = element.getElementsByTagName("codEmp").item(0).getTextContent();
        String codFil = element.getElementsByTagName("codFil").item(0).getTextContent();
        String ideExt = element.getElementsByTagName("ideExt").item(0).getTextContent();
        String msgRet = element.getElementsByTagName("msgRet").item(0).getTextContent();
        String numPed = element.getElementsByTagName("numPed").item(0).getTextContent();
        String retorno = element.getElementsByTagName("retorno").item(0).getTextContent();
        String sitPed = element.getElementsByTagName("sitPed").item(0).getTextContent();
        String tipPed = element.getElementsByTagName("tipPed").item(0).getTextContent();
        String tipRet = element.getElementsByTagName("tipRet").item(0).getTextContent();

        List<RetornoItemPedido> gridProList = new ArrayList<>();
        NodeList gridPro = element.getElementsByTagName("gridPro");
        for (int i = 0; i < gridPro.getLength(); i++) {
            Node nNodePro = gridPro.item(i);
            if (nNodePro.getNodeType() == Node.ELEMENT_NODE) {
                Element elPro = (Element) nNodePro;
                String cnpjFilialPro = elPro.getElementsByTagName("cnpjFilial").item(0).getTextContent();
                String codEmpPro = elPro.getElementsByTagName("codEmp").item(0).getTextContent();
                String codFilPro = elPro.getElementsByTagName("codFil").item(0).getTextContent();
                String numPedPro = elPro.getElementsByTagName("numPed").item(0).getTextContent();
                String seqIpdPro = elPro.getElementsByTagName("seqIpd").item(0).getTextContent();
                String retornoPro = elPro.getElementsByTagName("retorno").item(0).getTextContent();
                String sitIpdPro = elPro.getElementsByTagName("sitIpd").item(0).getTextContent();

                gridProList.add(new RetornoItemPedido(cnpjFilialPro, codEmpPro, codFilPro, numPedPro, seqIpdPro, retornoPro, sitIpdPro));
            }
        }

        return new RetornoPedido(cnpjFilial, codEmp, codFil, ideExt, msgRet, numPed, retorno, sitPed, tipPed, tipRet, gridProList);
    }
}
