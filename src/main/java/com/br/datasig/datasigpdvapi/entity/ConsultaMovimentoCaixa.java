package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class ConsultaMovimentoCaixa {
    private String codTns;
    private String datMov;
    private String debCre;
    private String hisMov;
    private String numCxa;
    private String seqMov;
    private Double vlrMov;
    private Double salAcu;

    public static ConsultaMovimentoCaixa fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codTns = element.getElementsByTagName("codTns").item(0).getTextContent();
        String datMov = element.getElementsByTagName("datMov").item(0).getTextContent();
        String debCre = element.getElementsByTagName("debCre").item(0).getTextContent();
        String hisMov = element.getElementsByTagName("hisMov").item(0).getTextContent();
        String numCxa = element.getElementsByTagName("numCxa").item(0).getTextContent();
        String seqMov = element.getElementsByTagName("seqMov").item(0).getTextContent();
        Double vlrMov = parseField(element, "vlrMov");
        Double salAcu = parseField(element, "salAcu");

        return new ConsultaMovimentoCaixa(codTns, datMov, debCre, hisMov, numCxa, seqMov, vlrMov, salAcu);
    }

    private static Double parseField(Element element, String field) {
        var strValue = element.getElementsByTagName(field).item(0).getTextContent();
        var dblValue = Double.parseDouble("0");
        if (!strValue.isEmpty()) {
            dblValue = Double.parseDouble(strValue.replace(".", "").replace(",", "."));
        }
        return dblValue;
    }
}
