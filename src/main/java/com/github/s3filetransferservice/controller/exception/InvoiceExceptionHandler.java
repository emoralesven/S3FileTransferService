package com.github.s3filetransferservice.controller.exception;

import com.github.s3filetransferservice.exception.InvoiceException;
import org.apache.tika.exception.TikaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class InvoiceExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Invalid request");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler({TikaException.class,  SAXException.class})
    public ResponseEntity<?> handleOtherExceptions(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", "An error occurred processing the file in validation");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(InvoiceException.class)
    public ResponseEntity<Map<String, Object>> handleInvoiceBitacoraException(InvoiceException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", ex.getFaultTO().getFaultCode());
        response.put("message", ex.getFaultTO().getFaultString());
        return new ResponseEntity<>(response, ex.getFaultTO().getFaultCode() == 406 ? HttpStatus.NOT_ACCEPTABLE : HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", "Internal Server Error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
