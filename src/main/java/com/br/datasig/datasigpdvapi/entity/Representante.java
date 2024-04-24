package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
public class Representante { //TODO: ver se o FrontEnd vai precisar de mais dados. Se sim, colocar em funcao de retorno do WS
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
