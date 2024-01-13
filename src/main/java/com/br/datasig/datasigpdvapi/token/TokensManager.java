package com.br.datasig.datasigpdvapi.token;

import java.util.ArrayList;
import java.util.List;

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

    public List<Token> getValidTokens() {
        return validTokens;
    }

    public void addToken(String tokenValue, String nomUsu, String senUsu) {
        validTokens.add(new Token(tokenValue, nomUsu, senUsu));
    }

    public void removeInvalidTokens() {
        validTokens.removeIf(token -> !token.isValid());
    }

    public boolean isTokenValid(String token) {
        return validTokens.stream().anyMatch(o -> o.getValue().equals(token));
    }

    public String getUserNameFromToken(String tokenValue) {
        for (Token token : validTokens) {
            if(token.getValue().equals(tokenValue) && token.isValid())
                return token.getUserName();
        }
        return "";
    }

    public String getPasswordFromToken(String tokenValue) {
        for (Token token : validTokens) {
            if(token.getValue().equals(tokenValue) && token.isValid())
                return token.getPassword();
        }
        return "";
    }
}
