package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.token.TokensManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class SIDService {
    private final String sidUrl;
    private final String processBaixaOP = "970"; //TODO: colocar numeros de regra em application.properties

    public SIDService(Environment env) {
        String webServicesUrl = env.getProperty("webservices.url"); //TODO: será o mesmo endereco dos WSs?
        sidUrl = String.format("http://%ssapiensweb/conector?SIS=CO&LOGIN=SID&ACAO=EXESENHA", webServicesUrl);
    }

//    TODO: implementar funcoes de acordo com a seguinte:
//    public String runBaixaOP(String token, String aCodBar, String aIntExt, String aRemRet, String aCodFor) throws IOException {
//        String user = TokensManager.getInstance().getUserNameFromToken(token);
//        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
//
//        String url = String.format("%s&NOMUSU=%s&SENUSU=%s&PROXACAO=SID.Srv.Regra&NumReg=%s&aCodBar=%s&aIntExt=%s&aRemRet=%s&aCodFor=%s",
//                sidUrl,  URLEncoder.encode(user, StandardCharsets.UTF_8),  URLEncoder.encode(pswd, StandardCharsets.UTF_8), processBaixaOP,
//                aCodBar, aIntExt, aRemRet, aCodFor);
//        return getRequest(url);
//    }

    private String getRequest(String url) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpRequest = new HttpGet(url);
        HttpResponse httpResponse = client.execute(httpRequest);
        return EntityUtils.toString(httpResponse.getEntity());
    }
}

