package com.br.datasig.datasigpdvapi.entity;

import lombok.Data;

@Data
public class PayloadItemPedido {
    private String codPro;
    private String codDer;
    private String codTpr;
    private String qtdPed;
    private String seqIpd;
    private String vlrTot;
    private String obsIpd;
    private String vlrDsc;
    private String perDsc;
    private boolean excluir;
}
