package com.br.datasig.datasigpdvapi.entity;

import com.br.datasig.datasigpdvapi.service.nfce.NFCeManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class UltimoNumNFC {
    private String codSel;
    private String numCgc;
    private String ultNum;

    public static UltimoNumNFC fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codSel = element.getElementsByTagName("codSel").item(0).getTextContent();
        String numCgc = element.getElementsByTagName("numCgc").item(0).getTextContent();
        String ultNum = element.getElementsByTagName("ultNum").item(0).getTextContent();

        int ultNumInt = Integer.parseInt(ultNum) + NFCeManager.getInstance().getNumberOfNFCe() + 1;

        return new UltimoNumNFC(codSel, numCgc, String.valueOf(ultNumInt));
    }
}