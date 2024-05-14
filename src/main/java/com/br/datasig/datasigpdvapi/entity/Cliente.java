package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class Cliente {
    private String codCli;
    private String nomCli;
    private String apeCli;
    private String cgcCpf;
    private String tipCli;
    private String endCli;
    private String baiCli;
    private String cepCli;
    private String cidCli;
    private String cplEnd;
    private String fonCli;
    private String intNet;
    private String nenCli;
    private String sigUfs;

    public static Cliente fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codCli = element.getElementsByTagName("codCli").item(0).getTextContent();
        String nomCli = element.getElementsByTagName("nomCli").item(0).getTextContent();
        String apeCli = element.getElementsByTagName("apeCli").item(0).getTextContent();
        String cgcCpf = element.getElementsByTagName("cgcCpf").item(0).getTextContent();
        String tipCli = element.getElementsByTagName("tipCli").item(0).getTextContent();
        String endCli = element.getElementsByTagName("endCli").item(0).getTextContent();
        String baiCli = element.getElementsByTagName("baiCli").item(0).getTextContent();
        String cepCli = element.getElementsByTagName("cepCli").item(0).getTextContent();
        String cidCli = element.getElementsByTagName("cidCli").item(0).getTextContent();
        String cplEnd = element.getElementsByTagName("cplEnd").item(0).getTextContent();
        String fonCli = element.getElementsByTagName("fonCli").item(0).getTextContent();
        String intNet = element.getElementsByTagName("intNet").item(0).getTextContent();
        String nenCli = element.getElementsByTagName("nenCli").item(0).getTextContent();
        String sigUfs = element.getElementsByTagName("sigUfs").item(0).getTextContent();

        return new Cliente(codCli, nomCli, apeCli, cgcCpf, tipCli, endCli, baiCli, cepCli,
                cidCli, cplEnd, fonCli, intNet, nenCli, sigUfs);
    }
}
