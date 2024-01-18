package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.Representante;
import com.br.datasig.datasigpdvapi.service.WebServiceRequestsService;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/representantes")
@Tag(name = "Representantes", description = "Operações de representantes com o sistema PDV")
public class RepresentantesController extends DataSIGController {
    @Autowired
    private WebServiceRequestsService wsRequestsService;

    @Operation(
            summary = "Buscar representantes",
            description = "Busca os representantes cadastrados"
    )
    @GetMapping(value= "", produces = "application/json")
    @ResponseBody
    public List<Representante> getRepresentantes(@RequestParam String codEmp, @RequestParam String codFil, @RequestParam String token) throws Exception {
        if(isTokenValid(token))
            return wsRequestsService.getRepresentantes(codEmp, codFil, token);
        else
            throw new InvalidTokenException();
    }


}
