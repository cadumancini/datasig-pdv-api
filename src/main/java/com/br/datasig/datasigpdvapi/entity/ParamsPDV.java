package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class ParamsPDV {
    private String codCli;
    private String codDep;
    private String codInt;
    private String codSnf;
    private String codTpr;
    private String ideCsc;
    private String logNfc;
    private String numCsc;
    private String senNfc;
    private String sigInt;
    private String snfNfc;
    private String tnsNfv;
    private String tnsPed;

    public static ParamsPDV fromXml(Node nNode) {
        Element element = (Element) nNode;
        String codCli = element.getElementsByTagName("codCli").item(0).getTextContent();
        String codDep = element.getElementsByTagName("codDep").item(0).getTextContent();
        String codInt = element.getElementsByTagName("codInt").item(0).getTextContent();
        String codSnf = element.getElementsByTagName("codSnf").item(0).getTextContent();
        String codTpr = element.getElementsByTagName("codTpr").item(0).getTextContent();
        String ideCsc = element.getElementsByTagName("ideCsc").item(0).getTextContent();
        String logNfc = element.getElementsByTagName("logNfc").item(0).getTextContent();
        String numCsc = element.getElementsByTagName("numCsc").item(0).getTextContent();
        String senNfc = element.getElementsByTagName("senNfc").item(0).getTextContent();
        String sigInt = element.getElementsByTagName("sigInt").item(0).getTextContent();
        String snfNfc = element.getElementsByTagName("snfNfc").item(0).getTextContent();
        String tnsNfv = element.getElementsByTagName("tnsNfv").item(0).getTextContent();
        String tnsPed = element.getElementsByTagName("tnsPed").item(0).getTextContent();

        return new ParamsPDV(codCli, codDep, codInt, codSnf, codTpr, ideCsc, logNfc, numCsc, senNfc, sigInt, snfNfc, tnsNfv, tnsPed);
    }
}
