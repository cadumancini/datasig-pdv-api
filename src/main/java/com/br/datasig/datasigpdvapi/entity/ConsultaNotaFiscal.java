package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

@Data
@AllArgsConstructor
public class ConsultaNotaFiscal {
    private String codCli;
    private String codRep;
    private String codEmp;
    private String codFil;
    private String codSnf;
    private String numNfv;
    private String datEmi;
    private String horEmi;
    private String sitNfv;
    private String desSitNfv;
    private String sitDoe;
    private String desSitDoe;

    public static ConsultaNotaFiscal fromXml(Node node) {
        Element element = (Element) node;
        String codCli = element.getElementsByTagName("codCli").item(0).getTextContent();
        String codRep = element.getElementsByTagName("codRep").item(0).getTextContent();
        String codEmp = element.getElementsByTagName("codEmp").item(0).getTextContent();
        String codFil = element.getElementsByTagName("codFil").item(0).getTextContent();
        String codSnf = element.getElementsByTagName("codSnf").item(0).getTextContent();
        String numNfv = element.getElementsByTagName("numNfv").item(0).getTextContent();
        String datEmi = element.getElementsByTagName("datEmi").item(0).getTextContent();
        String horEmi = element.getElementsByTagName("horEmi").item(0).getTextContent();
        String sitNfv = element.getElementsByTagName("sitNfv").item(0).getTextContent();
        String desSitNfv = getDesSitNfv(sitNfv);
        String sitDoe = getSitDoe(element);
        String desSitDoe = getDesSitDoe(sitDoe);
        return new ConsultaNotaFiscal(codCli, codRep, codEmp, codFil, codSnf, numNfv, datEmi, horEmi, sitNfv, desSitNfv, sitDoe, desSitDoe);
    }

    private static String getDesSitNfv(String sitNfv) {
        return switch (sitNfv) {
            case "1" -> "Digitada";
            case "2" -> "Fechada";
            case "3" -> "Cancelada";
            case "4" -> "Documento Fiscal Emitido (saída)";
            case "5" -> "Aguardando Fechamento (pós-saída)";
            default -> "";
        };
    }

    private static String getSitDoe(Element element) {
        NodeList infoEletronica = element.getElementsByTagName("informacaoEletronica");
        for (int i = 0; i < infoEletronica.getLength(); i++) {
            Node nNodeInfo = infoEletronica.item(i);
            if (nNodeInfo.getNodeType() == Node.ELEMENT_NODE) {
                Element elInfoEletronica = (Element) nNodeInfo;
                return elInfoEletronica.getElementsByTagName("sitDoe").item(0).getTextContent();
            }
        }
        return "";
    }

    private static String getDesSitDoe(String sitDoe) {
        return switch (sitDoe) {
            case "1" -> "Não Enviada";
            case "2" -> "Enviada";
            case "3" -> "Autorizada";
            case "4" -> "Rejeitada";
            case "5" -> "Denegada";
            case "6" -> "Solicitado Inutilização";
            case "7" -> "Solicitado Cancelamento";
            case "8" -> "Inutilizada";
            case "9" -> "Cancelada";
            case "10" -> "Erro Geração";
            case "11" -> "Erro Solicitação Cancelamento";
            case "12" -> "Erro Solicitação Inutilização";
            case "13" -> "Pendente de Cancelamento";
            case "14" -> "Solicitado Encerramento (MDF-e)";
            case "15" -> "Encerrado (MDF-e)";
            case "16" -> "Autorizado em Contingência (utilizado apenas para retorno WebService)";
            case "17" -> "Evento registrado (evento pror. suspensão ICMS - utilizado apenas Web Service)";
            case "18" -> "Deferido parcial (evento pror. suspensão ICMS - utilizado apenas no Web Service)";
            case "19" -> "Indeferido (evento prorrogação suspensão ICMS - utilizado apenas no Web Service)";
            default -> "";
        };
    }
}
