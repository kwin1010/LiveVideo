package com.example.livephoto.service;

import com.example.livephoto.config.StorageProperties;
import com.example.livephoto.model.ConversionResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class LivePhotoService {

    private final StorageProperties storageProperties;
    private final CommandExecutor commandExecutor;

    public LivePhotoService(StorageProperties storageProperties, CommandExecutor commandExecutor) {
        this.storageProperties = storageProperties;
        this.commandExecutor = commandExecutor;
    }

    public ConversionResponse convert(MultipartFile photo, MultipartFile video, String requestId, String outputFormat) {
        String traceId = requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId;
        String sanitizedFormat = StringUtils.trimAllWhitespace(outputFormat).toLowerCase(Locale.ROOT);
        try {
            Path root = Files.createDirectories(Path.of(storageProperties.getStorageRoot()));
            Path workingDir = Files.createDirectories(root.resolve(traceId));

            Path videoPath = storeFile(video, workingDir, "video");
            Path photoPath = photo != null && !photo.isEmpty() ? storeFile(photo, workingDir, "photo") : null;

            Path outputPath = workingDir.resolve("livephoto-converted." + sanitizedFormat);

            List<String> command = buildCommand(videoPath, photoPath, outputPath, sanitizedFormat);
            commandExecutor.run(command, Duration.ofSeconds(30));

            return new ConversionResponse(traceId, sanitizedFormat, outputPath.toAbsolutePath().toString());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Conversion interrupted: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to convert Live Photo: " + ex.getMessage(), ex);
        }
    }

    private Path storeFile(MultipartFile file, Path workingDir, String prefix) throws IOException {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String sanitizedExt = extension != null ? extension : "bin";
        Path target = workingDir.resolve(prefix + "." + sanitizedExt);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    private List<String> buildCommand(Path videoPath, Path photoPath, Path outputPath, String outputFormat) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-i");
        command.add(videoPath.toString());

        if (photoPath != null) {
            command.add("-i");
            command.add(photoPath.toString());
            command.add("-filter_complex");
            command.add("[0:v][1:v]overlay=0:0:enable='between(t,0,1)'" );
        }

        command.add("-preset");
        command.add("fast");
        command.add(outputPath.toString());
        return command;
    }
}
