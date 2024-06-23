package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.Deposito;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.DepositoService;
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
@RequestMapping("/depositos")
@Tag(name = "Depósitos", description = "Operações com depósitos no sistema PDV")
public class DepositosController extends DataSIGController {
    @Autowired
    private DepositoService depositoService;

    @Operation(
            summary = "Buscar depósitos",
            description = "Busca os depósitos disponíveis para venda"
    )
    @GetMapping(value= "", produces = "application/json")
    public List<Deposito> getDepositos(@RequestParam String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        if(isTokenValid(token))
            return depositoService.getDepositos(token);
        else
            throw new InvalidTokenException();
    }
}
