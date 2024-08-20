package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.FormaPagamento;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.PagamentoService;
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
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/pagamentos")
@Tag(name = "Pagamentos", description = "Operações de pagamento com o sistema PDV")
public class PagamentoController extends DataSIGController {
    @Autowired
    private PagamentoService pagamentoService;

    @Operation(
            summary = "Buscar formas de pagamento",
            description = "Busca as formas de pagamento cadastradas"
    )
    @GetMapping(value= "/formas", produces = "application/json")
    public List<FormaPagamento> getFormas(@RequestParam String token) throws SOAPClientException, IOException, ParserConfigurationException, SAXException, TransformerException {
        if(isTokenValid(token))
            return pagamentoService.getFormasPagamento(token);
        else
            throw new InvalidTokenException();
    }
}
