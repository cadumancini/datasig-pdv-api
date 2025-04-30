package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class ParamsImpressao {
    private String codIp;
    private String codPDV;
    private String dirNfc;
    private String logNfc;
    private String nomImp;
    private String senNfc;
    private String tipDoc;
    private String urlSde;

    public static ParamsImpressao fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codIp = element.getElementsByTagName("codIp").item(0).getTextContent();
        String codPDV = element.getElementsByTagName("codPdv").item(0).getTextContent();
        String dirNfc = element.getElementsByTagName("dirNfc").item(0).getTextContent();
        String logNfc = element.getElementsByTagName("logNfc").item(0).getTextContent();
        String nomImp = element.getElementsByTagName("nomImp").item(0).getTextContent();
        String senNfc = element.getElementsByTagName("senNfc").item(0).getTextContent();
        String tipDoc = element.getElementsByTagName("tipDoc").item(0).getTextContent();
        String urlSde = element.getElementsByTagName("urlSde").item(0).getTextContent();

        return new ParamsImpressao(codIp, codPDV, dirNfc, logNfc, nomImp, senNfc, tipDoc, urlSde);
    }
}
