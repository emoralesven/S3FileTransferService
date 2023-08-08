package com.github.s3filetransferservice.service;

import com.github.s3filetransferservice.dto.InvoiceDTO;
import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface InvoiceService {
    ResponseEntity<Map<String, Object>> uploadFile(InvoiceDTO invoiceDTO) throws TikaException, IOException, SAXException;
    ResponseEntity<Map<String, Object>> getPresignedUrl(String fileName) throws ExecutionException, InterruptedException;
}