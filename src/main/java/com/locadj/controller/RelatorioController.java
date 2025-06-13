package com.locadj.controller;

import com.itextpdf.text.DocumentException;
import com.locadj.model.User;
import com.locadj.repository.UserRepository;
import com.locadj.service.PdfExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RelatorioController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PdfExportService pdfExportService;

    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> exportarPdf() throws DocumentException {
        List<User> usuarios = userRepository.findAll();
        byte[] pdfBytes = pdfExportService.gerarRelatorioUsuarios(usuarios);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio-usuarios.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
