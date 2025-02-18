package com.br.datasig.datasigpdvapi.token;

import com.br.datasig.datasigpdvapi.entity.Caixa;
import com.br.datasig.datasigpdvapi.entity.ParamsImpressao;
import com.br.datasig.datasigpdvapi.entity.ParamsPDV;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class TokensManager {
    private static TokensManager instance = null;
    private final List<Token> validTokens;

    private TokensManager() {
        validTokens = new ArrayList<>();
    }

    public static TokensManager getInstance() {
        if(instance == null)
            instance = new TokensManager();

        return instance;
    }

    public void addToken(String tokenValue, String nomUsu, String senUsu, String codEmp, String codFil, ParamsPDV paramsPDV, ParamsImpressao paramsImpressao) {
        validTokens.add(new Token(tokenValue, nomUsu, senUsu, codEmp, codFil, paramsPDV, paramsImpressao));
    }

    public void removeInvalidTokens() {
        validTokens.removeIf(token -> !token.isValid());
    }

    public void removeToken(String tokenValue) {
        validTokens.removeIf(token -> token.getValue().equals(tokenValue));
    }

    public boolean isTokenValid(String token) {
        return validTokens.stream().anyMatch(o -> o.getValue().equals(token));
    }

    public String getUserNameFromToken(String tokenValue) {
        Token token = getTokenByValue(tokenValue);
        return token != null ? token.getUserName() : "";
    }

    public String getPasswordFromToken(String tokenValue) {
        Token token = getTokenByValue(tokenValue);
        return token != null ? token.getPassword() : "";
    }

    public String getCodEmpFromToken(String tokenValue) {
        Token token = getTokenByValue(tokenValue);
        return token != null ? token.getCodEmp() : "";
    }

    public String getCodFilFromToken(String tokenValue) {
        Token token = getTokenByValue(tokenValue);
        return token != null ? token.getCodFil() : "";
    }

    public ParamsPDV getParamsPDVFromToken(String tokenValue) {
        Token token = getTokenByValue(tokenValue);
        return token != null ? token.getParamsPDV() : null;
    }

    public Token getTokenByValue(String tokenValue) {
        return validTokens.stream().filter(token -> token.getValue().equals(tokenValue)).findFirst().orElse(null);
    }

    public Caixa getCaixaByToken(String tokenValue) {
        String logSis = getUserNameFromToken(tokenValue);
        Token token = getTokenByValue(tokenValue);
        Optional<Caixa> caixa = token.getParamsPDV().getCaixas().stream().filter(conta -> conta.getLogSis().equals(logSis)).findFirst();
        return caixa.orElse(null);
    }
}
