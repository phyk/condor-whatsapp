package com.company;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class DbExtractor
{
    public static void extractDbToDirectory(String pathToBackup, String directoryToCopyTo) throws IOException {
        Path source = findFileInBackup(pathToBackup);
        Path target = Paths.get(directoryToCopyTo+"/msgstore.db");
        Files.copy(source, target);
    }

    private static Path findFileInBackup(String pathToBackup) throws IOException {
        Stream<Path> stream = Files.find(Paths.get(pathToBackup), 100, (path, basicFileAttributes) -> {
            Path file = path.getFileName();
            return file.endsWith("1b6b187a1b60b9ae8b720c79e2c67f472bab09c0") ||
                    file.endsWith("275ee4a160b7a7d60825a46b0d3ff0dcdb2fbc9d") ||
                    file.endsWith("7c7fba66680ef796b916b067077cc246adacf01d");
        });
        return stream.findAny().get();
    }

    public static void extractEncryptedDbAndKeyFile(String pathToDb, String pathToKey, String enc_db, String key) throws IOException {
        Path source = Paths.get(pathToDb);
        Path target = Paths.get(enc_db);
        Files.copy(source, target);

        source = Paths.get(pathToKey);
        target = Paths.get(key);
        Files.copy(source, target);
    }
}
