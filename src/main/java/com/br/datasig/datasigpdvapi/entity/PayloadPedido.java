package com.br.datasig.datasigpdvapi.entity;

import lombok.Data;

import java.util.List;

@Data
public class PayloadPedido {
    private String codEmp;
    private String codFil;
    private String codCli;
    private String codCpg;
    private String codFpg;
    private String desFpg;
    private String tipFpg;
    private String codOpe;
    private String codRep;
    private String banOpe;
    private String catTef;
    private String numPed;
    private String nsuTef;
    private String tipInt;
    private String tipPar;
    private int qtdPar;
    private Double vlrTot;
    private Double vlrDar;
    private List<PayloadItemPedido> itens;
    private List<Parcela> parcelas;
    private boolean fechar;
}
