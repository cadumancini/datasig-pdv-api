package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.soap.SOAPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class WebServiceRequestsService {

    @Autowired
    protected SOAPClient soapClient;

    protected HashMap<String, String> prepareBaseParams(String codEmp, String codFil) {
        HashMap<String, String> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        return params;
    }
}

