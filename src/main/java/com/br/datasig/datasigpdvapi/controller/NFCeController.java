package com.br.datasig.datasigpdvapi.controller;

import com.br.datasig.datasigpdvapi.entity.ConsultaNotaFiscal;
import com.br.datasig.datasigpdvapi.entity.RetornoNFCe;
import com.br.datasig.datasigpdvapi.entity.SitEdocsResponse;
import com.br.datasig.datasigpdvapi.exceptions.InvalidTokenException;
import com.br.datasig.datasigpdvapi.exceptions.NfceException;
import com.br.datasig.datasigpdvapi.service.NFCeService;
import com.br.datasig.datasigpdvapi.soap.SOAPClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/nfce")
@Tag(name = "NFC-e", description = "Operações de Notas Fiscais de Compra com o sistema PDV")
public class NFCeController extends DataSIGController {
    @Autowired
    private NFCeService nfceService;

    @Operation(
            summary = "Gerar NFC-e",
            description = "Geração de NFC-e após pedido devidamente criado"
    )
    @PostMapping(value = "", produces = "application/json")
    public RetornoNFCe postNFCe(@RequestParam String token, @RequestParam String numPed)
            throws SOAPClientException, IOException, ParserConfigurationException, SAXException, NfceException, TransformerException {
        if(isTokenValid(token)) {
            return nfceService.createNFCe(token, numPed);
        } else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Consultar todas NFC-e's",
            description = "Relação de NFC-e's geradas pelo sistema PDV"
    )
    @GetMapping(value = "", produces = "application/json")
    public List<ConsultaNotaFiscal> getNFCes(@RequestParam String token,
                                             @RequestParam(required = false) String numNfv,
                                             @RequestParam(required = false) String sitNfv,
                                             @RequestParam(required = false) String sitDoe,
                                             @RequestParam(required = false) String datIni,
                                             @RequestParam(required = false) String datFim,
                                             @RequestParam(required = false) String codRep,
                                             @RequestParam(required = false) String nomUsu)
            throws SOAPClientException, IOException, ParserConfigurationException, SAXException, ParseException, TransformerException {
        if(isTokenValid(token))
            return nfceService.getNFCes(token, numNfv, sitNfv, sitDoe, datIni, datFim, codRep, nomUsu);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Cancelar NFC-e",
            description = "Cancelamento de NFC-e"
    )
    @PostMapping(value = "cancelar", produces = "text/plain;charset=UTF-8")
    public String cancelarNFCe(@RequestParam String token, @RequestParam String codSnf, @RequestParam String numNfv, @RequestParam String jusCan)
            throws SOAPClientException, IOException, ParserConfigurationException, SAXException, TransformerException {
        if(isTokenValid(token))
            return nfceService.cancelarNFCe(token, codSnf, numNfv, jusCan);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Inutilizar NFC-e",
            description = "Inutilização de NFC-e"
    )
    @PostMapping(value = "inutilizar", produces = "text/plain;charset=UTF-8")
    public String inutilizarNFCe(@RequestParam String token, @RequestParam String codSnf, @RequestParam String numNfv, @RequestParam String jusCan)
            throws SOAPClientException, IOException, ParserConfigurationException, SAXException, TransformerException {
        if(isTokenValid(token))
            return nfceService.inutilizarNFCe(token, codSnf, numNfv, jusCan);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Consultar no e-DOCS",
            description = "Consultar situação de NFC-e no e-DOCS"
    )
    @GetMapping(value = "edocs", produces = "application/json")
    public SitEdocsResponse getSitEDocs(@RequestParam String token, @RequestParam String codSnf, @RequestParam String numNfv)
            throws SOAPClientException, IOException, ParserConfigurationException, SAXException, ParseException, TransformerException {
        if(isTokenValid(token))
            return nfceService.getSitEDocs(token, codSnf, numNfv);
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Baixar PDF de NFCe",
            description = "Baixar PDF de NFCe para ser impresso"
    )
    @GetMapping(value = "/{nfce}/pdf", produces = "application/pdf")
    // @Deprecated(forRemoval = true) //TODO: remove
    public ResponseEntity<byte[]> getPdf(@RequestParam String token, @PathVariable String nfce) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException {
        if(isTokenValid(token)) {
            byte[] pdfBytes = nfceService.loadInvoiceFromDisk(token, nfce);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename("nfce.pdf").build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        }
        else
            throw new InvalidTokenException();
    }

    @Operation(
            summary = "Obter NFCe em Base64",
            description = "Baixar PDF de NFCe para ser impresso em Base64"
    )
    @GetMapping(value = "/{nfce}/base64", produces = "text/plain;charset=UTF-8")
    public String getNFCeBase64(@RequestParam String token, @PathVariable String nfce) throws SOAPClientException, ParserConfigurationException, IOException, TransformerException, SAXException {
        if(isTokenValid(token)) {
            return nfceService.loadInvoiceBase64(token, nfce);
        }
        else
            throw new InvalidTokenException();
    }
}
