package com.br.datasig.datasigpdvapi.entity;

import lombok.Data;

import java.util.List;

@Data
public class PayloadPedido {
    private String codEmp;
    private String codFil;
    private String codCli;
    private String codCpg;
    private String codRep;
    private String numPed;
    private Double vlrTot;
    private Double vlrDar;
    private List<PayloadItemPedido> itens;
    private List<PagamentoPedido> pagamentos;
    private boolean fechar;
}
