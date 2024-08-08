package com.br.datasig.datasigpdvapi.soap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@SpringBootTest
class SOAPClientTest {
    @Autowired
    private SOAPClient uut;

    @Test
    void prepareXmlBody_emptyParams() throws IOException {
        String expectedParams = getDocumentContent("emptyParams.xml");

        String service = "SERVICE";
        String user = "USER";
        String pswd = "PSWD";
        String encryption = "ENCRYPTION";
        Map<String, Object> params = new HashMap<>();
        String identificador = "<identificadorSistema>IDENTIFICADOR</identificadorSistema>";

        String xmlBody = uut.prepareXmlBody(service, user, pswd, encryption, params, identificador);

        assertThat(xmlBody, isSimilarTo(expectedParams).ignoreWhitespace());
    }

    @Test
    void prepareXmlBody_criarPedido() throws IOException {
        String expectedEmptyParams = getDocumentContent("criarPedido.xml");

        String service = "SERVICE";
        String user = "USER";
        String pswd = "PSWD";
        String encryption = "ENCRYPTION";
        Map<String, Object> params = prepareParamsCriarPedido();
        String identificador = "";

        String xmlBody = uut.prepareXmlBody(service, user, pswd, encryption, params, identificador);

        assertThat(xmlBody, isSimilarTo(expectedEmptyParams).ignoreWhitespace());
    }

    private Map<String, Object> prepareParamsCriarPedido() {
        Map<String, Object> params = new HashMap<>();
        params.put("converterQtdUnidadeVenda", "N");
        params.put("ignorarErrosParcela", "N");
        params.put("inserirApenasPedidoCompleto", "S");
        params.put("converterQtdUnidadeEstoque", "N");
        params.put("ignorarPedidoBloqueado", "N");
        params.put("ignorarErrosPedidos", "N");
        params.put("ignorarErrosItens", "N");

        Map<String, Object> paramsPedido = new HashMap<>();
        paramsPedido.put("tnsPro","12345");
        paramsPedido.put("codEmp","1");
        paramsPedido.put("codFil","1");
        paramsPedido.put("indPre","1");
        paramsPedido.put("opeExe","I");
        paramsPedido.put("temPar","N");
        paramsPedido.put("codCli","1");
        paramsPedido.put("acePar","N");
        paramsPedido.put("codCpg","001D");
        paramsPedido.put("usuario","<cmpUsu>USU_VLRTRO</cmpUsu><vlrUsu>0</vlrUsu>");
        paramsPedido.put("vlrDar","0");
        paramsPedido.put("codRep","35");
        paramsPedido.put("cifFob","X");

        List<HashMap<String, Object>> itens = new ArrayList<>();
        HashMap<String, Object> item1 = new HashMap<>();
        item1.put("tnsPro", "12345");
        item1.put("codDer", "01");
        item1.put("codTpr", "TPL");
        item1.put("qtdPed", "1");
        item1.put("resEst", "S");
        item1.put("codDep", "1000");
        item1.put("opeExe", "I");
        item1.put("codPro", "0202");
        item1.put("perAcr", "0,0");
        item1.put("perDsc", "0,0");
        item1.put("obsIpd", "");
        item1.put("pedPrv", "N");
        item1.put("vlrDsc", "0,0");

        HashMap<String, Object> item2 = new HashMap<>();
        item2.put("tnsPro", "12345");
        item2.put("codDer", "02");
        item2.put("codTpr", "TPL");
        item2.put("qtdPed", "1");
        item2.put("resEst", "S");
        item2.put("codDep", "1000");
        item2.put("opeExe", "I");
        item2.put("codPro", "0202");
        item2.put("perAcr", "0,0");
        item2.put("perDsc", "0,0");
        item2.put("obsIpd", "");
        item2.put("pedPrv", "N");
        item2.put("vlrDsc", "0,0");

        itens.add(item1);
        itens.add(item2);
        paramsPedido.put("produto", itens);

        params.put("pedido", paramsPedido);
        return params;
    }

    private String getDocumentContent(String path) throws IOException {
        return new String(getClass().getClassLoader().getResourceAsStream(path).readAllBytes());
    }
}
