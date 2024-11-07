package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.concurrent.TimeUnit.*;

@Data
@AllArgsConstructor
public class ConsultaNotaFiscal {
    private String codCli;
    private String codRep;
    private String codEmp;
    private String codFil;
    private String codSnf;
    private String numNfv;
    private int numNfvInt;
    private String datEmi;
    private String horEmi;
    private String sitNfv;
    private String desSitNfv;
    private String sitDoe;
    private String desSitDoe;
    private boolean cancelavel;
    private boolean inutilizavel;
    private int qtdFat;
    private Double vlrLiq;
    private String nomRep;
    private String nomCli;

    public static ConsultaNotaFiscal fromXml(Node node) throws ParseException {
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
        String sitDoe = element.getElementsByTagName("sitDoe").item(0).getTextContent();
        String nomRep = element.getElementsByTagName("nomRep").item(0).getTextContent();
        String nomCli = element.getElementsByTagName("nomCli").item(0).getTextContent();
        int qtdFat = Integer.parseInt(element.getElementsByTagName("qtdFat").item(0).getTextContent());
        Double vlrLiq = Double.parseDouble(element.getElementsByTagName("vlrLiq").item(0).getTextContent()
                .replace(",", "."));
        String desSitNfv = getDesSitNfv(sitNfv);
        String desSitDoe = getDesSitDoe(sitDoe);
        return new ConsultaNotaFiscal(codCli, codRep, codEmp, codFil, codSnf, numNfv, Integer.parseInt(numNfv), datEmi,
                horEmi, sitNfv, desSitNfv, sitDoe, desSitDoe, isCancelavel(datEmi, horEmi, sitDoe), isInutilizavel(sitDoe),
                qtdFat, vlrLiq, nomRep, nomCli);
    }

    private static String getDesSitNfv(String sitNfv) {
        return switch (Integer.parseInt(sitNfv)) {
            case 1 -> "Digitada";
            case 2 -> "Fechada";
            case 3 -> "Cancelada";
            case 4 -> "Documento Fiscal Emitido (saída)";
            case 5 -> "Aguardando Fechamento (pós-saída)";
            default -> "";
        };
    }

    private static String getDesSitDoe(String sitDoe) {
        return switch (Integer.parseInt(sitDoe)) {
            case 1 -> "Não Enviada";
            case 2 -> "Enviada";
            case 3 -> "Autorizada";
            case 4 -> "Rejeitada";
            case 5 -> "Denegada";
            case 6 -> "Solicitado Inutilização";
            case 7 -> "Solicitado Cancelamento";
            case 8 -> "Inutilizada";
            case 9 -> "Cancelada";
            case 10 -> "Erro Geração";
            case 11 -> "Erro Solicitação Cancelamento";
            case 12 -> "Erro Solicitação Inutilização";
            case 13 -> "Pendente de Cancelamento";
            case 14 -> "Solicitado Encerramento (MDF-e)";
            case 15 -> "Encerrado (MDF-e)";
            case 16 -> "Autorizado em Contingência (utilizado apenas para retorno WebService)";
            case 17 -> "Evento registrado (evento pror. suspensão ICMS - utilizado apenas Web Service)";
            case 18 -> "Deferido parcial (evento pror. suspensão ICMS - utilizado apenas no Web Service)";
            case 19 -> "Indeferido (evento prorrogação suspensão ICMS - utilizado apenas no Web Service)";
            default -> "";
        };
    }

    private static boolean isCancelavel(String datEmi, String horEmi, String sitDoe) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date dateDatEmi = formatter.parse(datEmi + " " + horEmi);
        Date now = new Date();
        long difference = now.getTime() - dateDatEmi.getTime();
        long maxDuration = MILLISECONDS.convert(30, MINUTES);
        return (difference <= maxDuration && Integer.parseInt(sitDoe) == 3);
    }

    private static boolean isInutilizavel(String sitDoe) {
        return (Integer.parseInt(sitDoe) == 4 || Integer.parseInt(sitDoe) == 10);
    }
}
