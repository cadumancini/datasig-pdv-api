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
    private List<Operadora> operadoras;

    public static CondicaoPagamento fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codCpg = element.getElementsByTagName("codCpg").item(0).getTextContent();
        String abrCpg = element.getElementsByTagName("abrCpg").item(0).getTextContent();
        String desCpg = element.getElementsByTagName("desCpg").item(0).getTextContent();
        String tipPar = element.getElementsByTagName("tipPar").item(0).getTextContent();
        int qtdParCpg = Integer.parseInt(element.getElementsByTagName("parCpg").item(0).getTextContent());

        return new CondicaoPagamento(codCpg, abrCpg, desCpg, tipPar, qtdParCpg, getParcelas(element), getOperadoras(element));
    }

    private static List<Parcela> getParcelas(Element element) {
        List<Parcela> parcelas = new ArrayList<>();
        NodeList parcelasNode = element.getElementsByTagName("parcelas");
        for (int i = 0; i < parcelasNode.getLength(); i++) {
            Node nNode = parcelasNode.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elPar = (Element) nNode;
                int seqIcp = Integer.parseInt(elPar.getElementsByTagName("seqIcp").item(0).getTextContent());
                int diaPar = Integer.parseInt(elPar.getElementsByTagName("diaPar").item(0).getTextContent());
                int qtdPar = Integer.parseInt(elPar.getElementsByTagName("qtdPar").item(0).getTextContent());
                String perPar = elPar.getElementsByTagName("perPar").item(0).getTextContent();

                parcelas.add(new Parcela(seqIcp, diaPar, qtdPar, perPar));
            }
        }
        return parcelas;
    }

    private static List<Operadora> getOperadoras(Element element) {
        List<Operadora> operadoras = new ArrayList<>();
        NodeList operadorasNode = element.getElementsByTagName("operadora");
        for (int i = 0; i < operadorasNode.getLength(); i++) {
            Node nNode = operadorasNode.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) nNode;
                String cgcCpf = el.getElementsByTagName("cgcCpf").item(0).getTextContent();
                String codOpe = el.getElementsByTagName("codOpe").item(0).getTextContent();
                String desOpe = el.getElementsByTagName("desOpe").item(0).getTextContent();

                operadoras.add(new Operadora(cgcCpf, codOpe, desOpe));
            }
        }
        return operadoras;
    }
}
