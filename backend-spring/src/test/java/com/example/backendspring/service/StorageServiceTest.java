package com.example.backendspring.service;

import com.example.backendspring.config.properties.MinioBucketsProperties;
import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.repository.JobRepository;
import com.example.backendspring.support.TestResources;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private MinioBucketsProperties buckets;

    @Mock
    private MinioClient minioClient;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private StorageService storageService;

    @Test
    void loadTextFileCreatesJobWithCreatedStatus() throws Exception {
        byte[] data = "hello from file".getBytes(StandardCharsets.UTF_8);

        when(buckets.text()).thenReturn("text-bucket");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(data));
        when(file.getOriginalFilename()).thenReturn("input.txt");
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getSize()).thenReturn((long) data.length);
        when(minioClient.bucketExists(argThat(args -> "text-bucket".equals(args.bucket())))).thenReturn(false);
        when(jobRepository.save(argThat(job ->
                JobStatus.CREATED.equals(job.getStatus())
                        && TestResources.USER_ID.equals(job.getUserId())
                        && "input.txt".equals(job.getFileName())
                        && job.getSourceFile() != null
                        && job.getSourceFile().endsWith("input.txt")
        ))).thenAnswer(invocation -> invocation.getArgument(0));

        Job saved = storageService.loadTextFile(file, TestResources.USER_ID);

        assertNotNull(saved);
        assertEquals(JobStatus.CREATED, saved.getStatus());
        assertEquals("input.txt", saved.getFileName());
        assertEquals(TestResources.USER_ID, saved.getUserId());
        verify(minioClient).makeBucket(argThat(args -> "text-bucket".equals(args.bucket())));
        verify(minioClient).putObject(argThat(args ->
                "text-bucket".equals(args.bucket())
                        && args.object().endsWith("input.txt")
        ));
    }

    @Test
    void loadTextFileWrapsExceptionsFromMinio() throws Exception {
        when(buckets.text()).thenReturn("text-bucket");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8)));
        when(minioClient.bucketExists(argThat(args -> "text-bucket".equals(args.bucket()))))
                .thenThrow(new RuntimeException("minio unavailable"));

        assertThrows(RuntimeException.class, () -> storageService.loadTextFile(file, TestResources.USER_ID));

        verifyNoInteractions(jobRepository);
    }

    @Test
    void downloadFileThrowsWhenJobNotDone() {
        Job job = TestResources.job(JobStatus.PROCESSING);

        assertThrows(IllegalStateException.class, () -> storageService.downloadFile(job));
    }

    @Test
    void downloadFileThrowsWhenResultFileIsMissing() {
        Job job = TestResources.job(JobStatus.DONE);
        job.setResultFile(null);

        assertThrows(IllegalStateException.class, () -> storageService.downloadFile(job));
    }

    @Test
    void downloadFileReturnsInputStreamFromMinio() throws Exception {
        Job job = TestResources.job(JobStatus.DONE);
        job.setResultFile("speech/output.mp3");
        GetObjectResponse expectedStream = org.mockito.Mockito.mock(GetObjectResponse.class);

        when(buckets.speech()).thenReturn("speech-bucket");
        when(minioClient.getObject(argThat(args ->
                "speech-bucket".equals(args.bucket()) && "speech/output.mp3".equals(args.object())
        ))).thenReturn(expectedStream);

        InputStream result = storageService.downloadFile(job);

        assertSame(expectedStream, result);
    }

    @Test
    void downloadFileWrapsMinioFailure() throws Exception {
        Job job = TestResources.job(JobStatus.DONE);
        job.setResultFile("speech/output.mp3");

        when(buckets.speech()).thenReturn("speech-bucket");
        when(minioClient.getObject(argThat(args ->
                "speech-bucket".equals(args.bucket()) && "speech/output.mp3".equals(args.object())
        ))).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> storageService.downloadFile(job));
    }
}

