package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.OperacaoCaixaResultado;
import com.br.datasig.datasigpdvapi.entity.TipoOperacaoCaixa;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.OperacaoCaixaService;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@RestController
@RequestMapping("/caixa")
@Tag(name = "Caixa", description = "Operações de Caixa no sistema PDV")
public class CaixaController extends DataSIGController {
    @Autowired
    private OperacaoCaixaService caixaService;

    @Operation(
            summary = "Operação caixa",
            description = "Realizar operação no caixa (operações disponíveis: abertura, sangria e fechamento)"
    )
    @PostMapping(value = "", produces = "application/json")
    public OperacaoCaixaResultado realizarOperacaoCaixa(@RequestParam String token, @RequestParam TipoOperacaoCaixa tipoOperacao, @RequestParam String valorOperacao, @RequestParam String hisMov) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        if(isTokenValid(token))
            return caixaService.realizarOperacaoCaixa(token, tipoOperacao, valorOperacao, hisMov);
        else
            throw new InvalidTokenException();
    }
}
