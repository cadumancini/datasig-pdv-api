package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class Representante {
    private String codRep;
    private String nomRep;
    private String apeRep;

    public static Representante fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codRep = element.getElementsByTagName("codRep").item(0).getTextContent();
        String nomRep = element.getElementsByTagName("nomRep").item(0).getTextContent();
        String apeRep = element.getElementsByTagName("apeRep").item(0).getTextContent();

        return new Representante(codRep, nomRep, apeRep);
    }
}
