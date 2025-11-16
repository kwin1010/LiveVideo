package com.example.livephoto;

import com.example.livephoto.config.StorageProperties;
import com.example.livephoto.model.ConversionResponse;
import com.example.livephoto.service.CommandExecutor;
import com.example.livephoto.service.LivePhotoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LivePhotoServiceTests {

    @TempDir
    Path tempDir;

    private StorageProperties properties;

    @BeforeEach
    void setup() {
        properties = new StorageProperties();
        properties.setStorageRoot(tempDir.toString());
    }

    @Test
    void convertBuildsFfmpegCommandWithPhoto() {
        RecordingExecutor executor = new RecordingExecutor();
        LivePhotoService service = new LivePhotoService(properties, executor);

        MockMultipartFile photo = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MockMultipartFile video = new MockMultipartFile("video", "clip.mov", "video/quicktime", new byte[]{4, 5, 6});

        ConversionResponse response = service.convert(photo, video, "req-1", "mp4");

        assertThat(Files.exists(Path.of(response.outputPath()))).isTrue();
        assertThat(executor.lastCommand).contains("-filter_complex");
        assertThat(executor.lastCommand).anyMatch(arg -> arg.endsWith("livephoto-converted.mp4"));
    }

    @Test
    void convertWithoutPhotoSkipsOverlay() {
        RecordingExecutor executor = new RecordingExecutor();
        LivePhotoService service = new LivePhotoService(properties, executor);

        MockMultipartFile video = new MockMultipartFile("video", "clip.mov", "video/quicktime", new byte[]{4, 5, 6});

        service.convert(null, video, "req-2", "mp4");

        assertThat(executor.lastCommand).doesNotContain("-filter_complex");
    }

    private static class RecordingExecutor implements CommandExecutor {
        private List<String> lastCommand;

        @Override
        public void run(List<String> command, Duration timeout) throws IOException {
            this.lastCommand = List.copyOf(command);
        }
    }
}
