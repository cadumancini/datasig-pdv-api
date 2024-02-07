package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.ProdutoDerivacao;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.WebServiceRequestsService;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/produtos")
@Tag(name = "Produtos", description = "Operações de produtos com o sistema PDV")
public class ProdutosController extends DataSIGController {
    @Autowired
    private WebServiceRequestsService wsRequestsService;

    @Operation(
            summary = "Buscar produtos",
            description = "Busca os produtos ativos (produto e derivação) cadastrados"
    )
    @GetMapping(value= "", produces = "application/json")
    public List<ProdutoDerivacao> getProdutos(@RequestParam String token, @RequestParam String codTpr) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token))
            return wsRequestsService.getProdutos(token, codTpr);
        else
            throw new InvalidTokenException();
    }
}
