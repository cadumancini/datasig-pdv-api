package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.Pedido;
import com.br.datasig.datasigpdvapi.entity.RetornoPedido;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.PedidoService;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Operações de pedidos com o sistema PDV")
public class PedidoController extends DataSIGController {
    @Autowired
    private PedidoService pedidoService;

    @Operation(
            summary = "Gerar pedido",
            description = "Geração de pedido"
    )
    @PutMapping(value = "", produces = "application/json", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public RetornoPedido putPedido(@RequestParam String token, @RequestBody Pedido pedido) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token))
            return pedidoService.createPedido(token, pedido);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Gerar NFC-e",
            description = "Geração de NFC-e após pedido devidamente criado"
    )
    @PutMapping(value = "/nfce", produces = "text/plain;charset=UTF-8")
    @ResponseStatus(code = HttpStatus.CREATED)
    public String putNFCe(@RequestParam String token, @RequestParam String numPed) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token))
            return pedidoService.createNFCe(token, numPed);
        else
            throw new InvalidTokenException();
    }
}
