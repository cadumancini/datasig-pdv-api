package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SitEdocsResponse {
    String response;
    ConsultaNotaFiscal notaFiscal;
}
