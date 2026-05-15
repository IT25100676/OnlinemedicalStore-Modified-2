package com.example.OnlineMedicalStore.repository;

import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

abstract class AbstractTextFileRepository<T extends Serializable> {

    @Value("${app.data.dir:data}")
    private String dataDir;

    private final String fileName;

    protected AbstractTextFileRepository(String fileName) {
        this.fileName = fileName;
    }

    public synchronized List<T> findAll() {
        return readAll();
    }

    public synchronized Optional<T> findById(Long id) {
        return readAll().stream()
                .filter(item -> id != null && id.equals(getId(item)))
                .findFirst();
    }

    public synchronized <S extends T> S save(S item) {
        List<T> items = readAll();
        Long id = getId(item);
        if (id == null) {
            setId(item, nextId(items));
        }

        Long savedId = getId(item);
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            if (savedId.equals(getId(items.get(i)))) {
                items.set(i, item);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            items.add(item);
        }
        writeAll(items);
        return item;
    }

    public synchronized void deleteById(Long id) {
        List<T> items = readAll();
        items.removeIf(item -> id != null && id.equals(getId(item)));
        writeAll(items);
    }

    public synchronized long count() {
        return readAll().size();
    }

    private Long nextId(List<T> items) {
        return items.stream()
                .map(this::getId)
                .filter(id -> id != null)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }

    private Path filePath() {
        return Paths.get(dataDir, fileName);
    }

    private List<T> readAll() {
        Path path = filePath();
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            List<T> items = new ArrayList<>();
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    items.add(deserialize(line));
                }
            }
            return items;
        } catch (Exception e) {
            throw new IllegalStateException("Could not read " + path, e);
        }
    }

    private void writeAll(List<T> items) {
        Path path = filePath();
        try {
            Files.createDirectories(path.getParent());
            List<String> lines = new ArrayList<>();
            for (T item : items) {
                lines.add(serialize(item));
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Could not write " + path, e);
        }
    }

    private String serialize(T item) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(item);
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    @SuppressWarnings("unchecked")
    private T deserialize(String line) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(line);
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) in.readObject();
        }
    }

    private Long getId(T item) {
        try {
            Method method = item.getClass().getMethod("getId");
            return (Long) method.invoke(item);
        } catch (Exception e) {
            throw new IllegalStateException("Stored object must have getId()", e);
        }
    }

    private void setId(T item, Long id) {
        try {
            Method method = item.getClass().getMethod("setId", Long.class);
            method.invoke(item, id);
        } catch (Exception e) {
            throw new IllegalStateException("Stored object must have setId(Long)", e);
        }
    }
}
