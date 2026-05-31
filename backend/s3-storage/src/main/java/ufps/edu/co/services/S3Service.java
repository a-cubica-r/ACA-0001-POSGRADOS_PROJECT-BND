package ufps.edu.co.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import ufps.edu.co.processor.crud.DocumentoProcessor;
import ufps.edu.co.records.input.entity.AspiranteInput.ASPIRANTE_FIND;
import ufps.edu.co.records.input.entity.DocumentoInput.DOCUMENTO_FIND;
import ufps.edu.co.records.output.entity.DocumentoOutput;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${AWS_BUCKET_NAME}")
    private String bucketName;

    @Value("${AWS_REGION}")
    private String region;

    @Autowired
    private DocumentoProcessor documentoProcessor;

    public record UploadResult(String keyfile, String enlaceurl) {
    }

    public UploadResult uploadFile(MultipartFile file) {
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        int dotIndex = originalName.lastIndexOf(".");
        String baseName = (dotIndex > 0) ? originalName.substring(0, dotIndex) : originalName;
        String extension = (dotIndex > 0) ? originalName.substring(dotIndex) : "";
        String sanitizedName = baseName.replaceAll("[^a-zA-Z0-9._-]", "-").toLowerCase();
        String key = UUID.randomUUID().toString() + "-" + sanitizedName + extension;
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo a S3: " + e.getMessage(), e);
        }
        String url = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
        return new UploadResult(key, url);
    }

    public UploadResult uploadBytes(byte[] content, String contentType, String originalName) {
        String safeOriginalName = originalName != null ? originalName : "file.pdf";
        int dotIndex = safeOriginalName.lastIndexOf(".");
        String baseName = (dotIndex > 0) ? safeOriginalName.substring(0, dotIndex) : safeOriginalName;
        String extension = (dotIndex > 0) ? safeOriginalName.substring(dotIndex) : ".pdf";
        String sanitizedName = baseName.replaceAll("[^a-zA-Z0-9._-]", "-").toLowerCase();
        String key = UUID.randomUUID().toString() + "-" + sanitizedName + extension;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType != null ? contentType : "application/octet-stream")
                            .contentLength(content != null ? Long.valueOf(content.length) : 0L)
                            .build(),
                    RequestBody.fromBytes(content != null ? content : new byte[0]));
        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo a S3: " + e.getMessage(), e);
        }

        String url = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
        return new UploadResult(key, url);
    }

    public byte[] downloadDocument(DOCUMENTO_FIND input) {
        DocumentoOutput output = documentoProcessor.findById(input);
        String key = output.keyfile();
        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build());
        return objectAsBytes.asByteArray();
    }

    public List<DocumentoOutput> findDocumentByAspirantId(ASPIRANTE_FIND input) {
        try {
            return documentoProcessor.findByAspiranteId(input);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching documents: " + e.getMessage(), e);
        }
    }}

    

    
    
        
                
                        
                        
                        
                        
                        
                
    
    
    
        
    

    