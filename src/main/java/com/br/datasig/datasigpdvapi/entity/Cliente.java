package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class Cliente { //TODO: ver se o FrontEnd vai precisar de mais dados. Se sim, colocar em funcao de retorno do WS
    private String codCli;
    private String nomCli;
    private String apeCli;
    private String cgcCpf;

    public static Cliente fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codCli = element.getElementsByTagName("codCli").item(0).getTextContent();
        String nomCli = element.getElementsByTagName("nomCli").item(0).getTextContent();
        String apeCli = element.getElementsByTagName("apeCli").item(0).getTextContent();
        String cgcCpf = element.getElementsByTagName("cgcCpf").item(0).getTextContent();

        return new Cliente(codCli, nomCli, apeCli, cgcCpf);
    }
}
