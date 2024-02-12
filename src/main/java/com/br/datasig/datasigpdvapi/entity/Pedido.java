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
    private String codRep;
    private List<ItemPedido> itens;
}
