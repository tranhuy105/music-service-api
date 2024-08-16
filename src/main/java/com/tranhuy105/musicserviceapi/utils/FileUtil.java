package com.tranhuy105.musicserviceapi.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FileUtil {
    public static File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = File.createTempFile("temp", multipartFile.getOriginalFilename());

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }

        return file;
    }

    public static void cleanupFile(File file) {
        if (file.exists()) {
            try {
                boolean deleted = file.delete();
                if (!deleted) {
                    log.error("Failed to delete file: " + file.getAbsolutePath());
                } else {
                    log.info("clean up file: " + file.getAbsolutePath());
                }
            } catch (Exception exception) {
                log.error("Failed to delete file: " + file.getAbsolutePath(), exception);
            }
        }
    }

    public static File reduceAudioQuality(File inputFile) throws IOException {
        File outputFile = File.createTempFile("temp_low_quality", ".mp3");

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", inputFile.getAbsolutePath(),
                "-ab", "128k",
                "-ar", "44100",
                "-y",
                outputFile.getAbsolutePath()
        );

        Process process = processBuilder.start();
        try {
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                throw new IOException("ffmpeg process timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("ffmpeg process was interrupted", e);
        } finally {
            if (process.isAlive() || process.exitValue() != 0) {
                process.destroy();
                if (outputFile.exists()) {
                    boolean deleted = outputFile.delete();
                    if (!deleted) {
                        log.error("Failed to delete incomplete file: " + outputFile.getAbsolutePath());
                    }
                }
            }
        }

        return outputFile;
    }

    public static int getAudioDuration(File file) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", file.getAbsolutePath(),
                "-f", "null",
                "-"
        );

        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Duration:")) {
                    String durationStr = line.split("Duration:")[1].split(",")[0].trim();
                    String[] timeParts = durationStr.split(":");
                    long hours = Long.parseLong(timeParts[0]);
                    long minutes = Long.parseLong(timeParts[1]);
                    double seconds = Double.parseDouble(timeParts[2].replaceAll(",", "."));
                    return (int) ((hours * 3600 + minutes * 60 + seconds) * 1000);
                }
            }
        } catch (IOException e) {
            throw new IOException("Failed to read ffmpeg error stream", e);
        }

        throw new IOException("Failed to extract duration from ffmpeg output");
    }
}
