package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.ProdutoDerivacao;
import com.br.datasig.datasigpdvapi.entity.ProdutoPrecos;
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
            summary = "Buscar produtos por tabela de preço",
            description = "Busca todos os produtos presentes numa tabela de preço informada"
    )
    @GetMapping(value= "/tabela", produces = "application/json")
    public List<ProdutoPrecos> getPreco(@RequestParam String token, @RequestParam String codTpr) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token)) {
            return produtosService.getProdutosPorTabelaDePreco(token, codTpr);
        }
        else
            throw new InvalidTokenException();
    }
}
