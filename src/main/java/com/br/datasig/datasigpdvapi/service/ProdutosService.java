package com.br.datasig.datasigpdvapi.service;

import com.br.datasig.datasigpdvapi.entity.FaixaPreco;
import com.br.datasig.datasigpdvapi.entity.ProdutoPrecos;
import com.br.datasig.datasigpdvapi.entity.ProdutoTabela;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import com.br.datasig.datasigpdvapi.token.TokensManager;
import com.br.datasig.datasigpdvapi.util.XmlUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Component
public class ProdutosService extends WebServiceRequestsService{
    public List<ProdutoPrecos> getProdutosPorTabelaDePreco(String token, String codTpr) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        String codEmp = TokensManager.getInstance().getCodEmpFromToken(token);
        String codFil = TokensManager.getInstance().getCodFilFromToken(token);
        HashMap<String, Object> params = prepareBaseParams(codEmp, codFil);
        addParamsForProdutosPorTabelaDePreco(params, codTpr);

        String xml = soapClient.requestFromSeniorWS("ConsultaTabelaPreco", "Consultar", token, "0", params, true);

        XmlUtils.validateXmlResponse(xml);
        List<ProdutoTabela> produtosPorTabela = getProdutosPorTabelaFromXml(xml, codTpr);

        return groupProdutosPorFaixasPreco(produtosPorTabela);
    }

    private List<ProdutoPrecos> groupProdutosPorFaixasPreco(List<ProdutoTabela> produtosPorTabela) {
        List<ProdutoPrecos> produtosGrouped = new ArrayList<>();

        produtosPorTabela.sort(Comparator.comparing(ProdutoTabela::getCodPro)
                .thenComparing(ProdutoTabela::getCodDer)
                .thenComparing(ProdutoTabela::getQtdMax));

        for(ProdutoTabela produto : produtosPorTabela) {
            ProdutoPrecos produtoPrecos = produtosGrouped.stream().filter(grouped -> grouped.getCodPro().equals(produto.getCodPro()) && grouped.getCodDer().equals(produto.getCodDer())).findFirst().orElse(null);
            if (produtoPrecos == null) {
                List<FaixaPreco> faixasPreco = new ArrayList<>();
                faixasPreco.add(new FaixaPreco(produto.getPreBas(), produto.getQtdMax()));
                produtosGrouped.add(new ProdutoPrecos(produto.getCodPro(), produto.getCodDer(), produto.getCodBar(),
                        produto.getCodTpr(), produto.getDatIni(), produto.getDesPro(), faixasPreco));
            } else {
                produtosGrouped.get(produtosGrouped.indexOf(produtoPrecos))
                        .getFaixasPreco().add(new FaixaPreco(produto.getPreBas(), produto.getQtdMax()));
            }
        }

        return produtosGrouped;
    }

    private void addParamsForProdutosPorTabelaDePreco(HashMap<String, Object> params, String codTpr) {
        params.put("codTpr", codTpr);
    }

    private List<ProdutoTabela> getProdutosPorTabelaFromXml(String xml, String codTpr) throws ParserConfigurationException, IOException, SAXException {
        List<ProdutoTabela> produtos = new ArrayList<>();
        NodeList nList = XmlUtils.getNodeListByElementName(xml, "tabela");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                produtos.add(ProdutoTabela.fromXml(nNode));
            }
        }
        return produtos.stream().filter(prod -> prod.getCodTpr().equals(codTpr)).toList();
    }
}
