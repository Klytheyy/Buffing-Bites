public class ScoreManager {

    // TODO: Agree on the points awarded for each action.
    private int currentScore = 0;

    // Best score reached during the current application session.
    private int sessionBestScore = 0;

    // TODO: Decide whether score can ever decrease.
    private boolean gameOver = false;

    /*
     * TODO: Add points when the snake eats regular food.
     * Suggested starting value: 10 points.
     */
    public void addFoodPoints() {
        if (gameOver) {
            return;
        }

        currentScore += 10; //tentative - depende sa fruit
        updateBestScore(); 
    }

    /*
     * TODO: Add bonus points from Dacanay's special-fruit effects.
     * The points argument allows different fruits to award different amounts.
     */
    public void addBonusPoints(int points) {
        if (gameOver || points <= 0) {
            return;
        }

        currentScore += points;
        updateBestScore();    
    }

    /*
     * TODO: Return the current score for the game screen.
     */
    public int getCurrentScore() {
        return currentScore;
    }

    /*
     * TODO: Return the best score reached during this session.
     */
    public int getSessionBestScore() {
        return sessionBestScore;
    }

    /*
     * TODO: Mark the current round as over.
     *
     * Coordinate with Ken on displaying the game-over screen
     * and with Tantay on saving the final score.
     */
    public void endGame() {
         gameOver = true;
    }

    /*
     * TODO: Reset the current score and game-over status for a new round.
     * Keep the session best score unless the group decides otherwise.
     */
    public void resetScore() {
        currentScore = 0;
        gameOver = false;
    }

    /*
     * TODO: Update sessionBestScore whenever currentScore exceeds it.
     * Call this after changing the score.
     */
    private void updateBestScore() {
         if (currentScore > sessionBestScore) {
            sessionBestScore = currentScore;
        }
    }

    /*
     * TODO: Return whether the current round has ended.
     */
    public boolean isGameOver() {
        return gameOver;
    }
}

