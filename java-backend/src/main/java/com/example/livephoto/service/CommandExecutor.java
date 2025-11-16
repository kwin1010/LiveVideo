package com.example.livephoto.service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public interface CommandExecutor {
    void run(List<String> command, Duration timeout) throws IOException, InterruptedException;
}
