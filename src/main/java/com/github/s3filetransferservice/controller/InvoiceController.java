package com.github.s3filetransferservice.controller;

import com.github.s3filetransferservice.dto.InvoiceDTO;
import com.github.s3filetransferservice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/upload/v1")
    public ResponseEntity<Map<String, Object>> uploadFile(@Valid @RequestBody InvoiceDTO invoiceDTO) throws TikaException, IOException, SAXException {
        return invoiceService.uploadFile(invoiceDTO);
    }
    @GetMapping("/presigned-url/v1")
    public ResponseEntity<Map<String, Object>> presignedUrlFile(@Valid @RequestParam String os) throws ExecutionException, InterruptedException {
        return invoiceService.getPresignedUrl(os);
    }
}