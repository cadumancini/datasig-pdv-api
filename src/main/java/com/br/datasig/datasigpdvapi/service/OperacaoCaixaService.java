package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.OperacaoCaixaResultado;
import com.br.datasig.datasigpdvapi.entity.TipoOperacaoCaixa;
import com.br.datasig.datasigpdvapi.exceptions.CashOperationException;
import com.br.datasig.datasigpdvapi.exceptions.WebServiceRuntimeException;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class OperacaoCaixaService extends WebServiceRequestsService {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public OperacaoCaixaResultado realizarOperacaoCaixa(String token, TipoOperacaoCaixa tipoOperacao, String valorOperacao) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        String numCxa = getNumCxaFromToken(token);
        String numCco = getNumCcoFromToken(token);
        String hisMov = tipoOperacao.name() + " DE CAIXA - " + numCxa;
        return switch (tipoOperacao) {
            case ABERTURA -> {
                movimentar(numCxa, "90675", valorOperacao, hisMov, token);
                yield movimentar(numCco, "90677", valorOperacao, hisMov, token);
            }
            case SANGRIA -> {
                movimentar(numCxa, "90676", valorOperacao, hisMov, token);
                yield movimentar(numCco, "90679", valorOperacao, hisMov, token);
            }
            case FECHAMENTO -> {
                movimentar(numCxa, "90674", valorOperacao, hisMov, token);
                yield movimentar(numCco, "90678", valorOperacao, hisMov, token);
            }
        };
    }

    private String getNumCxaFromToken(String token) {
        return TokensManager.getInstance().getCaixaByToken(token).getNumCxa();
    }

    private String getNumCcoFromToken(String token) {
        return TokensManager.getInstance().getCaixaByToken(token).getNumCco();
    }

    private OperacaoCaixaResultado movimentar(String numCco, String codTns, String vlrMov, String hisMov, String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        HashMap<String, Object> params = new HashMap<>();
        HashMap<String, Object> lancamento = new HashMap<>();
        lancamento.put("codEmp", TokensManager.getInstance().getCodEmpFromToken(token));
        lancamento.put("codFil", TokensManager.getInstance().getCodFilFromToken(token));
        lancamento.put("numCco", numCco);
        lancamento.put("datMov", dateFormat.format(new Date()));
        lancamento.put("codTns", codTns);
        lancamento.put("vlrMov", vlrMov);
        lancamento.put("sitMcc", "A");
        lancamento.put("hisMov", hisMov);
        params.put("lancamentos", lancamento);
        params.put("indTec", "N");
        params.put("sigInt", TokensManager.getInstance().getParamsPDVFromToken(token).getSigInt());

        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_mfi_tes_lancamentos", "GerarLancamentos", token, "0", params, false);
        XmlUtils.validateXmlResponse(xml);
        OperacaoCaixaResultado operacaoCaixaResultado = getOperacaoCaixaResultado(xml);
        validateRetornoOperacaoCaixa(operacaoCaixaResultado);
        return operacaoCaixaResultado;
    }

    private void validateRetornoOperacaoCaixa(OperacaoCaixaResultado resultado) {
        if(!resultado.getResultado().equals("OK")) {
            throw new CashOperationException(resultado.getResultado());
        }
    }

    private OperacaoCaixaResultado getOperacaoCaixaResultado(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "resultado");
        Node nNode = nList.item(0);
        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            return OperacaoCaixaResultado.fromXml(nNode);
        }
        throw new WebServiceRuntimeException("Erro ao converter retorno da operação de caixa");
    }
}
