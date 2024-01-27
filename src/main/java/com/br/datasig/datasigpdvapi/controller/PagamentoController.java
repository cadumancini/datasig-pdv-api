package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.Cliente;
import com.br.datasig.datasigpdvapi.entity.CondicaoPagamento;
import com.br.datasig.datasigpdvapi.entity.FormaPagamento;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.WebServiceRequestsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagamentos")
@Tag(name = "Pagamentos", description = "Operações de pagamento com o sistema PDV")
public class PagamentoController extends DataSIGController {
    @Autowired
    private WebServiceRequestsService wsRequestsService;

    @Operation(
            summary = "Buscar condições de pagamento",
            description = "Busca as condições de pagamento cadastradas"
    )
    @GetMapping(value= "/condicoes", produces = "application/json")
    @ResponseBody
    public List<CondicaoPagamento> getCondicoes(@RequestParam String token) throws Exception {
        if(isTokenValid(token))
            return wsRequestsService.getCondicoesPagamento(token);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Buscar formas de pagamento",
            description = "Busca as formas de pagamento cadastradas"
    )
    @GetMapping(value= "/formas", produces = "application/json")
    @ResponseBody
    public List<FormaPagamento> getFormas(@RequestParam String token) throws Exception {
        if(isTokenValid(token))
            return wsRequestsService.getFormasPagamento(token);
        else
            throw new InvalidTokenException();
    }
}
