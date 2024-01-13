package com.br.datasig.datasigpdvapi.service;


import com.br.datasig.datasigpdvapi.soap.SOAPClient;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@Component
public class WebServiceRequestsService {
    @Autowired
    private SOAPClient soapClient;

    public String performLogin(String user, String pswd) throws IOException {
        HashMap<String, String> emptyParams = new HashMap<>();
        String response = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", user, pswd, "0", emptyParams);

        if(response.contains("Credenciais inválidas"))
            return "Credenciais inválidas";
        else {
            Date currentDateTime = Calendar.getInstance().getTime();
            String hash = DigestUtils.sha256Hex(user + pswd + currentDateTime);
            TokensManager.getInstance().addToken(hash, user, pswd);

            return hash;
        }
    }
}

