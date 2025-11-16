package com.example.livephoto.service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ProcessCommandExecutor implements CommandExecutor {

    @Override
    public void run(List<String> command, Duration timeout) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();
        boolean finished = process.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("ffmpeg command timed out");
        }
        if (process.exitValue() != 0) {
            String message = new String(process.getErrorStream().readAllBytes());
            throw new IOException("ffmpeg failed: " + message);
        }
    }
}
