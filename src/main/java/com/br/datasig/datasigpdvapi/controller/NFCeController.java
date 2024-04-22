package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.ConsultaNotaFiscal;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.NFCeService;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/nfce")
@Tag(name = "NFC-ee", description = "Operações de Notas Fiscais de Compra com o sistema PDV")
public class NFCeController extends DataSIGController {
    @Autowired
    private NFCeService nfceService;

    @Operation(
            summary = "Gerar NFC-e",
            description = "Geração de NFC-e após pedido devidamente criado"
    )
    @PutMapping(value = "", produces = "text/plain;charset=UTF-8")
    @ResponseStatus(code = HttpStatus.CREATED)
    public String putNFCe(@RequestParam String token, @RequestParam String numPed) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token))
            return nfceService.createNFCe(token, numPed);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Consultar todas NFC-e's",
            description = "Relação de NFC-e's geradas pelo sistema PDV"
    )
    @GetMapping(value = "", produces = "application/json")
    public List<ConsultaNotaFiscal> getNFCes(@RequestParam String token,
                                             @RequestParam(required = false) String numNfv,
                                             @RequestParam(required = false) String sitNfv,
                                             @RequestParam(required = false) String datIni,
                                             @RequestParam(required = false) String datFim) throws SOAPClientException, IOException, ParserConfigurationException, SAXException, ParseException {
        if(isTokenValid(token))
            return nfceService.getNFCes(token, numNfv, sitNfv, datIni, datFim);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Cancelar NFC-e",
            description = "Cancelamento de NFC-e"
    )
    @PutMapping(value = "cancelar", produces = "text/plain;charset=UTF-8")
    public String cancelarNFCe(@RequestParam String token, @RequestParam String codSnf, @RequestParam String numNfv, @RequestParam String jusCan) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token))
            return nfceService.cancelarNFCe(token, codSnf, numNfv, jusCan);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Inutilizar NFC-e",
            description = "Inutilização de NFC-e"
    )
    @PutMapping(value = "inutilizar", produces = "text/plain;charset=UTF-8")
    public String inutilizarNFCe(@RequestParam String token, @RequestParam String codSnf, @RequestParam String numNfv, @RequestParam String jusCan) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token))
            return nfceService.inutilizarNFCe(token, codSnf, numNfv, jusCan);
        else
            throw new InvalidTokenException();
    }
}