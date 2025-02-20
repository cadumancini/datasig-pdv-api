package com.br.datasig.datasigpdvapi.token;

import com.br.datasig.datasigpdvapi.entity.ParamsImpressao;
import com.br.datasig.datasigpdvapi.entity.ParamsPDV;
import lombok.Getter;

import java.util.Calendar;

public class Token {
    @Getter
    private final String value;
    private final String nomUsu;
    private final String senUsu;
    @Getter
    private final String codEmp;
    @Getter
    private final String codFil;
    @Getter
    private final ParamsPDV paramsPDV;
    @Getter
    private final ParamsImpressao paramsImpressao;
    @Getter
    private final long createdAt;
    @Getter
    private boolean valid;

    public Token (String value, String nomUsu, String senUsu, String codEmp, String codFil, ParamsPDV paramsPDV, ParamsImpressao paramsImpressao) {
        this.value = value;
        this.nomUsu = nomUsu;
        this.senUsu = senUsu;
        this.codEmp = codEmp;
        this.codFil = codFil;
        this.paramsPDV = paramsPDV;
        this.paramsImpressao = paramsImpressao;
        this.createdAt = Calendar.getInstance().getTimeInMillis();
        this.valid = true;
    }

    public String getUserName() {
        return nomUsu;
    }

    public String getPassword() {
        return senUsu;
    }

    public void invalidateToken() {
        this.valid = false;
    }

}
