package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.OperacaoCaixaResultado;
import com.br.datasig.datasigpdvapi.entity.TipoOperacaoCaixa;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;

//Exemplo fechamento:
//<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://services.senior.com.br">
//  <soapenv:Body>
//    <ser:GerarLancamentos>
//      <user>teste</user>
//      <password>teste</password>
//      <encryption>0</encryption>
//      <parameters>
//        <lancamentos>
//          <codEmp>1</codEmp>
//          <codFil>3</codFil>
//          <numCco>PDV_1</numCco>
//          <datMov>17/07/2024</datMov>
//          <codTns>90674</codTns>
//          <vlrMov>850,00</vlrMov>
//          <sitMcc>A</sitMcc>
//          <hisMov>FECHAMENTO DE CAIXA - PDV_1</hisMov>
//        </lancamentos>
//        <indTec>N</indTec>
//        <sigInt>SELLPOINT</sigInt>
//      </parameters>
//    </ser:GerarLancamentos>
//  </soapenv:Body>
//</soapenv:Envelope>
//
//Exemplo entrada cofre (fechamento):
//<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://services.senior.com.br">
//  <soapenv:Body>
//    <ser:GerarLancamentos>
//      <user>teste</user>
//      <password>teste</password>
//      <encryption>0</encryption>
//      <parameters>
//        <lancamentos>
//          <codEmp>1</codEmp>
//          <codFil>3</codFil>
//          <numCco>CF01</numCco>
//          <datMov>17/07/2024</datMov>
//          <codTns>90678</codTns>
//          <vlrMov>850,00</vlrMov>
//          <sitMcc>A</sitMcc>
//          <hisMov>FECHAMENTO DE CAIXA - PDV_1</hisMov>
//        </lancamentos>
//        <indTec>N</indTec>
//        <sigInt>SELLPOINT</sigInt>
//      </parameters>
//    </ser:GerarLancamentos>
//  </soapenv:Body>
//</soapenv:Envelope>

@Component
public class OperacaoCaixaService extends WebServiceRequestsService {
    public OperacaoCaixaResultado realizarOperacaoCaixa(String token, TipoOperacaoCaixa tipoOperacao, String valorOperacao) {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, Object> params = prepareBaseParams(codEmp, codFil);

//        TODO: continuar implementacao aqui
        return null;
    }
}
