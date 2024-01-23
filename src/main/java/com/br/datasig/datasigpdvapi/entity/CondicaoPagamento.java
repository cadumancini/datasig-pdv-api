package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class CondicaoPagamento { //TODO: ver se o FrontEnd vai precisar de mais dados. Se sim, colocar em funcao de retorno do WS
    private String codCpg;
    private String abrCpg;
    private String desCpg;

    public static CondicaoPagamento fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codCpg = element.getElementsByTagName("codCpg").item(0).getTextContent();
        String abrCpg = element.getElementsByTagName("abrCpg").item(0).getTextContent();
        String desCpg = element.getElementsByTagName("desCpg").item(0).getTextContent();

        return new CondicaoPagamento(codCpg, abrCpg, desCpg);
    }
}
