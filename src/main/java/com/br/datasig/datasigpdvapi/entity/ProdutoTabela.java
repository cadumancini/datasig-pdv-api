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
    private String codBar;
    private String codTpr;
    private String datIni;
    private String desPro;
    private Double preBas;
    private int qtdMax;

    public static ProdutoTabela fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codPro = element.getElementsByTagName("codPro").item(0).getTextContent();
        String codDer = element.getElementsByTagName("codDer").item(0).getTextContent();
        String codBar = element.getElementsByTagName("codBar").item(0).getTextContent();
        String codTpr = element.getElementsByTagName("codTpr").item(0).getTextContent();
        String datIni = element.getElementsByTagName("datIni").item(0).getTextContent();
        String desPro = element.getElementsByTagName("desPro").item(0).getTextContent();
        Double preBas = Double.parseDouble(element.getElementsByTagName("preBas").item(0).getTextContent().replace(",", "."));
        int qtdMax = Integer.parseInt(element.getElementsByTagName("qtdMax").item(0).getTextContent());

        return new ProdutoTabela(codPro, codDer, codBar, codTpr, datIni, desPro, preBas, qtdMax);
    }
}
