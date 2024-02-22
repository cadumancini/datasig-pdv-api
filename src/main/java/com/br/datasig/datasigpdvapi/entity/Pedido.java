package com.br.datasig.datasigpdvapi.entity;

import lombok.Data;

import java.util.List;

@Data
public class Pedido {
    private String codEmp;
    private String codFil;
    private String codCli;
    private String codCpg;
    private String codFpg;
    private String desFpg;
    private String codRep;
    private String banOpe;
    private String catTef;
    private String nsuTef;
    private String cgcCre;
    private int qtdPar;
    private Double vlrTot;
    private List<ItemPedido> itens;
    private List<Parcela> parcelas;
}
