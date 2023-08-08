package com.github.s3filetransferservice.exception;

import com.github.s3filetransferservice.dto.exception.FaultTO;
import lombok.Data;

@Data
public class InvoiceException extends RuntimeException {

    private final FaultTO faultTO;

    public InvoiceException(String message, FaultTO faultTO) {
        super(message);
        this.faultTO = faultTO;
    }

    public InvoiceException(String message, Throwable cause, FaultTO faultTO) {
        super(message, cause);
        this.faultTO = faultTO;
    }
}

