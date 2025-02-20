package com.br.datasig.datasigpdvapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RetornoNFCe {
    private String nfce;
    private String printer;
    private String pdfFile;
}
