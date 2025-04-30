package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.TokenResponse;
import com.br.datasig.datasigpdvapi.exceptions.NotAllowedUserException;
import com.br.datasig.datasigpdvapi.service.UserService;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Operações de usuário com o sistema PDV")
public class UserController extends DataSIGController {
    @Autowired
    private UserService userService;

    @Operation(
            summary = "Realizar login no sistema PDV",
            description = "Cria um token baseado no usuário, senha e timestamp de login, que é retornado ao FrontEnd e utilizado nas outras requisições"
    )
    @PostMapping(value = "/login", produces = "text/plain;charset=UTF-8")
    public String login(@RequestParam String user, @RequestParam String pswd, HttpServletRequest request) throws IOException, ParserConfigurationException, SAXException, SOAPClientException, NotAllowedUserException, TransformerException {
        String clientIp = getClientIp(request);
        return userService.login(user, pswd, clientIp);
    }

    @Operation(
            summary = "Realizar logout no sistema PDV",
            description = "Remove o token da lista"
    )
    @PostMapping(value = "/logout", produces = "text/plain;charset=UTF-8")
    public String logout(@RequestParam String token) {
        return userService.logout(token);
    }

    @Operation(
            summary = "Buscar parâmetros usuário",
            description = "Retorna os parâmetros do usuário baseados no token"
    )
    @GetMapping(value = "/params", produces = "application/json")
    public TokenResponse getParamsFromToken(@RequestParam String token) {
        return userService.getParamsFromToken(token);
    }
}
