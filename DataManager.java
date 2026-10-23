import java.util.List;
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

/*
 * TEAM MEMBER: Tantay
 *
 * PURPOSE:
 * Handle persistent game data.
 *
 * RESPONSIBILITIES:
 * 1. Save completed game scores.
 * 2. Load saved scores for the High Scores screen.
 * 3. Keep score data available after the game closes.
 * 4. Handle file/database errors safely.
 *
 * IMPORTANT:
 * Confirm the instructor's required storage technology first.
 * Possible options include CSV, TXT, or a database.
 * Do not choose one until the requirement is confirmed.
 */


//Uses CSV files
public class DataManager {
    // Set the location of the score file
    private static final Path SCORE_FILE = Paths.get("data", "scores.csv");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    
    /*
     * TODO: Implement this method after the storage method is agreed upon.
     *
     * Parameters:
     * playerName - the name associated with the score
     * score      - the final score for the completed game
     *
     * Return:
     * true if saving succeeds; false if it fails.
     */
    
    // Method for saving a player's score
    public boolean saveScore(String playerName, int score) {
        if (playerName == null || playerName.trim().isEmpty() || score < 0) {
            return false;
        }

        try {
            // Get the folder containing the score file
            Path parent = SCORE_FILE.getParent();

            // Create the data folder if it does not exist
            if (parent != null) {
                Files.createDirectories(parent);
            }
            
            // Check whether the file is missing or empty
            // A header is needed only when creating a new CSV file
            boolean writeHeader = !Files.exists(SCORE_FILE) || Files.size(SCORE_FILE) == 0;

            // Open the file for writing without deleting old scores
            // CREATE creates the file if it does not exist
            // APPEND adds new scores to the end of the file
            try (BufferedWriter writer = Files.newBufferedWriter(SCORE_FILE, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

                if (writeHeader) {
                    writer.write("Player,Score,Date");
                    writer.newLine();
                }

                String name = playerName.trim();
                String date = LocalDateTime.now().format(DATE_FORMAT);
                writer.write(name + "," + score + "," + date);
                writer.newLine();
            }
            return true;

        } catch (IOException e) {
            // Display an error if the file cannot be written
            System.out.println("Error saving score: " + e.getMessage());
            return false;
        }
    }

    /*
     * TODO: Load saved scores from the agreed storage system.
     *
     * Coordinate the return format with Ken before implementing
     * the High Scores screen.
     */
    
    // Load saved scores and return the top 10 as formatted text
    public List<String> loadHighScores() {
        // Temporarily store the score records before sorting
        List<ScoreRecord> records = new ArrayList<>();

        if (!Files.exists(SCORE_FILE)) {
            return new ArrayList<>();
        }

        try (BufferedReader reader = Files.newBufferedReader(
                SCORE_FILE, StandardCharsets.UTF_8)) {

            reader.readLine();
            
            String line;
            
             // Read the file one line at a time until there are no more lines
            while ((line = reader.readLine()) != null) {
                try {
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() != 3) {
                        continue;
                    }
                    String name = fields.get(0);
                    int score = Integer.parseInt(fields.get(1));
                    String date = fields.get(2);
                    if (name.trim().isEmpty() || score < 0) {
                        continue;
                    }
                    // Create a score record and add it to the list
                    records.add(new ScoreRecord(name, score, date));
                    
                } catch (IllegalArgumentException e) {
                    // Skip invalid score records.
                }
            }
        } catch (IOException e) {
            // Display an error if the file cannot be read
            System.out.println("Error loading scores: " + e.getMessage());
        }
        
        // Sort scores from highest to lowest
        // If scores are tied, sort by date in ascending order
        records.sort(Comparator.comparingInt(ScoreRecord::getScore).reversed().thenComparing(ScoreRecord::getDate));
        
        List<String> highScores = new ArrayList<>();
        
        // Display no more than 10 scores
        int limit = Math.min(10, records.size());
        
        for (int i = 0; i < limit; i++) {
            ScoreRecord record = records.get(i);
            highScores.add((i + 1) + ". " + record.getPlayerName() + " - " + record.getScore() + " points - " + record.getDate()
            );
        }
        return highScores;
    }
    /*
     * TODO: Decide how the application should handle missing,
     * empty, or unreadable score data.
     */

    // Check whether the program can access the score storage location
    public boolean isStorageAvailable() {
        try {
            // Get the folder containing the score file
            Path parent = SCORE_FILE.getParent();
            
            // If there is no parent folder, check the file directly
            if (parent == null) {
                return Files.exists(SCORE_FILE)
                        && Files.isReadable(SCORE_FILE)
                        && Files.isWritable(SCORE_FILE);
            }

            // Create the data folder if it does not exist
            Files.createDirectories(parent);    
            
            // Confirm that the folder exists and can be written to
            return Files.isDirectory(parent)
                    && Files.isWritable(parent)
                    && (!Files.exists(SCORE_FILE)
                    || (Files.isRegularFile(SCORE_FILE)
                    && Files.isReadable(SCORE_FILE)
                    && Files.isWritable(SCORE_FILE)));

        } catch (IOException e) {
            // Return false if checking or creating storage fails
            return false;
        }
    }
    
    // Reads saved scores from the CSV file
    private List<String> parseCsvLine(String line) {

        // Split the line wherever a comma appears
        String[] fields = line.split(",", -1);

        // Convert the array into a list and return it
        return new ArrayList<>(java.util.Arrays.asList(fields));
    }

    // Starts the score record with its three values
    private static class ScoreRecord {
        private final String playerName;
        private final int score;
        private final String date;

        public ScoreRecord(String playerName, int score, String date) {
            this.playerName = playerName;
            this.score = score;
            this.date = date;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getScore() {
            return score;
        }

        public String getDate() {
            return date;
        }
    }
}
