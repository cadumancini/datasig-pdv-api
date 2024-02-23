package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.Cliente;
import com.br.datasig.datasigpdvapi.entity.ClientePayload;
import com.br.datasig.datasigpdvapi.entity.ClienteResponse;
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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ClientesService extends WebServiceRequestsService{
    public List<Cliente> getClientes(String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, Object> params = prepareBaseParams(codEmp, codFil);
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

    public ClienteResponse putCliente(String token, ClientePayload cliente) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, WebServiceRuntimeException {
        HashMap<String, Object> params = prepareParams(token, cliente);
        String xml = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_cad_clientes", "GravarClientes_5", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        ClienteResponse clienteResponse = getClienteResponseFromXml(xml);
        validateClienteResponse(clienteResponse);

        return clienteResponse;
    }

    private void validateClienteResponse(ClienteResponse clienteResponse) {
        if(!clienteResponse.getRetorno().equals("OK"))
            throw new WebServiceRuntimeException(clienteResponse.getRetorno());
    }

    private HashMap<String, Object> prepareParams(String token, ClientePayload cliente) {
        String nomCli = sanitizeString(cliente.getNomCli());
        String endCli = sanitizeString(cliente.getEndCli());
        String baiCli = sanitizeString(cliente.getBaiCli());
        String cplEnd = sanitizeString(cliente.getCplEnd());
        String cidCli = sanitizeString(cliente.getCidCli());
        String cepCli = sanitizeString(cliente.getCepCli()).replace("-", "");
        String cgcCpf = sanitizeString(cliente.getCgcCpf()).replace("-", "").replace(".", "").replace("/", "");

        HashMap<String, Object> paramsDadosGerais = new HashMap<>();
        paramsDadosGerais.put("tipCli", cliente.getTipCli());
        paramsDadosGerais.put("apeCli", nomCli);
        paramsDadosGerais.put("nomCli", nomCli);
        paramsDadosGerais.put("cgcCpf", cgcCpf);
        paramsDadosGerais.put("cepCli", cepCli);
        paramsDadosGerais.put("endCli", endCli);
        paramsDadosGerais.put("nenCli", cliente.getNenCli());
        paramsDadosGerais.put("cplEnd", cplEnd);
        paramsDadosGerais.put("baiCli", baiCli);
        paramsDadosGerais.put("cidCli", cidCli);
        paramsDadosGerais.put("sigUfs", cliente.getSigUfs());
        paramsDadosGerais.put("fonCli", cliente.getFonCli());
        paramsDadosGerais.put("fonCl2", cliente.getFonCli());
        paramsDadosGerais.put("intNet", cliente.getEmaCli().toUpperCase());
        paramsDadosGerais.put("emaNfe", cliente.getEmaCli().toUpperCase());
        paramsDadosGerais.put("tipMer", "I");
        paramsDadosGerais.put("cliCon", "N");
        paramsDadosGerais.put("sitCli", "A");
        paramsDadosGerais.put("calFun", "N");
        paramsDadosGerais.put("calSen", "N");
        paramsDadosGerais.put("cliFor", "C");
        paramsDadosGerais.put("insEst", "ISENTO");

        List<HashMap<String, Object>> listaDefinicoes = new ArrayList<>();
        HashMap<String, Object> paramsDefinicoesCliente = new HashMap<>();
        paramsDefinicoesCliente.put("cifFob", "X");
        paramsDefinicoesCliente.put("codCpg", "001");
        paramsDefinicoesCliente.put("codEmp", TokensManager.getInstance().getCodEmpFromToken(token));
        paramsDefinicoesCliente.put("codFil", TokensManager.getInstance().getCodFilFromToken(token));
        paramsDefinicoesCliente.put("codRep", cliente.getCodRep());
        paramsDefinicoesCliente.put("conFin", "S");
        paramsDefinicoesCliente.put("indPre", "1");
        listaDefinicoes.add(paramsDefinicoesCliente);
        paramsDadosGerais.put("definicoesCliente", listaDefinicoes);

        HashMap<String, Object> params = new HashMap<>();
        params.put("dadosGeraisCliente", paramsDadosGerais);
        return params;
    }

    private String sanitizeString(String value) {
        String normalizer = Normalizer.normalize(value, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalizer).replaceAll("").toUpperCase();
    }

    private ClienteResponse getClienteResponseFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "retornosClientes");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                return ClienteResponse.fromXml(nNode);
            }
        }
        throw new WebServiceRuntimeException("Erro ao obter retorno do cadastro de cliente");
    }
}
