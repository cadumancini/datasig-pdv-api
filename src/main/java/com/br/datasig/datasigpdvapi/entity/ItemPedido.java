package com.br.datasig.datasigpdvapi.entity;

import lombok.Data;

@Data
public class ItemPedido {
    private String codPro;
    private String codDer;
    private String codTpr;
    private String qtdPed;
    private String vlrTot;
}
