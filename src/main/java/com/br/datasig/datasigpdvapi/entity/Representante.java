package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
public class Representante { //TODO: ver se o FrontEnd vai precisar de mais dados. Se sim, colocar em funcao de retorno do WS
    private String codRep;
    private String nomRep;
    private String apeRep;
    private String tipRep;
    private String indPdv;

    public static Representante fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codRep = element.getElementsByTagName("codRep").item(0).getTextContent();
        String nomRep = element.getElementsByTagName("nomRep").item(0).getTextContent();
        String apeRep = element.getElementsByTagName("apeRep").item(0).getTextContent();
        String tipRep = element.getElementsByTagName("tipRep").item(0).getTextContent();

        List<CampoUsuarioRepresentante> camposUsuario = new ArrayList<>();
        NodeList camposUsuarioNodeList = element.getElementsByTagName("campoUsuarioRepresentante");
        for (int i = 0; i < camposUsuarioNodeList.getLength(); i++) {
            Node campoUsuarioNode = camposUsuarioNodeList.item(i);
            if (campoUsuarioNode.getNodeType() == Node.ELEMENT_NODE) {
                Element campoEl = (Element) campoUsuarioNode;
                String campo = campoEl.getElementsByTagName("campo").item(0).getTextContent();
                String valor = campoEl.getElementsByTagName("valor").item(0).getTextContent();

                camposUsuario.add(new CampoUsuarioRepresentante(campo, valor));
            }
        }
        Optional<CampoUsuarioRepresentante> indPdvOpt = camposUsuario.stream().filter(campo -> campo.getCampo().equals("USU_INDPDV")).findFirst();
        String indPdv = indPdvOpt.isPresent() ? indPdvOpt.get().getValor() : "N";
        if (indPdv == null || indPdv.isEmpty()) indPdv = "N";
        return new Representante(codRep, nomRep, apeRep, tipRep, indPdv);
    }
}

@Data
@AllArgsConstructor
class CampoUsuarioRepresentante {
    private String campo;
    private String valor;
}
