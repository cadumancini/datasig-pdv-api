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
    private List<RamoAtividade> ramos;
    private String codDep;
    private String codEmp;
    private String codFil;
    private String nomEmp;
    private String nomFil;
    private String nomUsu;
    private String codIp;
}
