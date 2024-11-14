package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

@Data
@AllArgsConstructor
public class ConsultaTitulo {
    private String codCli;
    private String codEmp;
    private String codFil;
    private String codFpg;
    private String codRep;
    private String codSnf;
    private String codTpt;
    private String datEmi;
    private String desFpg;
    private String nomCli;
    private String nomRep;
    private String numNfv;
    private int numNfvInt;
    private String numTit;
    private String sitDoe;
    private String sitNfv;
    private String sitTit;
    private String desSitNfv;
    private String desSitDoe;
    private String desSitTit;
    private Double vlrAbe;
    private Double vlrOri;

    public static ConsultaTitulo fromXml(Node node) throws ParseException {
        Element element = (Element) node;
        String codCli = element.getElementsByTagName("codCli").item(0).getTextContent();
        String codEmp = element.getElementsByTagName("codEmp").item(0).getTextContent();
        String codFil = element.getElementsByTagName("codFil").item(0).getTextContent();
        String codFpg = element.getElementsByTagName("codFpg").item(0).getTextContent();
        String codRep = element.getElementsByTagName("codRep").item(0).getTextContent();
        String codSnf = element.getElementsByTagName("codSnf").item(0).getTextContent();
        String codTpt = element.getElementsByTagName("codTpt").item(0).getTextContent();
        String datEmi = element.getElementsByTagName("datEmi").item(0).getTextContent();
        String desFpg = element.getElementsByTagName("desFpg").item(0).getTextContent();
        String nomCli = element.getElementsByTagName("nomCli").item(0).getTextContent();
        String nomRep = element.getElementsByTagName("nomRep").item(0).getTextContent();
        String numNfv = element.getElementsByTagName("numNfv").item(0).getTextContent();
        String numTit = element.getElementsByTagName("numTit").item(0).getTextContent();
        String sitDoe = element.getElementsByTagName("sitDoe").item(0).getTextContent();
        String sitNfv = element.getElementsByTagName("sitNfv").item(0).getTextContent();
        String sitTit = element.getElementsByTagName("sitTit").item(0).getTextContent();
        String desSitNfv = getDesSitNfv(sitNfv);
        String desSitDoe = getDesSitDoe(sitDoe);
        String desSitTit = getDesSitTit(sitTit);
        Double vlrAbe = Double.parseDouble(element.getElementsByTagName("vlrAbe").item(0).getTextContent()
                .replace(",", "."));
        Double vlrOri = Double.parseDouble(element.getElementsByTagName("vlrOri").item(0).getTextContent()
                .replace(",", "."));
        return new ConsultaTitulo(codCli, codEmp, codFil, codFpg, codRep, codSnf, codTpt, datEmi, desFpg, nomCli,
                nomRep, numNfv, Integer.parseInt(numNfv), numTit, sitDoe, sitNfv, sitTit, desSitNfv, desSitDoe,
                desSitTit, vlrAbe, vlrOri);
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

    private static String getDesSitTit(String sitDoe) {
        return switch (sitDoe) {
            case "AO" -> "Aberto ao Órgão de Proteção ao Crédito";
            case "AN" -> "Aberto Negociação";
            case "AA" -> "Aberto Advogado";
            case "AB" -> "Aberto Normal";
            case "AC" -> "Aberto Cartório";
            case "AE" -> "Aberto Encontro de Contas";
            case "AI" -> "Aberto Impostos";
            case "AJ" -> "Aberto Retorno Jurídico";
            case "AP" -> "Aberto Protestado";
            case "AR" -> "Aberto Representante";
            case "AS" -> "Aberto Suspenso";
            case "AV" -> "Aberto Gestão de Pessoas";
            case "AX" -> "Aberto Externo";
            case "CA" -> "Cancelado";
            case "CE" -> "Aberto CE (Preparação Cobrança Escritural)";
            case "CO" -> "Aberto Cobrança";
            case "LQ" -> "Liquidado Normal";
            case "LC" -> "Liquidado Cartório";
            case "LI" -> "Liquidado Impostos";
            case "LM" -> "Liquidado Compensado";
            case "LO" -> "Liquidado Cobrança";
            case "LP" -> "Liquidado Protestado";
            case "LS" -> "Liquidado Substituído";
            case "LV" -> "Liquidado Gestão de Pessoas";
            case "LX" -> "Liquidado Externo";
            case "PE" -> "Aberto PE (Pagamento Eletrônico)";
            default -> "";
        };
    }
}
