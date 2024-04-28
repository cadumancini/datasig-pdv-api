package com.br.datasig.datasigpdvapi.entity;

import lombok.Data;

import java.util.List;

@Data
public class ConsultaDetalhesPedido {
    private String numPed;
    private String datEmi;
    private String desRep;
    private String desCli;
    private String staPed;
    private List<ConsultaDetalhesItemPedido> itens;
}
