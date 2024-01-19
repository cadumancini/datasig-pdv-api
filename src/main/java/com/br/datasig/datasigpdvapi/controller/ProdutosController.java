package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.ProdutoDerivacao;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.WebServiceRequestsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @ResponseBody
    public List<ProdutoDerivacao> getProdutos(@RequestParam String codEmp, @RequestParam String codFil, @RequestParam String token) throws Exception {
        if(isTokenValid(token))
            return wsRequestsService.getProdutos(codEmp, codFil, token);
        else
            throw new InvalidTokenException();
    }
}
