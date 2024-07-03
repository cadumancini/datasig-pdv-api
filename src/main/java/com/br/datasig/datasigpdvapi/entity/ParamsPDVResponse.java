package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ParamsPDVResponse {
    private String codTpr;
    private String dscTot;
    private List<Deposito> depositos;
    private String codDep;
    private String codEmp;
    private String codFil;
    private String nomUsu;
}
