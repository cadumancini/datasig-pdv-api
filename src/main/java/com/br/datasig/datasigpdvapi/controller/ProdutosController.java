package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.ProdutoDerivacao;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.ProdutosService;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/produtos")
@Tag(name = "Produtos", description = "Operações de produtos com o sistema PDV")
public class ProdutosController extends DataSIGController {
    @Autowired
    private ProdutosService produtosService;

    @Operation(
            summary = "Buscar produtos",
            description = "Busca os produtos ativos (produto e derivação) cadastrados"
    )
    @GetMapping(value= "", produces = "application/json")
    public List<ProdutoDerivacao> getProdutos(@RequestParam String token) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token))
            return produtosService.getProdutos(token);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Consultar preço",
            description = "Busca o preço do produto na tabela de preços informada"
    )
    @GetMapping(value= "/preco", produces = "text/plain;charset=UTF-8")
    public String getPreco(@RequestParam String token, @RequestParam String codPro, @RequestParam String codDer, @RequestParam String codTpr, @RequestParam String qtdPdv) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token))
            return produtosService.getPreco(token, codPro, codDer, codTpr, qtdPdv);
        else
            throw new InvalidTokenException();
    }
}
