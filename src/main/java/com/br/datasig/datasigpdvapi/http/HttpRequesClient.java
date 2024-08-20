package com.br.datasig.datasigpdvapi.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class HttpRequesClient {
    private final String url;

    public HttpRequesClient(String url) {
        this.url = url;
    }

    public HttpResponse getRequest(String suffix) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpRequest = new HttpGet(url + suffix);
        return client.execute(httpRequest);
    }
}
