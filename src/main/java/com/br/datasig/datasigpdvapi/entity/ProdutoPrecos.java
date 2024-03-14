package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProdutoPrecos {
    private String codPro;
    private String codDer;
    private String codBar;
    private String codTpr;
    private String datIni;
    private String desPro;
    private List<FaixaPreco> faixasPreco;
}
