package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConsultaParcelaPedido {
    private String banOpe;
    private String catTef;
    private String codFpg;
    private String codOpe;
    private String indPag;
    private String seqPar;
    private String tipInt;
    private String vctPar;
    private String vlrPar;
}