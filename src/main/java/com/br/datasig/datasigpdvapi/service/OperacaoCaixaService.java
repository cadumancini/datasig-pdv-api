package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.ConsultaMovimentoCaixa;
import com.br.datasig.datasigpdvapi.entity.OperacaoCaixaResultado;
import com.br.datasig.datasigpdvapi.entity.TipoOperacaoCaixa;
import com.br.datasig.datasigpdvapi.exceptions.CashOperationException;
import com.br.datasig.datasigpdvapi.exceptions.ValueNotAllowedException;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
                validarCaixaFechado(token, cxaAbr, cxaFec);
                movimentar(numCxa, cxaAbr, valorOperacao, hisMov, token);
                yield movimentar(numCco, cofAbr, valorOperacao, hisMov, token);
            }
            case SANGRIA -> {
                baixarTitulos(token);
                validarSalAcu(valorOperacao, token);
                validarCaixaAberto(token, cxaAbr, cxaFec, cxaSan);
                movimentar(numCxa, cxaSan, valorOperacao, hisMov, token);
                yield movimentar(numCco, cofSan, valorOperacao, hisMov, token);
            }
            case FECHAMENTO -> {
                validarCaixaAberto(token, cxaAbr, cxaFec, cxaSan);
                baixarTitulos(token);
                movimentar(numCxa, cxaFec, valorOperacao, hisMov, token);
                yield movimentar(numCco, cofFec, valorOperacao, hisMov, token);
            }
        };
    }

    private void validarCaixaFechado(String token, String cxaAbr, String cxaFec) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException {
        var movtos = getMovtosLastXDays(token, 30);
        var lastValidMovtos = filtrarLancamentosPorTransacoes(movtos, List.of(cxaAbr, cxaFec));
        if (!lastValidMovtos.isEmpty()) {
            var lastMov = lastValidMovtos.get(lastValidMovtos.size() - 1);
            if (!lastMov.getCodTns().equals(cxaFec))
                throw new CashOperationException("Não existe um fechamento anterior para poder realizar uma nova abertura de caixa.");
        }
    }

    private void validarCaixaAberto(String token, String cxaAbr, String cxaFec, String cxaSan) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException {
        var movtos = getMovtosLastXDays(token, 30);
        var lastValidMovtos = filtrarLancamentosPorTransacoes(movtos, List.of(cxaAbr, cxaFec, cxaSan));
        var lastMov = validarListaDeMovtosERetornarUltimo(lastValidMovtos);
        if (lastMov.getCodTns().equals(cxaFec))
            throw new CashOperationException("O caixa não está aberto, portanto não é permitido realizar a operação.");
    }

    private ConsultaMovimentoCaixa validarListaDeMovtosERetornarUltimo(List<ConsultaMovimentoCaixa> movtos) {
        if (movtos.isEmpty())  throw new CashOperationException("Não existe movimento de caixa nos últimos 30 dias. Verifique no sistema ERP.");
        return movtos.get(movtos.size() - 1);
    }

    private List<ConsultaMovimentoCaixa> filtrarLancamentosPorTransacoes(List<ConsultaMovimentoCaixa> movtos, List<String> validTns) {
        return movtos.stream()
                .filter(mov -> validTns.contains(mov.getCodTns()))
                .toList();
    }

    private void validarSalAcu(String valorOperacao, String token) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException {
        var movtos = getMovtosLastXDays(token, 30);
        var lastMov = validarListaDeMovtosERetornarUltimo(movtos);
        var vlrMov = Double.parseDouble(valorOperacao.replace(".", "").replace(",", "."));
        if (vlrMov > lastMov.getSalAcu())
            throw new ValueNotAllowedException("O valor movimentado é maior que o saldo acumulado em caixa.");
    }

    private List<ConsultaMovimentoCaixa> getMovtosLastXDays(String token, int days) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException {
        String datIni = defineDatIni(days);
        return getMovimentosCaixa(token, datIni, null);
    }

    private String defineDatIni(int daysToSubstract) {
        LocalDate localDateSubstracted = LocalDate.now().minusDays(daysToSubstract);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return localDateSubstracted.format(formatter);
    }

    private void baixarTitulos(String token) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException {
        Map<String, Object> params = preparaParamsForBaixarTitulos(token);
        exeRegra(token, params);
    }

    private Map<String, Object> preparaParamsForBaixarTitulos(String token) {
        String numReg = TokensManager.getInstance().getParamsPDVFromToken(token).getRegBai();
        Map<String, Object> params = getBaseParams(token, numReg);
        params.put("aWsPdv", "S");
        return params;
    }

    private Map<String, Object> getBaseParams(String token, String numReg) {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);

        Map<String, Object> params = new HashMap<>();
        params.put("acao", "sid.srv.regra");
        params.put("numreg", numReg);
        params.put("aCodEmp", codEmp);
        params.put("aCodFil", codFil);
        return params;
    }

    private void exeRegra(String token, Map<String, Object> params) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        String xml = soapClient.requestFromSeniorWSSID("com_senior_g5_co_ger_sid", "Executar", token, "0", params);
        XmlUtils.validateXmlResponse(xml);
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
