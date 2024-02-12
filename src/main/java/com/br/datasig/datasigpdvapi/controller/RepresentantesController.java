package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.Representante;
import com.br.datasig.datasigpdvapi.entity.TabelaPreco;
import com.br.datasig.datasigpdvapi.service.RepresentantesService;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
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
@RequestMapping("/representantes")
@Tag(name = "Representantes", description = "Operações de representantes com o sistema PDV")
public class RepresentantesController extends DataSIGController {
    @Autowired
    private RepresentantesService representantesService;

    @Operation(
            summary = "Buscar representantes",
            description = "Busca os representantes cadastrados"
    )
    @GetMapping(value= "", produces = "application/json")
    public List<Representante> getRepresentantes(@RequestParam String token) throws ParserConfigurationException, IOException, SAXException, SOAPClientException {
        if(isTokenValid(token))
            return representantesService.getRepresentantes(token);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Buscar tabelas de preço",
            description = "Busca as tabelas de preço ligadas ao Representante"
    )
    @GetMapping(value= "/tabelasPreco", produces = "application/json")
    public List<TabelaPreco> getTabelasPrecoPorRepresentantes(@RequestParam String token, @RequestParam String codRep) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token))
            return representantesService.getTabelasPrecoPorRepresentantes(token, codRep);
        else
            throw new InvalidTokenException();
    }
}
