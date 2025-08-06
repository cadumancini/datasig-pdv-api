package com.br.datasig.datasigpdvapi.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class HttpRequestClient {
    private final String url;

    public HttpRequestClient(String url) {
        this.url = url;
    }

    public HttpResponse getRequest(String suffix) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpRequest = new HttpGet(url + suffix);
        return client.execute(httpRequest);
    }
}
