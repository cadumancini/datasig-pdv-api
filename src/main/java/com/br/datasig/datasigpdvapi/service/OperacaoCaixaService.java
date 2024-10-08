package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.ConsultaMovimentoCaixa;
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
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OperacaoCaixaService extends WebServiceRequestsService {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public OperacaoCaixaResultado realizarOperacaoCaixa(String token, TipoOperacaoCaixa tipoOperacao, String valorOperacao, String hisMov) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        String numCxa = getNumCxaFromToken(token);
        String numCco = getNumCcoFromToken(token);

        String cofAbr = TokensManager.getInstance().getParamsPDVFromToken(token).getCofAbr();
        String cofFec = TokensManager.getInstance().getParamsPDVFromToken(token).getCofFec();
        String cofSan = TokensManager.getInstance().getParamsPDVFromToken(token).getCofSan();
        String cxaAbr = TokensManager.getInstance().getParamsPDVFromToken(token).getCxaAbr();
        String cxaFec = TokensManager.getInstance().getParamsPDVFromToken(token).getCxaFec();
        String cxaSan = TokensManager.getInstance().getParamsPDVFromToken(token).getCxaSan();

        return switch (tipoOperacao) {
            case ABERTURA -> {
                movimentar(numCxa, cxaAbr, valorOperacao, hisMov, token);
                yield movimentar(numCco, cofAbr, valorOperacao, hisMov, token);
            }
            case SANGRIA -> {
                movimentar(numCxa, cxaSan, valorOperacao, hisMov, token);
                yield movimentar(numCco, cofSan, valorOperacao, hisMov, token);
            }
            case FECHAMENTO -> {
                movimentar(numCxa, cxaFec, valorOperacao, hisMov, token);
                yield movimentar(numCco, cofFec, valorOperacao, hisMov, token);
            }
        };
    }

    private String getNumCxaFromToken(String token) {
        return TokensManager.getInstance().getCaixaByToken(token).getNumCxa();
    }

    private String getNumCcoFromToken(String token) {
        return TokensManager.getInstance().getCaixaByToken(token).getNumCco();
    }

    private OperacaoCaixaResultado movimentar(String numCco, String codTns, String vlrMov, String hisMov, String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
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

    public List<ConsultaMovimentoCaixa> getMovimentosCaixa(String token, String datIni, String datFim) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        String numCxa = TokensManager.getInstance().getCaixaByToken(token).getNumCxa();
        HashMap<String, Object> params = prepareBaseParams(codEmp, codFil);
        addParamsForConsultaMovtosCaixa(params, datIni, datFim, numCxa);

        String xml = soapClient.requestFromSeniorWS("PDV_DS_ConsultaMovimentoTesouraria", "Consultar", token, "0", params, false);
        XmlUtils.validateXmlResponse(xml);

        return getMovimentosCaixa(xml);
    }

    private void addParamsForConsultaMovtosCaixa(HashMap<String, Object> params, String datIni, String datFim, String numCxa) {
        params.put("datIni", datIni == null ? "" : datIni);
        params.put("datFim", datFim == null ? "" : datFim);
        params.put("numCxa", numCxa);
    }

    private List<ConsultaMovimentoCaixa> getMovimentosCaixa(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<ConsultaMovimentoCaixa> movtos = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "movimento");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                movtos.add(ConsultaMovimentoCaixa.fromXml(nNode));
            }
        }
        return movtos.stream().filter(movto -> !movto.getSeqMov().equals("0")).collect(Collectors.toList());
    }
}
