package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class RetornoNFCeCriada {
    private String chvNfc;
    private String sucesso;
    private String sitNfv;
    private String mensagem;

    public static RetornoNFCeCriada fromXml(Node nNode) {
        Element element = (Element) nNode;
        String chvNfc = element.getElementsByTagName("chvNfc").item(0).getTextContent();
        String sucesso = element.getElementsByTagName("sucesso").item(0).getTextContent();
        String mensagem = element.getElementsByTagName("mensagem").item(0).getTextContent();
        String sitNfv = "";
        try {
            sitNfv = element.getElementsByTagName("sitNfv").item(0).getTextContent();
        } catch (Exception e) {}

        return new RetornoNFCeCriada(chvNfc, sucesso, sitNfv, mensagem);
    }
}
