package com.github.s3filetransferservice.client;

import com.github.s3filetransferservice.dto.exception.FaultTO;
import com.github.s3filetransferservice.exception.InvoiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
@Component
public class AmazonS3Client {
    @Value("${bucket.region}")
    private String awsRegion;

    @Value("${bucket.s3}")
    private String awsBucket;

    @Value("${bucket.path}")
    private String awsPath;
    @Value("${bucket.duration}")
    private String duration;
    private S3Client s3;
    private  S3Presigner presigner;

    private static final Logger log = LoggerFactory.getLogger(AmazonS3Client.class);
    @PostConstruct
    public void init() {
        log.info("Initializing S3 client");
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create("AKIAWUQAQIKDNDBN4CLA", "blsjD2LgXE5eYEpoZaBhK9dWeqreiXMflA4S8XE7");
        this.s3 = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(awsRegion))
                .build();
        this.presigner = S3Presigner.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
    public Optional<String> putInvoiceToS3(byte[] bytesArray, String keyS3) {

            String keyFinal= awsPath.concat("/"+keyS3);
            log.info("Begin process upload file byte array to S3  and keyS3 : "+keyS3);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(awsBucket).key(keyFinal).
                    build();
            s3.putObject(request,
                    RequestBody.fromBytes(bytesArray));
            log.info("Ended process upload file byte array to S3 for keyS3 : "+keyS3);

            return Optional.ofNullable(keyFinal);


    }

    public byte[] getInvoiceArrayByteFromS3(String keyS3) {
        String msge;

        try {

            log.info("Begin process get Invoice Array Byte From keyS3 : "+keyS3);
            log.info("Load file optFileKey : "+ keyS3);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(awsBucket)
                    .key(keyS3)
                    .build();
            ResponseBytes<GetObjectResponse> responseBytes = s3.getObjectAsBytes(getObjectRequest);
            return responseBytes.asByteArray();

        } catch (Exception e) {
            msge =  String.format("Error  awsBucket: %2s - awsPath: %3s - error: %4s" , awsBucket, awsPath, e.getMessage());
            log.error(msge);
            throw new InvoiceException( msge, FaultTO.builder().faultString(msge).faultCode(500).build());
        }
    }


    public Optional<String> getPresignedUrlFromS3( Optional<String> keyS3) {
        String msge;
        String keyFinal = null;
        try {
            if(keyS3.isPresent()|| !keyS3.isEmpty()) {
                keyFinal= keyS3.get();
            }
            log.info("Begin process get Presigned Url From S3 : " +keyFinal);
            log.info("Load file optFileKey : " + keyFinal);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(awsBucket)
                    .key(keyFinal)
                    .build();
            log.info("Generating presignedUrl with duration: : " + duration);

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(Integer.parseInt(duration)))
                    .getObjectRequest(getObjectRequest)
                    .build();

            URL presignedUrl = presigner.presignGetObject(presignRequest).url();

            log.info("Presigned URL: " + presignedUrl.toString());

            return Optional.ofNullable(presignedUrl.toString());

        } catch (Exception e) {
            msge =  String.format("Error  awsBucket: %2s - awsPath: %3s - error: %4s" , awsBucket, awsPath, e.getMessage());
            log.error(msge);
            throw new InvoiceException( msge, FaultTO.builder().faultString(msge).faultCode(500).build());
        }
    }

}
