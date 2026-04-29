package com.itsm.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        System.out.println("CloudinaryService: Uploading file to folder: " + folder);
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "auto"
        );
        
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            String url = (String) uploadResult.get("secure_url");
            System.out.println("CloudinaryService: Upload successful. URL: " + url);
            return url;
        } catch (Exception e) {
            System.err.println("CloudinaryService: Upload failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
