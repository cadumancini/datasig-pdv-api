package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class ParamsEmpresa {
    private String codEmp;
    private String codFil;
    private String codUsu;
    private String sitUsu;
    private boolean usaTEF;

    public static ParamsEmpresa fromXml(Node nNode, boolean usaTEF) {
        Element element = (Element) nNode;
        String codEmp = element.getElementsByTagName("EMPATI").item(0).getTextContent();
        String codFil = element.getElementsByTagName("FILATI").item(0).getTextContent();
        String codUsu = element.getElementsByTagName("CODUSU").item(0).getTextContent();
        String sitUsu = element.getElementsByTagName("SITUSU").item(0).getTextContent();

        return new ParamsEmpresa(codEmp, codFil, codUsu, sitUsu, usaTEF);
    }
}
