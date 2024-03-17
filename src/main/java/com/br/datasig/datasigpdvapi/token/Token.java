package com.br.datasig.datasigpdvapi.token;

import com.br.datasig.datasigpdvapi.entity.ParamsPDV;

import java.util.Calendar;

public class Token {
    private final String value;
    private final String nomUsu;
    private final String senUsu;
    private final String codEmp;
    private final String codFil;
    private final boolean usaTEF;
    private final ParamsPDV paramsPDV;
    private final long createdAt;
    private boolean valid;

    public Token (String value, String nomUsu, String senUsu, String codEmp, String codFil, boolean usaTEF, ParamsPDV paramsPDV) {
        this.value = value;
        this.nomUsu = nomUsu;
        this.senUsu = senUsu;
        this.codEmp = codEmp;
        this.codFil = codFil;
        this.usaTEF = usaTEF;
        this.paramsPDV = paramsPDV;
        this.createdAt = Calendar.getInstance().getTimeInMillis();
        this.valid = true;
    }

    public String getValue() {
        return value;
    }

    public String getUserName() {
        return nomUsu;
    }

    public String getPassword() {
        return senUsu;
    }

    public String getCodEmp() {
        return codEmp;
    }

    public String getCodFil() {
        return codFil;
    }

    public boolean isUsaTEF() {
        return usaTEF;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void invalidateToken() {
        this.valid = false;
    }

    public boolean isValid() {
        return this.valid;
    }

    public ParamsPDV getParamsPDV() {
        return this.paramsPDV;
    }
}
