package com.example.livephoto.controller;

import com.example.livephoto.model.ConversionResponse;
import com.example.livephoto.service.LivePhotoService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
public class LivePhotoController {

    private final LivePhotoService livePhotoService;

    public LivePhotoController(LivePhotoService livePhotoService) {
        this.livePhotoService = livePhotoService;
    }

    @PostMapping(path = "/api/livephoto/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ConversionResponse convert(
            @RequestParam(name = "photo", required = false) MultipartFile photo,
            @RequestParam(name = "video") MultipartFile video,
            @RequestParam(name = "requestId", required = false, defaultValue = "") String requestId,
            @RequestParam(name = "outputFormat", required = false, defaultValue = "mp4") @NotBlank String outputFormat
    ) {
        return livePhotoService.convert(photo, video, requestId, outputFormat);
    }
}
