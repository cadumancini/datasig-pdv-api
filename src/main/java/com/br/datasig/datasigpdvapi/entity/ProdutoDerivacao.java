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
public class ProdutoDerivacao { //TODO: ver se o FrontEnd vai precisar de mais dados. Se sim, colocar em funcao de retorno do WS
    private String codPro;
    private String codDer;
    private String codMar;
    private String desPro;
    private String codBa2;
    private Double preBas;

    public static List<ProdutoDerivacao> fromXml(Node nNode) {
        List<ProdutoDerivacao> produtos = new ArrayList<>();
        Element element = (Element) nNode;
        String codPro = element.getElementsByTagName("codPro").item(0).getTextContent();
        String codMar = element.getElementsByTagName("codMar").item(0).getTextContent();
        String desPro = element.getElementsByTagName("desNfv").item(0).getTextContent();

        NodeList derivacoes = element.getElementsByTagName("derivacao");
        for (int i = 0; i < derivacoes.getLength(); i++) {
            Node nNodeDer = derivacoes.item(i);
            if (nNodeDer.getNodeType() == Node.ELEMENT_NODE) {
                Element elDer = (Element) nNodeDer;
                String codDer = elDer.getElementsByTagName("codDer").item(0).getTextContent();
                String desDer = elDer.getElementsByTagName("desDer").item(0).getTextContent();
                String codBa2 = elDer.getElementsByTagName("codBa2").item(0).getTextContent();
                String desCpl = String.format("%s %s", desPro, desDer);

                produtos.add(new ProdutoDerivacao(codPro, codDer, codMar, desCpl, codBa2, (double) 0));
            }
        }
        return produtos;
    }
}
