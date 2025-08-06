package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Data
@AllArgsConstructor
public class ConsultaPedido implements PedidoConsultavel {
    private String codCli;
    private String codCpg;
    private String codEmp;
    private String codFil;
    private String codFpg;
    private String codRep;
    private String codTns;
    private String datEmi;
    private String numPed;
    private String sitPed;
    private String vlrDar;
    private String staPed;
    private String tipPed;

    public static ConsultaPedido fromXml(Node node, String tnsOrc) {
        Element element = (Element) node;
        String codCli = element.getElementsByTagName("codCli").item(0).getTextContent();
        String codCpg = element.getElementsByTagName("codCpg").item(0).getTextContent();
        String codEmp = element.getElementsByTagName("codEmp").item(0).getTextContent();
        String codFil = element.getElementsByTagName("codFil").item(0).getTextContent();
        String codFpg = element.getElementsByTagName("codFpg").item(0).getTextContent();
        String codRep = element.getElementsByTagName("codRep").item(0).getTextContent();
        String codTns = element.getElementsByTagName("codTns").item(0).getTextContent();
        String datEmi = element.getElementsByTagName("datEmi").item(0).getTextContent();
        String numPed = element.getElementsByTagName("numPed").item(0).getTextContent();
        String sitPed = element.getElementsByTagName("sitPed").item(0).getTextContent();
        String vlrDar = element.getElementsByTagName("vlrDar").item(0).getTextContent();
        String staPed = defineStaPed(sitPed);
        String tipPed = defineTipPed(codTns, tnsOrc);

        return new ConsultaPedido(codCli, codCpg, codEmp, codFil, codFpg, codRep, codTns, datEmi, numPed, sitPed, vlrDar, staPed, tipPed);
    }

    private static String defineTipPed(String codTns, String tnsOrc) {
        return codTns.equals(tnsOrc) ? "ORÇAMENTO" : "NORMAL";
    }

    private static String defineStaPed(String sitPed) {
        return switch (sitPed) {
            case "1" -> "FECHADO";
            case "5" -> "CANCELADO";
            case "9" -> "ABERTO";
            default -> "";
        };
    }

    @Override
    public int getNumPedInt() {
        return Integer.parseInt(numPed);
    }
}
