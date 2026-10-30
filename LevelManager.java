public class LevelManager {

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    private Difficulty currentDifficulty = Difficulty.MEDIUM;

    public void setDifficulty(Difficulty difficulty) {
        currentDifficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return currentDifficulty;
    }

    public int getTimerDelay() {
        switch (currentDifficulty) {
            case EASY:
                return 150;
            case MEDIUM:
                return 100;
            case HARD:
                return 65;
            default:
                return 100;
        }
    }

    public String getDescription() {
        switch (currentDifficulty) {
            case EASY:
                return "Slower snake speed. Great for beginners!";
            case MEDIUM:
                return "Balanced speed for a normal challenge.";
            case HARD:
                return "Fast snake speed. For experienced players!";
            default:
                return "";
        }
    }
}

