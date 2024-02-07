package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class TabelaPreco {
    private String codTpr;

    public static TabelaPreco fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codTpr = element.getElementsByTagName("CODTPR").item(0).getTextContent();

        return new TabelaPreco(codTpr);
    }
}
