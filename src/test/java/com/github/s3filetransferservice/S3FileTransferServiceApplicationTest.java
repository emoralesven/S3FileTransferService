package com.github.s3filetransferservice;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class S3FileTransferServiceApplicationTest {

    @Test
    public void testApplicationInstantiation() {
        S3FileTransferServiceApplication appInstance = new S3FileTransferServiceApplication();
        assertNotNull(appInstance);
    }

}