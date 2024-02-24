package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class FormaPagamento { //TODO: ver se o FrontEnd vai precisar de mais dados. Se sim, colocar em funcao de retorno do WS
    private String codFpg;
    private String abrFpg;
    private String desFpg;
    private String codOpe;
    private String tipFpg;

    public static FormaPagamento fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codFpg = element.getElementsByTagName("codFpg").item(0).getTextContent();
        String abrFpg = element.getElementsByTagName("abrFpg").item(0).getTextContent();
        String desFpg = element.getElementsByTagName("desFpg").item(0).getTextContent();
        String codOpe = element.getElementsByTagName("codOpe").item(0).getTextContent();
        String tipFpg = element.getElementsByTagName("tipFpg").item(0).getTextContent();

        return new FormaPagamento(codFpg, abrFpg, desFpg, codOpe, tipFpg);
    }
}
