package com.github.s3filetransferservice.service.impl;

import com.github.s3filetransferservice.client.AmazonS3Client;
import com.github.s3filetransferservice.dto.InvoiceDTO;
import com.github.s3filetransferservice.dto.RecGenericContact;
import com.github.s3filetransferservice.dto.exception.FaultTO;
import com.github.s3filetransferservice.exception.InvoiceException;
import com.github.s3filetransferservice.model.Invoice;
import com.github.s3filetransferservice.repository.InvoiceBitacoraRepository;
import com.github.s3filetransferservice.service.InvoiceService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String MESSAGE = "Invoice stored successfully";
    private static final int SUCCESS_CODE = 200;
    private final AmazonS3Client amazonS3Client;
    private final InvoiceBitacoraRepository invoiceBitacoraRepository;

    @Override
    public ResponseEntity<Map<String, Object>> uploadFile(InvoiceDTO invoiceDTO) {
        log.info("Begin uploadFile");
        byte[] decodeBase64 = decodeBase64(invoiceDTO.getFileBase64());
        validatePDF(decodeBase64);
        String keyS3 = generateKeyS3(invoiceDTO);
        Optional<String> optFileKey = amazonS3Client.putInvoiceToS3(decodeBase64,keyS3);
        Invoice invoice = convertToInvoice(invoiceDTO,optFileKey.isPresent()?optFileKey.get():null);
        saveOrUpdate(invoice);
        Map<String, Object> response = createResponseUploadFile();
        log.info("Ended uploadFile");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getPresignedUrl(String os) throws ExecutionException, InterruptedException {
        log.info("Begin get PDF FiLe");
        List<String> osList = Arrays.asList(os.split(","));
        log.info("Os to search "+String.join(", ", osList ));
        Optional<List<Invoice>> invoiceBitacoraListOptional = invoiceBitacoraRepository.findByOsIn(osList);
        invoiceBitacoraListOptional.ifPresent(invoiceBitacoraList -> {
            List<String> osValues = invoiceBitacoraList.stream()
                    .map(Invoice::getOs)
                    .collect(Collectors.toList());

            String osValuesString = String.join(", ", osValues);
            log.info("OS values in invoice bitacora: " + osValuesString);
        });
        List<String> osNotInInvoiceBitacora;
        List<Invoice> invoiceBitacoraList = null;
        if (invoiceBitacoraListOptional.isPresent()) {
            invoiceBitacoraList = invoiceBitacoraListOptional.get();
            Set<String> invoiceBitacoraOsSet = invoiceBitacoraList.stream()
                    .map(Invoice::getOs)
                    .collect(Collectors.toSet());
            osNotInInvoiceBitacora = osList.stream()
                    .filter(osItem -> !invoiceBitacoraOsSet.contains(osItem))
                    .collect(Collectors.toList());
        } else {
            osNotInInvoiceBitacora = new ArrayList<>(osList);
        }
        log.info("Os cant be found "+String.join(", ", osNotInInvoiceBitacora ));


        String presignedUrl = generateByteOutputStream(invoiceBitacoraList);
        Map<String, Object> response = createResponsePresignedUrlFile(presignedUrl,osNotInInvoiceBitacora);
        log.info("Ended get Invoice PDF FiLe" + osNotInInvoiceBitacora);
        return ResponseEntity.ok(response);

    }

    private String generateByteOutputStream(List<Invoice> invoiceBitacoraList) throws ExecutionException, InterruptedException {
        List<byte[]> byteArrayList = new ArrayList<>();

        for (Invoice keyS3 : invoiceBitacoraList) {
            byteArrayList.add(amazonS3Client.getInvoiceArrayByteFromS3(keyS3.getKeyS3()));
        }
        if(byteArrayList.size()>0){
            return mergePdfFile(byteArrayList,invoiceBitacoraList);
        }
        return null;
    }

    private String mergePdfFile(List<byte[]> listaFileLabel, List<Invoice> invoiceBitacoraList) throws ExecutionException, InterruptedException {
        log.info("mergePdfFile  {}", listaFileLabel.size());
        PdfWriter pdfWriter  = null;
        byte[] archivobase = listaFileLabel.get(0);

        try (InputStream inputStream = new ByteArrayInputStream(archivobase);
             OutputStream outputStream = new ByteArrayOutputStream();) {
            inputStream.transferTo(outputStream);

            ByteArrayOutputStream bos = (ByteArrayOutputStream) outputStream;
            pdfWriter = new PdfWriter(bos);

            return generateMerger(listaFileLabel, pdfWriter, bos,invoiceBitacoraList);

        } catch (IOException | NoSuchAlgorithmException e) {

        } catch (InvoiceException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String generateMerger(List<byte[]> listaInvoiceBitacora, PdfWriter pdfWriter, ByteArrayOutputStream bos, List<Invoice> invoiceBitacoraList) throws IOException, ExecutionException, InterruptedException, NoSuchAlgorithmException, InvoiceException {

        List<PdfReader> listaPdfReader = new ArrayList<>();

        log.info("Starting call to generateMerger {}");

        for (int i = 0; i < listaInvoiceBitacora.size(); i++) {
            listaPdfReader.add(new PdfReader(new ByteArrayInputStream(listaInvoiceBitacora.get(i))));
        }
        try{
            PdfDocument pdfDocumentBase = new PdfDocument(listaPdfReader.get(0), pdfWriter);
            PdfMerger merger = new PdfMerger(pdfDocumentBase);
            List<PdfDocument> listPdfDocument = new ArrayList<>();

            for (int j = 1; j < listaPdfReader.size(); j++) {
                listPdfDocument.add(new PdfDocument(listaPdfReader.get(j)));
            }

            for (int k = 0; k < listPdfDocument.size(); k++) {
                merger.merge(listPdfDocument.get(k),1,
                        listPdfDocument.get(k).getNumberOfPages());
                listPdfDocument.get(k).close();
            }
            pdfDocumentBase.close();
            merger.close();
        }catch(Exception e){

        }
        log.info("Starting call to AWS S3");
        String keyS3 = concatenateFields(invoiceBitacoraList);
        log.info("Key S3 Merge PDF FILE: "+keyS3);

        Optional<String> keyFinal = amazonS3Client.putInvoiceToS3(bos.toByteArray()
                ,keyS3+".pdf");

        if(keyFinal.isPresent()) {
            Optional<String> presignedUrlOptional = amazonS3Client.getPresignedUrlFromS3(keyFinal);
            if (presignedUrlOptional.isPresent()) {
                return presignedUrlOptional.get();
            }else{
                throw new InvoiceException(
                        "500", FaultTO.builder().faultString("Cant get PresignedUrl").faultCode(406).build());
            }
        }else{
            throw new InvoiceException(
                    "500", FaultTO.builder().faultString("Cant get Key S3").faultCode(500).build());
        }
    }
    public String concatenateFields(List<Invoice> invoiceBitacoraList) {
        StringBuilder concatenatedString = new StringBuilder();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String dateStr = now.format(formatter);
        concatenatedString.append(dateStr);
        invoiceBitacoraList.forEach(invoiceBitacora -> {
            concatenatedString.append(invoiceBitacora.getOs());
        });
        return concatenatedString.toString();
    }
    private byte[] decodeBase64(String base64File) {
        return Base64.getDecoder().decode(base64File);
    }
    public void validatePDF(byte[] decodedBytes)  {
        try (InputStream is = new ByteArrayInputStream(decodedBytes)) {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext pcontext = new ParseContext();

            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(is, handler, metadata, pcontext);

            String fileType = metadata.get("Content-Type");

            if (!"application/pdf".equals(fileType)) {
                log.info("Invalid File");

                throw new InvoiceException(
                        "406", FaultTO.builder().faultString("Invalid File").faultCode(406).build());
            }
        } catch (IOException | TikaException | SAXException e) {
            throw new RuntimeException("An error occurred processing the file", e);
        }
    }
    public String generateKeyS3(InvoiceDTO invoiceDTO) {
        StringBuilder keyS3Builder = new StringBuilder();
        keyS3Builder.append(invoiceDTO.getOs())
                .append(invoiceDTO.getReferenceNumber())
                .append(invoiceDTO.getAccount())
                .append(".pdf");
        return keyS3Builder.toString();
    }
    private Invoice convertToInvoice(InvoiceDTO invoiceDTO,String keyS3) {
        Invoice invoice = Invoice.builder()
                .fileBase64(invoiceDTO.getFileBase64())
                .os(invoiceDTO.getOs())
                .account(invoiceDTO.getAccount())
                .referenceNumber(invoiceDTO.getReferenceNumber())
                .keyS3(keyS3)
                .build();


        return invoice;
    }
    private void saveOrUpdate(Invoice invoice) {
        Optional<Invoice> existingInvoiceOpt = invoiceBitacoraRepository.findByOsAndReferenceNumber(invoice.getOs(), invoice.getReferenceNumber());

        if (existingInvoiceOpt.isPresent()) {
            log.info("Document found");
            Invoice existingInvoice = existingInvoiceOpt.get();
            existingInvoice.setFileBase64(invoice.getFileBase64());
            existingInvoice.setKeyS3(invoice.getKeyS3());
            existingInvoice.setUpdate_at(LocalDateTime.now().format(FORMATTER));
            invoiceBitacoraRepository.save(existingInvoice);
        } else {
            log.info("Document not found");
            invoiceBitacoraRepository.save(invoice);
        }
    }



    private Map<String, Object> createResponseUploadFile() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", SUCCESS_CODE);
        response.put("message", MESSAGE);
        return response;
    }
    private Map<String, Object> createResponsePresignedUrlFile(String presignedUrl, List<String> osNotInInvoiceBitacora) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", 200);
        responseBody.put("message", osNotInInvoiceBitacora.isEmpty()?"OK": "OK but this is OS cant be found: "+String.join(", ", osNotInInvoiceBitacora ));
        responseBody.put("presignedUrl",presignedUrl);
        return responseBody;
    }

}
