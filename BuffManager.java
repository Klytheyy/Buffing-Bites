import java.util.Random;

public class BuffManager {

    private static final long FRUIT_LIFETIME_MS = 10_000;

    private final Random random = new Random();

    private SpecialFruit activeFruit;

    private long nextSpawnTime = 0;

    public enum EffectType {
        SPEED_BUFF,
        SLOW_DEBUFF,
        BONUS_SCORE,
        REVERSE_CONTROLS
    }

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

    public boolean shouldSpawn(long currentTime) {
        return activeFruit == null && currentTime >= nextSpawnTime;
    }

    public void spawnSpecialFruit(int x, int y, long currentTime) {
        if (activeFruit != null || currentTime < nextSpawnTime) {
            return;
        }

        EffectType[] effects = EffectType.values();
        EffectType chosenEffect = effects[random.nextInt(effects.length)];

        activeFruit = new SpecialFruit(x, y, chosenEffect, currentTime);
    }

    public SpecialFruit getActiveFruit() {
        return activeFruit;
    }

    public void update(long currentTime) {
        if (activeFruit != null
                && currentTime - activeFruit.getSpawnTime() >= FRUIT_LIFETIME_MS) {

            applyRotPenalty(activeFruit.getEffectType());
            activeFruit = null;
            nextSpawnTime = currentTime + 5000 + random.nextInt(10000);
        }
    }

    public void collectSpecialFruit() {
        if (activeFruit != null) {
            applyBuff(activeFruit.getEffectType());
            activeFruit = null;

            long currentTime = System.currentTimeMillis();
            nextSpawnTime = currentTime + 5000 + random.nextInt(10000);
        }
    }

    private void applyBuff(EffectType effectType) {
        switch (effectType) {
            case SPEED_BUFF:
                break;

            case BONUS_SCORE:
                break;

            case SLOW_DEBUFF:
                break;

            case REVERSE_CONTROLS:
                break;

            default:
                break;
        }
    }

    private void applyRotPenalty(EffectType effectType) {
        switch (effectType) {
            case SLOW_DEBUFF:
                break;

            case REVERSE_CONTROLS:
                break;

            default:
                break;
        }
    }

    public boolean hasActiveFruit() {
        return activeFruit != null;
    }

    public void reset() {
        activeFruit = null;
        long currentTime = System.currentTimeMillis();
        nextSpawnTime = currentTime + 5000;
    }
}
