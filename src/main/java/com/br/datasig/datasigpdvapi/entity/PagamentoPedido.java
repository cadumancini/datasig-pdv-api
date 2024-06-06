package com.br.datasig.datasigpdvapi.entity;

import lombok.Data;

@Data
public class PagamentoPedido {
    private FormaPagamento forma;
    private CondicaoPagamento condicao;
    private Double valorPago;
    private Double valorDesconto;
    private Double valorTotalPago;
    private String banOpe;
    private String catTef;
    private String nsuTef;
}
