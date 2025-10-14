package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class DownloadNFCeResult {
    private String Pdf;

    public static DownloadNFCeResult fromXml(Node nNode) {
        Element element = (Element) nNode;
        String Pdf = element.getElementsByTagName("Pdf").item(0).getTextContent();

        return new DownloadNFCeResult(Pdf);
    }
}
