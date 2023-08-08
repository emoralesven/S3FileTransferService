package com.github.s3filetransferservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Document(collection = "InvoiceBitacora")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO implements Serializable {

    private static final long serialVersionUID = 7672936385153673862L;
    @NotNull
    private String fileBase64;
    @NotNull
    private String os;
    @NotNull
    private String account;
    @NotNull
    private String referenceNumber;

}
