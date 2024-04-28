package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.*;
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
import java.util.List;

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
    public RetornoPedido putPedido(@RequestParam String token, @RequestBody PayloadPedido pedido) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token))
            return pedidoService.createPedido(token, pedido);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Buscar pedidos",
            description = "Busca de pedidos para consulta no PDV"
    )
    @GetMapping(value = "", produces = "application/json")
    public List<ConsultaPedido> getPedidos(@RequestParam String token,
                                           @RequestParam TipoBuscaPedidos statusPedido,
                                           @RequestParam String order,
                                           @RequestParam(required = false) String numPed,
                                           @RequestParam(required = false) String datIni,
                                           @RequestParam(required = false) String datFim) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token))
            return pedidoService.getPedidos(token, statusPedido, order, numPed, datIni, datFim);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Buscar detalhes do pedido",
            description = "Busca de detalhes de pedido específico para consulta no PDV"
    )
    @GetMapping(value = "detalhes", produces = "application/json")
    public ConsultaDetalhesPedido getPedido(@RequestParam String token,
                                                  @RequestParam String numPed) throws SOAPClientException, IOException, ParserConfigurationException, SAXException {
        if(isTokenValid(token))
            return pedidoService.getPedido(token, numPed);
        else
            throw new InvalidTokenException();
    }
}
