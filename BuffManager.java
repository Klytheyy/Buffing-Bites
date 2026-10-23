import java.util.Random;

public class BuffManager {

    // A special fruit has ten seconds before it expires.
    private static final long FRUIT_LIFETIME_MS = 10_000;

    private final Random random = new Random();

    // Current special fruit, if one exists.
    private SpecialFruit activeFruit;

    // Time when another special fruit is allowed to spawn.
    private long nextSpawnTime = 0;

    public enum EffectType {
        SPEED_BUFF,
        SLOW_DEBUFF,
        BONUS_SCORE,
        REVERSE_CONTROLS
    }

    // Represents one special fruit on the board.
    public static class SpecialFruit {

        private final int x;
        private final int y;
        private final EffectType effectType;
        private final long spawnTime;

        public SpecialFruit(
                int x,
                int y,
                EffectType effectType,
                long spawnTime) {

            this.x = x;
            this.y = y;
            this.effectType = effectType;
            this.spawnTime = spawnTime;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public EffectType getEffectType() {
            return effectType;
        }

        public long getSpawnTime() {
            return spawnTime;
        }
    }

    // Returns true when a special fruit is allowed to spawn.
    public boolean shouldSpawn(long currentTime) {
        return activeFruit == null && currentTime >= nextSpawnTime;
    }

    // Creates a special fruit at the supplied grid position.
    // GamePanel should make sure the position is not occupied.
    public void spawnSpecialFruit(int x, int y, long currentTime) {
        if (activeFruit != null || currentTime < nextSpawnTime) {
            return;
        }

        EffectType[] effects = EffectType.values();
        EffectType chosenEffect = effects[random.nextInt(effects.length)];

        activeFruit = new SpecialFruit(x, y, chosenEffect, currentTime);
    }

    // Returns the active fruit, or null if there is none.
    public SpecialFruit getActiveFruit() {
        return activeFruit;
    }

    // Checks whether the active fruit has expired.
    // Pass System.currentTimeMillis() as currentTime.
    public void update(long currentTime) {
        if (activeFruit != null
                && currentTime - activeFruit.getSpawnTime() >= FRUIT_LIFETIME_MS) {

            applyRotPenalty(activeFruit.getEffectType());
            activeFruit = null;
            nextSpawnTime = currentTime + 5000 + random.nextInt(10000);
        }
    }

    // Called when the snake collects the active special fruit.
    public void collectSpecialFruit() {
        if (activeFruit != null) {
            applyBuff(activeFruit.getEffectType());
            activeFruit = null;

            long currentTime = System.currentTimeMillis();
            nextSpawnTime = currentTime + 5000 + random.nextInt(10000);
        }
    }

    // TODO: Connect these effects to GamePanel and ScoreManager.
    private void applyBuff(EffectType effectType) {
        switch (effectType) {
            case SPEED_BUFF:
                // TODO: Temporarily increase the snake's movement speed.
                break;

            case BONUS_SCORE:
                // TODO: Ask ScoreManager to award bonus points.
                break;

            case SLOW_DEBUFF:
                // TODO: Temporarily decrease the snake's movement speed.
                break;

            case REVERSE_CONTROLS:
                // TODO: Temporarily reverse the snake's controls.
                break;

            default:
                break;
        }
    }

    // Called when a special fruit remains uneaten for ten seconds.
    private void applyRotPenalty(EffectType effectType) {
        switch (effectType) {
            case SLOW_DEBUFF:
                // TODO: Apply a temporary speed penalty.
                break;

            case REVERSE_CONTROLS:
                // TODO: Apply a temporary reversed-controls penalty.
                break;

            default:
                // TODO: Apply the agreed penalty for allowing a fruit to rot.
                break;
        }
    }

    public boolean hasActiveFruit() {
        return activeFruit != null;
    }

    // Clears the active fruit and schedules the next spawn after five seconds.
    public void reset() {
        activeFruit = null;
        long currentTime = System.currentTimeMillis();
        nextSpawnTime = currentTime + 5000;
    }
}
