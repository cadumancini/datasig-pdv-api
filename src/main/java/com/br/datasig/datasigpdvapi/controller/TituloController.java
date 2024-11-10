package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.ConsultaTitulo;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.TituloService;
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
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/titulo")
@Tag(name = "Título", description = "Operações de Títulos com o sistema PDV")
public class TituloController extends DataSIGController {
    @Autowired
    private TituloService tituloService;

    @Operation(
            summary = "Consultar Títulos",
            description = "Relação de títulos gerados pelo sistema PDV"
    )
    @GetMapping(value = "", produces = "application/json")
    public List<ConsultaTitulo> getTitulos(@RequestParam String token,
                                           @RequestParam(required = false) String codFpg,
                                           @RequestParam(required = false) String codRep,
                                           @RequestParam(required = false) String datIni,
                                           @RequestParam(required = false) String datFim,
                                           @RequestParam(required = false) String numNfv,
                                           @RequestParam(required = false) String sitDoe,
                                           @RequestParam(required = false) String sitTit)
            throws SOAPClientException, IOException, ParserConfigurationException, SAXException, ParseException, TransformerException {
        if(isTokenValid(token))
            return tituloService.getTitulos(token, codFpg, codRep, datIni, datFim, numNfv, sitDoe, sitTit);
        else
            throw new InvalidTokenException();
    }
}
