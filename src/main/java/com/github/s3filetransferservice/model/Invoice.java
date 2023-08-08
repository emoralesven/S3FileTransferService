package com.github.s3filetransferservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document(collection = "InvoiceBitacora")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice implements Serializable {

    private static final long serialVersionUID = 7672936385153673862L;
    @Id
    private String id;
    private String fileBase64;

    private String os;
    private String account;
    private String referenceNumber;
    private String keyS3;
    private String create_at;
    private String update_at;
}