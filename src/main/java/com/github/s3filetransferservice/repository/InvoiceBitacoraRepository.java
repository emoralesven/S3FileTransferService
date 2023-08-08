package com.github.s3filetransferservice.repository;

import com.github.s3filetransferservice.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceBitacoraRepository extends MongoRepository<Invoice, String> {

    Optional<Invoice> findByOs(String os);
    Optional<Invoice> findByOsAndReferenceNumber(String os, String referenceNumber);
    Optional<List<Invoice>> findByOsIn(List<String> osList);
    Long deleteByOs(String os);


}