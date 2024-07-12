package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class ConsultaCEP {
    private String cep;
    private String logradouro;
    private String bairro;
    private String localidade;
    private String uf;

    public static ConsultaCEP fromXml(Node nNode) {
        Element element = (Element) nNode;
        String cep = element.getElementsByTagName("cep").item(0).getTextContent();
        String logradouro = element.getElementsByTagName("logradouro").item(0).getTextContent();
        String bairro = element.getElementsByTagName("bairro").item(0).getTextContent();
        String localidade = element.getElementsByTagName("localidade").item(0).getTextContent();
        String uf = element.getElementsByTagName("uf").item(0).getTextContent();

        return new ConsultaCEP(cep, logradouro, bairro, localidade, uf);
    }
}
