package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.InvalidTokenException;
import com.br.datasig.datasigpdvapi.util.ResourceNotFoundException;
import com.br.datasig.datasigpdvapi.util.WebServiceRuntimeException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

public class DataSIGController {

    @ExceptionHandler({InvalidTokenException.class})
    public String invalidTokenError() {
        return getJsonMessage("Token inválido.");
    }

    @ExceptionHandler({WebServiceRuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String webServiceRuntimeException(WebServiceRuntimeException ex) {
        return getJsonMessage(ex.getMessage());
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String resourceNotFoundException(ResourceNotFoundException ex) {
        return getJsonMessage(ex.getMessage());
    }

    private String getJsonMessage(String errorMessage) {
        return new JSONObject().put("message", errorMessage).toString();
    }

    protected boolean isTokenValid(String token) {
        return TokensManager.getInstance().isTokenValid(token);
    }
}
