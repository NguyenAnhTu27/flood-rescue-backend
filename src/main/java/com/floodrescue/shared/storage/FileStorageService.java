package com.floodrescue.shared.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    StoredFile storeRescueAttachment(MultipartFile file, String extension) throws IOException;
}
