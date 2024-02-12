package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.*;
import com.br.datasig.datasigpdvapi.exceptions.ResourceNotFoundException;
import com.br.datasig.datasigpdvapi.exceptions.WebServiceRuntimeException;
import com.br.datasig.datasigpdvapi.soap.SOAPClient;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class WebServiceRequestsService {
    private static final Logger logger = LoggerFactory.getLogger(WebServiceRequestsService.class);
    private final String numRegTpr;

    @Autowired
    private SOAPClient soapClient;

    public WebServiceRequestsService(Environment env) {
        numRegTpr = env.getProperty("numRegTpr");
    }

    /* Login */
    public String performLogin(String user, String pswd) throws IOException, ParserConfigurationException, SAXException, SOAPClientException {
        HashMap<String, String> emptyParams = new HashMap<>();
        logger.info("Tentativa de login para usuário {}", user);
        String response = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", user, pswd, "0", emptyParams, false);

        if(response.contains("Credenciais inválidas")) {
            logger.error("Credenciais inválidas para usuário {}", user);
            return "Credenciais inválidas";
        }
        else {
            logger.info("Login bem sucedido para usuário {}", user);
            Date currentDateTime = Calendar.getInstance().getTime();
            String hash = DigestUtils.sha256Hex(user + pswd + currentDateTime);

            ParamsEmpresa paramsEmpFil = defineCodEmpCodFil(user, pswd);
            TokensManager.getInstance().addToken(hash, user, pswd, paramsEmpFil.getCodEmp(), paramsEmpFil.getCodFil());

            return hash;
        }
    }

    private ParamsEmpresa defineCodEmpCodFil(String user, String pswd) throws IOException, ParserConfigurationException, SAXException, SOAPClientException {
        logger.info("Buscando empresa e filial ativas para usuário {}", user);
        HashMap<String, String> params = prepareParamsForEmpresaAtiva(user);
        String xml = soapClient.requestFromSeniorWS("ConsultaEmpresaAtiva", "Usuario", user, pswd, "0", params, false);

        XmlUtils.validateXmlResponse(xml);
        return getParamsEmpresaFromXml(xml);
    }

    private ParamsEmpresa getParamsEmpresaFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "result");

        if (nList.getLength() == 1) {
            return ParamsEmpresa.fromXml(nList.item(0));
        } else {
            throw new ResourceNotFoundException("Parâmetros não encontrados para o usuário");
        }
    }

    /* Representantes */
    public List<Representante> getRepresentantes(String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_representante", "ConsultarCadastro", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getRepresentantesFromXml(xml);
    }

    private List<Representante> getRepresentantesFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<Representante> representantes = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "representante");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                representantes.add(Representante.fromXml(nNode));
            }
        }
        return representantes;
    }

    public List<TabelaPreco> getTabelasPrecoPorRepresentantes(String token, String codRep) throws IOException, ParserConfigurationException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        addParamsForTabelaPreco(params, codRep);
        String xml = soapClient.requestFromSeniorWS("ConsultaTabeladePreco", "Representante", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getTabelasPrecoFromXml(xml);
    }

    private List<TabelaPreco> getTabelasPrecoFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<TabelaPreco> tabelas = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "TABELA");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                tabelas.add(TabelaPreco.fromXml(nNode));
            }
        }
        return tabelas;
    }

    /* Clientes */
    public List<Cliente> getClientes(String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_clientes", "ConsultarGeral_2", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getClientesFromXml(xml);
    }

    private List<Cliente> getClientesFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<Cliente> clientes = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "cliente");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                clientes.add(Cliente.fromXml(nNode));
            }
        }
        return clientes;
    }

    /* Produtos */
    public List<ProdutoDerivacao> getProdutos(String token) throws IOException, ParserConfigurationException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        addParamsForProdutos(params);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_produtos", "ConsultarGeral_4", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getProdutosFromXml(xml);
    }

    public String getPreco(String token, String codPro, String codDer, String codTpr, String qtdPdv) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String datIni = getCurrentDate();

        String params = prepareParamsForConsultaPrecos(codEmp, codTpr, codPro, codDer, datIni, qtdPdv);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", token, "0", params);
        XmlUtils.validateXmlResponse(xml);
        return getPriceFromXml(xml);
    }

    private String getCurrentDate() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(currentDate);
    }

    private String getPriceFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "result");
        if (nList.getLength() == 1) {
            Element element = (Element) nList.item(0);
            String preBas = element.getElementsByTagName("resultado").item(0).getTextContent();
            return preBas.replace(",", ".");
        } else {
            throw new ResourceNotFoundException("Preço não encontrado para o produto");
        }
    }

    private List<ProdutoDerivacao> getProdutosFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<ProdutoDerivacao> produtos = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "produto");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                produtos.addAll(ProdutoDerivacao.fromXml(nNode));
            }
        }
        return produtos;
    }

    /* Condicoes de Pagamento */
    public List<CondicaoPagamento> getCondicoesPagamento(String token) throws IOException, ParserConfigurationException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        addParamsForCondicao(params);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_condicaopagamento", "ConsultarGeral", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getCondicoesPagamentoFromXml(xml);
    }

    private List<CondicaoPagamento> getCondicoesPagamentoFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<CondicaoPagamento> condicoes = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "condicaoDePagamento");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                condicoes.add(CondicaoPagamento.fromXml(nNode));
            }
        }
        return condicoes;
    }

    /* Formas de Pagamento */
    public List<FormaPagamento> getFormasPagamento(String token) throws IOException, ParserConfigurationException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, String> params = prepareBaseParams(codEmp, codFil);
        addParamsForFormas(params);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_cad_formapagamento", "ConsultarGeral", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        return getFormasPagamentoFromXml(xml);
    }

    private List<FormaPagamento> getFormasPagamentoFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<FormaPagamento> formas = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "formaDePagamento");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                formas.add(FormaPagamento.fromXml(nNode));
            }
        }
        return formas;
    }

    /* Pedido */
    public RetornoPedido createPedido(String token, Pedido pedido) throws ParserConfigurationException, IOException, SAXException, SOAPClientException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        pedido.setCodEmp(codEmp);
        pedido.setCodFil(codFil);

        HashMap<String, Object> params = prepareParamsForPedido(pedido);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_pedidos", "GravarPedidos_13", token, "0", params, true);
        XmlUtils.validateXmlResponse(xml);
        return getRetornoPedidoFromXml(xml);
    }

    private HashMap<String, Object> prepareParamsForPedido(Pedido pedido) {
        HashMap<String, Object> paramsPedido = new HashMap<>();
        paramsPedido.put("converterQtdUnidadeEstoque", "N");
        paramsPedido.put("converterQtdUnidadeVenda", "N");
        paramsPedido.put("converterQtdUnidadeVenda", "N");
        paramsPedido.put("dataBuild", "");
        paramsPedido.put("flowInstanceID", "");
        paramsPedido.put("flowName", "");
        paramsPedido.put("ignorarErrosItens", "N");
        paramsPedido.put("ignorarErrosParcela", "N");
        paramsPedido.put("ignorarErrosPedidos", "N");
        paramsPedido.put("ignorarPedidoBloqueado", "N");
        paramsPedido.put("inserirApenasPedidoCompleto", "S");

        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedido.getCodEmp());
        params.put("codFil", pedido.getCodFil());
        params.put("codCli", pedido.getCodCli());
        params.put("codCpg", pedido.getCodCpg());
        params.put("codFpg", pedido.getCodFpg());
        params.put("codRep", pedido.getCodRep());
        params.put("cifFob", "X");
        params.put("indPre", "1");
        params.put("opeExe", "I");
        params.put("tnsPro", "90199");

        List<HashMap<String, Object>> listaItens = new ArrayList<>();
        pedido.getItens().forEach(itemPedido -> {
            HashMap<String, Object> paramsItem = new HashMap<>();
            paramsItem.put("codPro", itemPedido.getCodPro());
            paramsItem.put("codDer", itemPedido.getCodDer());
            paramsItem.put("qtdPed", itemPedido.getQtdPed());
            paramsItem.put("codTpr", itemPedido.getCodTpr());
            paramsItem.put("codDep", "1000");
            paramsItem.put("tnsPro", "90199");
            paramsItem.put("resEst", "N");
            paramsItem.put("pedPrv", "N");
            paramsItem.put("opeExe", "I");
            listaItens.add(paramsItem);
        });
        params.put("produto", listaItens);

        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    private RetornoPedido getRetornoPedidoFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "respostaPedido");

        if (nList.getLength() == 1) {
            return RetornoPedido.fromXml(nList.item(0));
        } else {
            throw new WebServiceRuntimeException("Erro ao extrair retorno de pedido da resposta do servidor.");
        }
    }

    private HashMap<String, String> prepareBaseParams(String codEmp, String codFil) {
        HashMap<String, String> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        return params;
    }

    private HashMap<String, String> prepareParamsForEmpresaAtiva(String user) {
        HashMap<String, String> params = new HashMap<>();
        params.put("NOMUSU", user);
        return params;
    }

    private String prepareParamsForConsultaPrecos(String codEmp, String codTpr, String codPro, String codDer, String datIni, String qtdPdv) { //TODO: refatorar
        String params = "";
        params += "<SID><param>acao=sid.srv.regra</param></SID>";
        params += "<SID><param>numreg=" + numRegTpr + "</param></SID>";
        params += "<SID><param>aCodEmpPdv=" + codEmp + "</param></SID>";
        params += "<SID><param>aCodTprPdv=" + codTpr + "</param></SID>";
        params += "<SID><param>aCodProPdv=" + codPro + "</param></SID>";
        params += "<SID><param>aCodDerPdv=" + codDer + "</param></SID>";
        params += "<SID><param>aDatIniPdv=" + datIni + "</param></SID>";
        params += "<SID><param>aQtdMaxPdv=" + qtdPdv + "</param></SID>";
        return params;
    }

    private void addParamsForProdutos(HashMap<String, String> params) {
        params.put("sitPro", "A");
        params.put("sitDer", "A");
    }

    private void addParamsForTabelaPreco(HashMap<String, String> params, String codRep) {
        params.put("CODREP", codRep);
    }

    private void addParamsForCondicao(HashMap<String, String> params) {
        params.put("sitCpg", "A");
    }

    private void addParamsForFormas(HashMap<String, String> params) {
        params.put("sitCpg", "A");
    }

}

