package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RetornoItemPedido {
    private String cnpjFilial;
    private String codEmp;
    private String codFil;
    private String numPed;
    private String seqIpd;
    private String retorno;
    private String sitIpd;
}
