package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.*;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.service.PedidoService;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.ParseException;
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
    public RetornoPedido putPedido(@RequestParam String token, @RequestBody PayloadPedido pedido, HttpServletRequest request) throws SOAPClientException, IOException, ParserConfigurationException, SAXException, TransformerException {
        if(isTokenValid(token)) {
            String clientIP = getClientIp(request);
            return pedidoService.createPedido(token, pedido, clientIP);
        }
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Buscar pedidos",
            description = "Busca de pedidos para consulta no PDV"
    )
    @GetMapping(value = "", produces = "application/json")
    public List<PedidoConsultavel> getPedidos(@RequestParam String token,
                                           @RequestParam BuscaPedidosTipo tipo,
                                           @RequestParam BuscaPedidosSituacao situacao,
                                           @RequestParam String order,
                                           @RequestParam(required = false) String numPed,
                                           @RequestParam(required = false) String datIni,
                                           @RequestParam(required = false) String datFim,
                                           @RequestParam(required = false) String codCli,
                                           @RequestParam(required = false) String codRep,
                                           @RequestParam(required = false) boolean detalhado) throws SOAPClientException, IOException, ParserConfigurationException, SAXException, TransformerException {
        if(isTokenValid(token))
            return pedidoService.getPedidos(token, tipo, situacao, order, numPed, datIni, datFim, codCli, codRep, detalhado);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Buscar pedido detalhado",
            description = "Busca de dados de pedido para consulta no PDV"
    )
    @GetMapping(value = "/{numPed}", produces = "application/json")
    public ConsultaPedidoDetalhes getPedido(@RequestParam String token,
                                              @PathVariable String numPed) throws SOAPClientException, IOException, ParserConfigurationException, SAXException, TransformerException {
        if(isTokenValid(token))
            return pedidoService.getPedido(token, numPed);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Cancelar pedido",
            description = "Cancelamento de pedido"
    )
    @PostMapping(value = "cancelar", produces = "application/json")
    public RetornoPedido cancelarPedido(@RequestParam String token, @RequestParam String numPed,
                                        @RequestParam String sitPed, HttpServletRequest request) throws SOAPClientException, IOException, ParserConfigurationException, SAXException, TransformerException, ParseException {
        if(isTokenValid(token)) {
            String clientIP = getClientIp(request);
            return pedidoService.cancelarPedido(token, numPed, sitPed, clientIP);
        }
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Calcular desconto",
            description = "Cálculo de desconto em porcentagem"
    )
    @GetMapping(value = "calcularDesconto", produces = "text/plain;charset=UTF-8")
    public String calcularDesconto(@RequestParam String token, @RequestParam double vlrPro, @RequestParam double vlrDsc) {
        if(isTokenValid(token))
            return pedidoService.calcularDesconto(vlrPro, vlrDsc);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Calcular item com desconto",
            description = "Cálculo de valor do item com desconto em porcentagem"
    )
    @GetMapping(value = "calcularItemComDesconto", produces = "text/plain;charset=UTF-8")
    public String calcularItemComDesconto(@RequestParam String token, @RequestParam double vlrPro, @RequestParam double vlrDsc) {
        if(isTokenValid(token))
            return pedidoService.calcularItemComDesconto(vlrPro, vlrDsc);
        else
            throw new InvalidTokenException();
    }
}
