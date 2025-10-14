package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConsultaItemPedido {
    private String codDer;
    private String codPro;
    private String codDep;
    private String codTpr;
    private String cplIpd;
    private String preUni;
    private String qtdAbe;
    private String qtdCan;
    private String qtdFat;
    private String qtdPed;
    private String seqIpd;
    private String sitIpd;
    private String obsIpd;
    private String tipDsc;
    private String vlrDsc;
    private String perDsc;
    private String perAcr;
}