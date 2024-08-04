package com.br.datasig.datasigpdvapi.soap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@SpringBootTest
class SOAPClientTest {
    @Autowired
    private SOAPClient uut;

    @Test
    void prepareXmlBody() throws IOException {
        String expectedEmptyParams = getDocumentContent("emptyParams.xml");

        String service = "SERVICE";
        String user = "USER";
        String pswd = "PSWD";
        String encryption = "ENCRYPTION";
        Map<String, Object> params = new HashMap<>();
        String identificador = "<identificadorSistema>IDENTIFICADOR</identificadorSistema>";

        String xmlBody = uut.prepareXmlBody(service, user, pswd, encryption, params, identificador);

        assertThat(xmlBody, isSimilarTo(expectedEmptyParams).ignoreWhitespace());
    }

    private String getDocumentContent(String path) throws IOException {
        return new String(getClass().getClassLoader().getResourceAsStream(path).readAllBytes());
    }
}
