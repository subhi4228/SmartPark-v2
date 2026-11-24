package com.smartpark.util;

import java.io.*;
import java.util.*;

/**
 * Generic repository for local CSV persistence, offline backup, and pending sync queue.
 * Utilizes try-with-resources to guarantee safe file handle management.
 */
public class FileRepository<T extends CsvConvertible> {
    private final File file;

    public FileRepository(File file) {
        this.file = file;
        ensureParentExists();
    }

    private void ensureParentExists() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    // Time O(1), Space O(1)
    public synchronized void append(T item) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(item.toCsvRow());
            bw.newLine();
        }
    }

    // Time O(n), Space O(n)
    public synchronized void saveAll(List<T> items, String header) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            if (header != null && !header.isEmpty()) {
                bw.write(header);
                bw.newLine();
            }
            for (T item : items) {
                bw.write(item.toCsvRow());
                bw.newLine();
            }
        }
    }

    // Time O(n), Space O(n)
    public synchronized List<String> readAllLines() throws IOException {
        if (!file.exists()) {
            return Collections.emptyList();
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    public boolean exists() {
        return file.exists();
    }
}
