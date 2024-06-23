package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class Deposito {
    private String codDep;
    private String desDep;

    public static Deposito fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codDep = element.getElementsByTagName("codDep").item(0).getTextContent();
        String desDep = element.getElementsByTagName("desDep").item(0).getTextContent();

        return new Deposito(codDep, desDep);
    }
}
