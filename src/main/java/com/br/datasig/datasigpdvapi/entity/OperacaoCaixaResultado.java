package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class OperacaoCaixaResultado {
    private String codEmp;
    private String datMov;
    private String resultado;
    private String vlrMov;
    private String seqMov;

    public static OperacaoCaixaResultado fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codEmp = element.getElementsByTagName("codEmp").item(0).getTextContent();
        String datMov = element.getElementsByTagName("datMov").item(0).getTextContent();
        String resultado = element.getElementsByTagName("resultado").item(0).getTextContent();
        String vlrMov = element.getElementsByTagName("vlrMov").item(0).getTextContent();
        String seqMov = element.getElementsByTagName("seqMov").item(0).getTextContent();

        return new OperacaoCaixaResultado(codEmp, datMov, resultado, vlrMov, seqMov);
    }
}
