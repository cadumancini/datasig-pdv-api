package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private final String nomUsu;
    private final String codEmp;
    private final String codFil;
    private final boolean usaTEF;
    private final ParamsPDV parametrosPDV;
}
