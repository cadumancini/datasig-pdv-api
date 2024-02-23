package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.Cliente;
import com.br.datasig.datasigpdvapi.entity.ClientePayload;
import com.br.datasig.datasigpdvapi.entity.ClienteResponse;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.ClientesService;
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
    private ClientesService clientesService;

    @Operation(
            summary = "Buscar clientes",
            description = "Busca os clientes cadastrados"
    )
    @GetMapping(value= "", produces = "application/json")
    public List<Cliente> getClientes(@RequestParam String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        if(isTokenValid(token))
            return clientesService.getClientes(token);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Cadastrar cliente",
            description = "Cadastrar novo cliente na base"
    )
    @PutMapping(value= "", produces = "application/json")
    public ClienteResponse putCliente(@RequestParam String token, @RequestBody ClientePayload cliente) throws SOAPClientException, ParserConfigurationException, IOException, SAXException {
        if(isTokenValid(token))
            return clientesService.putCliente(token, cliente);
        else
            throw new InvalidTokenException();
    }
}
