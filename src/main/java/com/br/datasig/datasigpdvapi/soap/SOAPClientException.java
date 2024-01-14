package com.br.datasig.datasigpdvapi.soap;

import java.io.IOException;

public class SOAPClientException extends IOException {
    public SOAPClientException (String message) {
        super(message);
    }
}
