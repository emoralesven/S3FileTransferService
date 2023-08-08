package com.github.s3filetransferservice.dto.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FaultTO {
    private int faultCode;
    private String faultString;

    // Constructor, getters y setters

    public FaultTO(int faultCode, String faultString) {
        this.faultCode = faultCode;
        this.faultString = faultString;
    }

    public int getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(int faultCode) {
        this.faultCode = faultCode;
    }

    public String getFaultString() {
        return faultString;
    }

    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }
}

