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
public class CondicaoPagamento {
    private String codCpg;
    private String abrCpg;
    private String desCpg;
    private String tipPar;
    private int qtdParCpg;
    private List<Parcela> parcelas;

    public static CondicaoPagamento fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codCpg = element.getElementsByTagName("codCpg").item(0).getTextContent();
        String abrCpg = element.getElementsByTagName("abrCpg").item(0).getTextContent();
        String desCpg = element.getElementsByTagName("desCpg").item(0).getTextContent();
        String tipPar = element.getElementsByTagName("tipPar").item(0).getTextContent();
        int qtdParCpg = Integer.parseInt(element.getElementsByTagName("parCpg").item(0).getTextContent());

        List<Parcela> parcelasList = new ArrayList<>();
        NodeList parcelasNode = element.getElementsByTagName("parcelas");
        for (int i = 0; i < parcelasNode.getLength(); i++) {
            Node nNodePar = parcelasNode.item(i);
            if (nNodePar.getNodeType() == Node.ELEMENT_NODE) {
                Element elPar = (Element) nNodePar;
                int seqIcp = Integer.parseInt(elPar.getElementsByTagName("seqIcp").item(0).getTextContent());
                int diaPar = Integer.parseInt(elPar.getElementsByTagName("diaPar").item(0).getTextContent());
                int qtdPar = Integer.parseInt(elPar.getElementsByTagName("qtdPar").item(0).getTextContent());

                parcelasList.add(new Parcela(seqIcp, diaPar, qtdPar));
            }
        }

        return new CondicaoPagamento(codCpg, abrCpg, desCpg, tipPar, qtdParCpg, parcelasList);
    }
}
