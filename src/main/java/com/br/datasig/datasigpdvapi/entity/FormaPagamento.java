package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class FormaPagamento {
    private String codFpg;
    private String abrFpg;
    private String desFpg;
    private String codOpe;
    private String tipFpg;
    private List<CondicaoPagamento> condicoes;

    public static FormaPagamento fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codFpg = element.getElementsByTagName("codFpg").item(0).getTextContent();
        String abrFpg = element.getElementsByTagName("abrFpg").item(0).getTextContent();
        String desFpg = element.getElementsByTagName("desFpg").item(0).getTextContent();
        String codOpe = element.getElementsByTagName("codOpe").item(0).getTextContent();
        String tipFpg = element.getElementsByTagName("tipFpg").item(0).getTextContent();

        List<CondicaoPagamento> condicoes = new ArrayList<>();
        NodeList condicoesNode = element.getElementsByTagName("condicao");
        for (int i = 0; i < condicoesNode.getLength(); i++) {
            Node nNodeCon = condicoesNode.item(i);
            if (nNodeCon.getNodeType() == Node.ELEMENT_NODE) {
                condicoes.add(CondicaoPagamento.fromXml(nNodeCon));
            }
        }

        return new FormaPagamento(codFpg, abrFpg, desFpg, codOpe, tipFpg, condicoes);
    }
}
