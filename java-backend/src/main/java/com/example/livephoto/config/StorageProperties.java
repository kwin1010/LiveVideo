package com.example.livephoto.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "livephoto")
public class StorageProperties {

    /**
     * Root directory for storing uploads and converted files.
     */
    @NotBlank
    private String storageRoot = "storage";

    public String getStorageRoot() {
        return storageRoot;
    }

    public void setStorageRoot(String storageRoot) {
        this.storageRoot = storageRoot;
    }
}
