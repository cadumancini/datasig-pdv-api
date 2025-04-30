package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Caixa {
    private String codIp;
    private String codPDV;
    private String numCco;
    private String numCxa;
}
