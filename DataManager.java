import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DataManager {

    // SINGLETON PATTERN: only one DataManager object is shared by the game.
    // This keeps score-file access centralized instead of creating separate managers.
    private static final DataManager INSTANCE = new DataManager();

    // The private constructor prevents other classes from using "new DataManager()".
    DataManager() { }

    // Other classes call this method to get the one shared DataManager object.
    public static DataManager getInstance() {
        return INSTANCE;
    }

    private static final Path SCORE_FILE = Paths.get("data", "scores.csv");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean saveScore(String playerName, int score) {
        if (playerName == null || playerName.trim().isEmpty() || score < 0) {
            return false;
        }
        try {
            Files.createDirectories(SCORE_FILE.getParent());
            boolean writeHeader = !Files.exists(SCORE_FILE) || Files.size(SCORE_FILE) == 0;
            try (BufferedWriter writer = Files.newBufferedWriter(
                    SCORE_FILE, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                if (writeHeader) {
                    writer.write("Player,Score,Date");
                    writer.newLine();
                }
                String date = LocalDateTime.now().format(DATE_FORMAT);
                writer.write(csv(playerName.trim()) + "," + score + "," + csv(date));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error saving score: " + e.getMessage());
            return false;
        }
    }

    // Returns the ten highest scores for screens that only need a leaderboard preview.
    public List<String> loadHighScores() {
        List<String> allScores = loadAllHighScores();
        return new ArrayList<>(allScores.subList(0, Math.min(10, allScores.size())));
    }

    // Returns every saved score, sorted from highest to lowest.
    public List<String> loadAllHighScores() {
        List<ScoreRecord> records = readRecords();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            ScoreRecord record = records.get(i);
            result.add((i + 1) + ". " + record.playerName + " - "
                    + record.score + " points - " + record.date);
        }
        return result;
    }

    // Deletes the selected row using its position in the complete sorted score list.
    public boolean deleteHighScoreAt(int sortedIndex) {
        List<ScoreRecord> records = readRecords();
        if (sortedIndex < 0 || sortedIndex >= records.size()) {
            return false;
        }
        records.remove(sortedIndex);
        return writeRecords(records);
    }

    private List<ScoreRecord> readRecords() {
        List<ScoreRecord> records = new ArrayList<>();
        if (!Files.exists(SCORE_FILE)) return records;

        try (BufferedReader reader = Files.newBufferedReader(SCORE_FILE, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // CSV header
            while ((line = reader.readLine()) != null) {
                try {
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() != 3) continue;
                    String name = fields.get(0).trim();
                    int score = Integer.parseInt(fields.get(1).trim());
                    String date = fields.get(2).trim();
                    if (!name.isEmpty() && score >= 0) {
                        records.add(new ScoreRecord(name, score, date));
                    }
                } catch (IllegalArgumentException ignored) {
                    // Skip malformed rows but keep valid saved scores.
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading scores: " + e.getMessage());
        }
        records.sort(Comparator.comparingInt((ScoreRecord r) -> r.score)
                .reversed().thenComparing(r -> r.date));
        return records;
    }

    private boolean writeRecords(List<ScoreRecord> records) {
        try {
            Files.createDirectories(SCORE_FILE.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(
                    SCORE_FILE, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.write("Player,Score,Date");
                writer.newLine();
                for (ScoreRecord record : records) {
                    writer.write(csv(record.playerName) + "," + record.score + "," + csv(record.date));
                    writer.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error updating scores: " + e.getMessage());
            return false;
        }
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // Handles quoted CSV values, including player names containing commas.
    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                fields.add(value.toString());
                value.setLength(0);
            } else {
                value.append(ch);
            }
        }
        fields.add(value.toString());
        return fields;
    }

    public boolean isStorageAvailable() {
        try {
            Files.createDirectories(SCORE_FILE.getParent());
            return Files.isDirectory(SCORE_FILE.getParent())
                    && Files.isWritable(SCORE_FILE.getParent())
                    && (!Files.exists(SCORE_FILE)
                    || (Files.isRegularFile(SCORE_FILE)
                    && Files.isReadable(SCORE_FILE) && Files.isWritable(SCORE_FILE)));
        } catch (IOException e) {
            return false;
        }
    }

    private static class ScoreRecord {
        private final String playerName;
        private final int score;
        private final String date;

        private ScoreRecord(String playerName, int score, String date) {
            this.playerName = playerName;
            this.score = score;
            this.date = date;
        }
    }
}
