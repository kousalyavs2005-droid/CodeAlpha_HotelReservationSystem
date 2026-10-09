package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Small File I/O helper: pipe-delimited text files stored in the ./data folder. */
public final class FileStore {
    private static final Path DIR = Paths.get("data");

    private FileStore() { }

    public static boolean exists(String fileName) {
        return Files.exists(DIR.resolve(fileName));
    }

    public static List<String> readLines(String fileName) {
        try {
            Path p = DIR.resolve(fileName);
            if (!Files.exists(p)) return new ArrayList<>();
            return new ArrayList<>(Files.readAllLines(p, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Could not read " + fileName + ": " + e.getMessage(), e);
        }
    }

    public static void writeLines(String fileName, List<String> lines) {
        try {
            Files.createDirectories(DIR);
            Path target = DIR.resolve(fileName);
            Path tmp = DIR.resolve(fileName + ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            // temp file first, so a crash can never leave a half-written data file
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not write " + fileName + ": " + e.getMessage(), e);
        }
    }

    public static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append('|');
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    public static String[] split(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\' && i + 1 < line.length()) {
                char n = line.charAt(++i);
                cur.append(n == 'p' ? '|' : n == 'n' ? '\n' : n);
            } else if (c == '|') {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("|", "\\p").replace("\n", "\\n");
    }
}
