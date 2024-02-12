package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

        return new RetornoPedido(cnpjFilial, codEmp, codFil, ideExt, msgRet, numPed, retorno, sitPed, tipPed, tipRet);
    }
}
