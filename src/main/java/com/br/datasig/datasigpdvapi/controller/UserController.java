package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.service.WebServiceRequestsService;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Operações de usuário com o sistema PDV")
public class UserController {
    @Autowired
    private WebServiceRequestsService wsRequestsService;

    @Operation(
            summary = "Realizar login no sistema PDV",
            description = "Cria um token baseado no usuário, senha e timestamp de login, que é retornado ao FrontEnd e utilizado nas outras requisições"
    )
    @PostMapping(value = "/login", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String performLogin(@RequestParam String user, @RequestParam String pswd) throws SOAPClientException {
        return wsRequestsService.performLogin(user, pswd);
    }
}
