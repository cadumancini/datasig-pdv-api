package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class ProdutoTabela {
    private String codPro;
    private String codDer;
    private String codTpr;

    public static ProdutoTabela fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codPro = element.getElementsByTagName("codPro").item(0).getTextContent();
        String codDer = element.getElementsByTagName("codDer").item(0).getTextContent();
        String codTpr = element.getElementsByTagName("codTpr").item(0).getTextContent();

        return new ProdutoTabela(codPro, codDer, codTpr);
    }
}
