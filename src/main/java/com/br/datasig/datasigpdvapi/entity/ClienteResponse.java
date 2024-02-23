package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class ClienteResponse {
    private String codCli;
    private String codFor;
    private String retorno;
    private String cgcCpf;

    public static ClienteResponse fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codCli = element.getElementsByTagName("codCli").item(0).getTextContent();
        String codFor = element.getElementsByTagName("codFor").item(0).getTextContent();
        String retorno = element.getElementsByTagName("retorno").item(0).getTextContent();
        String cgcCpf = element.getElementsByTagName("cgcCpf").item(0).getTextContent();

        return new ClienteResponse(codCli, codFor, retorno, cgcCpf);
    }
}
