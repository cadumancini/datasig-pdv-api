package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.*;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.ClientesService;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
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
    public List<Cliente> getClientes(@RequestParam String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        if(isTokenValid(token))
            return clientesService.getClientes(token);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Buscar clientes (lista simples)",
            description = "Busca os clientes cadastrados e retorna uma lista simplificada, para mostrar na consulta de clientes"
    )
    @GetMapping(value= "simplified", produces = "application/json")
    public List<ClienteSimplified> getClientesSimplified(@RequestParam String token) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        if(isTokenValid(token))
            return clientesService.getClientesSimplified(token);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Buscar cliente",
            description = "Busca os dados completos de determinado cliente"
    )
    @GetMapping(value= "cliente", produces = "application/json")
    public Cliente getCliente(@RequestParam String token, @RequestParam(required = false) String codCli, @RequestParam(required = false) String cgcCpf) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        if(isTokenValid(token))
            return clientesService.getCliente(token, codCli, cgcCpf);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Cadastrar cliente",
            description = "Cadastrar novo cliente na base"
    )
    @PutMapping(value= "", produces = "application/json")
    public ClienteResponse putCliente(@RequestParam String token, @RequestBody ClientePayload cliente) throws SOAPClientException, ParserConfigurationException, IOException, SAXException, TransformerException {
        if(isTokenValid(token))
            return clientesService.putCliente(token, cliente);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Buscar CEP",
            description = "Busca informações do CEP para auxiliar no cadastro do cliente"
    )
    @GetMapping(value= "consultaCEP", produces = "application/json")
    public ConsultaCEP getInformacoesCEP(@RequestParam String token, @RequestParam String numCep) throws ParserConfigurationException, IOException, SAXException {
        if(isTokenValid(token))
            return clientesService.getInformacoesCEP(numCep);
        else
            throw new InvalidTokenException();
    }
}
