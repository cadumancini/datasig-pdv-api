package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ConsultaPedidoDetalhes implements PedidoConsultavel {
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
    private String perDs1;
    private String vlrTro;
    private String staPed;
    private String tipPed;
    private List<ConsultaItemPedido> itens;
    private List<ConsultaParcelaPedido> parcelas;

    public static ConsultaPedidoDetalhes fromXml(Node node, String tnsOrc) {
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
        String perDs1 = element.getElementsByTagName("perDs1").item(0).getTextContent();
        String vlrTro = element.getElementsByTagName("vlrTro").item(0).getTextContent();
        String staPed = defineStaPed(sitPed);
        String tipPed = defineTipPed(codTns, tnsOrc);
        List<ConsultaItemPedido> itens = getItensPedido(element);
        List<ConsultaParcelaPedido> parcelas = getParcelasPedido(element);

        return new ConsultaPedidoDetalhes(codCli, codCpg, codEmp, codFil, codFpg, codRep, codTns, datEmi, numPed, sitPed,
                vlrDar, perDs1, vlrTro, staPed, tipPed, itens, parcelas);
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

    private static List<ConsultaItemPedido> getItensPedido(Element element) {
        List<ConsultaItemPedido> itens = new ArrayList<>();
        NodeList itensList = element.getElementsByTagName("itens");
        for (int i = 0; i < itensList.getLength(); i++) {
            Node nNodeItem = itensList.item(i);
            if (nNodeItem.getNodeType() == Node.ELEMENT_NODE) {
                Element elItem = (Element) nNodeItem;
                String codDer = elItem.getElementsByTagName("codDer").item(0).getTextContent();
                String codPro = elItem.getElementsByTagName("codPro").item(0).getTextContent();
                String codDep = elItem.getElementsByTagName("codDep").item(0).getTextContent();
                String codTpr = elItem.getElementsByTagName("codTpr").item(0).getTextContent();
                String cplIpd = elItem.getElementsByTagName("cplIpd").item(0).getTextContent();
                String preUni = elItem.getElementsByTagName("preUni").item(0).getTextContent();
                String qtdAbe = elItem.getElementsByTagName("qtdAbe").item(0).getTextContent();
                String qtdCan = elItem.getElementsByTagName("qtdCan").item(0).getTextContent();
                String qtdFat = elItem.getElementsByTagName("qtdFat").item(0).getTextContent();
                String qtdPed = elItem.getElementsByTagName("qtdPed").item(0).getTextContent();
                String seqIpd = elItem.getElementsByTagName("seqIpd").item(0).getTextContent();
                String sitIpd = elItem.getElementsByTagName("sitIpd").item(0).getTextContent();
                String obsIpd = elItem.getElementsByTagName("obsIpd").item(0).getTextContent();
                String vlrDsc = elItem.getElementsByTagName("vlrDsc").item(0).getTextContent();
                String perDsc = elItem.getElementsByTagName("perDsc").item(0).getTextContent();
                String perAcr = elItem.getElementsByTagName("perAcr").item(0).getTextContent();
                String tipDsc = getTipDsc(perDsc, vlrDsc, perAcr);

                itens.add(new ConsultaItemPedido(codDer, codPro, codDep, codTpr, cplIpd, preUni, qtdAbe, qtdCan,
                        qtdFat, qtdPed, seqIpd, sitIpd, obsIpd.trim(), tipDsc, vlrDsc, perDsc, perAcr));
            }
        }
        return itens;
    }

    private static String getTipDsc(String perDsc, String vlrDsc, String perAcr) {
        if (!perDsc.equals("0,00") || !perAcr.equals("0,00")) return "porcentagem";
        else if (!vlrDsc.equals("0,00")) return "valor";
        return "";
    }

    private static List<ConsultaParcelaPedido> getParcelasPedido(Element element) {
        List<ConsultaParcelaPedido> parcelas = new ArrayList<>();
        NodeList parcelasList = element.getElementsByTagName("parcelas");
        for (int i = 0; i < parcelasList.getLength(); i++) {
            Node nNodeItem = parcelasList.item(i);
            if (nNodeItem.getNodeType() == Node.ELEMENT_NODE) {
                Element elItem = (Element) nNodeItem;
                String banOpe = elItem.getElementsByTagName("banOpe").item(0).getTextContent();
                String catTef = elItem.getElementsByTagName("catTef").item(0).getTextContent();
                String codFpg = elItem.getElementsByTagName("codFpg").item(0).getTextContent();
                String codOpe = elItem.getElementsByTagName("codOpe").item(0).getTextContent();
                String indPag = elItem.getElementsByTagName("indPag").item(0).getTextContent();
                String seqPar = elItem.getElementsByTagName("seqPar").item(0).getTextContent();
                String tipInt = elItem.getElementsByTagName("tipInt").item(0).getTextContent();
                String vctPar = elItem.getElementsByTagName("vctPar").item(0).getTextContent();
                String vlrPar = elItem.getElementsByTagName("vlrPar").item(0).getTextContent();

                parcelas.add(new ConsultaParcelaPedido(banOpe, catTef, codFpg, codOpe, indPag, seqPar, tipInt, vctPar, vlrPar));
            }
        }
        return parcelas;
    }

    @Override
    public int getNumPedInt() {
        return Integer.parseInt(numPed);
    }
}
