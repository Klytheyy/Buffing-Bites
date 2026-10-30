public class ScoreManager {

    private int currentScore = 0;
    private int sessionBestScore = 0;
    private boolean gameOver = false;

    // Adds the normal value of one regular food item.
    public void addFoodPoints() {
        addBonusPoints(10);
    }

    // Also accepts negative points for rotten fruit penalties.
    public void addBonusPoints(int points) {
        if (gameOver) return;

        currentScore = Math.max(0, currentScore + points);
        updateBestScore();
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getSessionBestScore() {
        return sessionBestScore;
    }

    public void endGame() {
        gameOver = true;
    }

    public void resetScore() {
        currentScore = 0;
        gameOver = false;
    }

    private void updateBestScore() {
        if (currentScore > sessionBestScore) {
            sessionBestScore = currentScore;
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
