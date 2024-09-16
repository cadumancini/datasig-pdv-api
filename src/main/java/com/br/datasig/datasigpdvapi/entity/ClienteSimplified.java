package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class ClienteSimplified {
    private String codCli;
    private String nomCli;
    private String apeCli;
    private String cgcCpf;

    public static ClienteSimplified fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codCli = element.getElementsByTagName("codCli").item(0).getTextContent();
        String nomCli = element.getElementsByTagName("nomCli").item(0).getTextContent();
        String apeCli = element.getElementsByTagName("apeCli").item(0).getTextContent();
        String cgcCpf = element.getElementsByTagName("cgcCpf").item(0).getTextContent();

        return new ClienteSimplified(codCli.trim(), nomCli.trim(), apeCli.trim(), cgcCpf.trim());
    }
}
