package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.Cliente;
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
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Operações de clientes com o sistema PDV")
public class ClientesController extends DataSIGController {
    @Autowired
    private WebServiceRequestsService wsRequestsService;

    @Operation(
            summary = "Buscar clientes",
            description = "Busca os clientes cadastrados"
    )
    @GetMapping(value= "", produces = "application/json")
    public List<Cliente> getClientes(@RequestParam String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        if(isTokenValid(token))
            return wsRequestsService.getClientes(token);
        else
            throw new InvalidTokenException();
    }
}
