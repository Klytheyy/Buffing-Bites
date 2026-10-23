/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.buffingbites;

import java.util.Random;

/*
 * TEAM MEMBER: Dacanay
 *
 * PURPOSE:
 * Manage special fruits and their temporary effects.
 *
 * RESPONSIBILITIES:
 * 1. Decide when special fruits appear.
 * 2. Choose a special-fruit type.
 * 3. Track the 10-second expiration time.
 * 4. Apply a buff when the snake eats the fruit.
 * 5. Apply a debuff when the fruit rots.
 *
 * COORDINATION:
 * Yee owns snake movement and the regular food system.
 * Do not create another game loop in this class.
 */
public class BuffManager {

    // A special fruit has ten seconds before it expires.
    private static final long FRUIT_LIFETIME_MS = 10_000;

    private final Random random = new Random();

    // TODO: Store the current special fruit, if one exists.
    private SpecialFruit activeFruit;

    // TODO: Add the next spawn time if using scheduled spawning.
    private long nextSpawnTime;

    /*
     * Keep the list of effect types in one place.
     * Discuss the final effects with the group before implementing them.
     */
    public enum EffectType {
        SPEED_BUFF,
        SLOW_DEBUFF,
        BONUS_SCORE,
        REVERSE_CONTROLS
    }

    /*
     * Represents one special fruit on the board.
     * You may expand this class if the design needs more information.
     */
    public static class SpecialFruit {

        private int x;
        private int y;
        private EffectType effectType;
        private long spawnTime;

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

    /*
     * TODO: Decide whether a new special fruit should spawn.
     * The calling game screen can check this during its update cycle.
     */
    public boolean shouldSpawn(long currentTime) {
        return activeFruit == null && currentTime >= nextSpawnTime;
    }

    /*
     * TODO: Spawn a special fruit at a valid board location.
     * Avoid placing it on top of the snake or regular food.
     *
     * Coordinate the board-position checks with Yee.
     */
    public void spawnSpecialFruit(
            int x,
            int y,
            long currentTime) {
        EffectType[] effects = EffectType.values();
        EffectType chosenEffect = effects[random.nextInt(effects.length)];
        
        activeFruit = new SpecialFruit(x, y, chosenEffect, currentTime);
    }

    /*
     * TODO: Return the active fruit so GamePanel can draw it.
     * Return null if no special fruit is active.
     */
    public SpecialFruit getActiveFruit() {
        return activeFruit;
    }

    /*
     * TODO: Check whether the active fruit has expired.
     * If ten seconds have passed:
     * 1. Remove the fruit.
     * 2. Apply its assigned rot penalty/debuff.
     * 3. Notify the game screen if needed.
     */
    public void update(long currentTime) {
        if (activeFruit != null) {
            if (currentTime - activeFruit.getSpawnTime() >= FRUIT_LIFETIME_MS) {
                applyRotPenalty(activeFruit.getEffectType());
                activeFruit = null;
                nextSpawnTime = currentTime + 5000 + random.nextInt(10000);
            }
        }
    }

    /*
     * TODO: Check whether the snake has eaten the special fruit.
     * If eaten:
     * 1. Apply the fruit's buff.
     * 2. Remove the fruit.
     * 3. Prevent its expiration penalty from also triggering.
     */
    public void collectSpecialFruit() {
        if (activeFruit != null) {
            applyBuff(activeFruit.getEffectType());
            activeFruit = null;
            nextSpawnTime = currentTime + 5000 + random.nextInt(10000);
        }
    }

    /*
     * TODO: Apply the selected effect to the game.
     *
     * Examples to discuss with the group:
     * SPEED_BUFF: temporarily increase movement speed.
     * SLOW_DEBUFF: temporarily reduce movement speed.
     * BONUS_SCORE: award extra points.
     * REVERSE_CONTROLS: temporarily reverse directions.
     *
     * Agree with Yee on how effects change movement speed or controls.
     */
    private void applyBuff(EffectType effectType) {
        switch (effectType) {
            case SPEED_BUFF -> { /* Increase snake speed */ }
            case BONUS_SCORE -> { /* Add extra points to score */ }
            case SLOW_DEBUFF -> { /* Decrease snake speed */ }
            case REVERSE_CONTROLS -> { /* Opposite controls */ }
    }

    /*
     * TODO: Apply the penalty for allowing a special fruit to rot.
     * Decide whether each fruit has its own penalty or uses one common
     * debuff. Coordinate the rule with the whole team.
     */
    private void applyRotPenalty(EffectType effectType) {
        switch (effectType) {
            case SLOW_DEBUFF -> { /* Decrease snake speed */ }
            case REVERSE_CONTROLS -> { /* Opposite controls */ }
            default -> { /* Default score penalty for missing regular buffs */ }
    }

    /*
     * TODO: Return whether a special fruit is currently active.
     */
    public boolean hasActiveFruit() {
        return activeFruit != null;
    }

    /*
     * TODO: Clear the active fruit and reset temporary effects
     * when the game restarts.
     */
    public void reset() {
        activeFruit = null;
        nextSpawnTime = currentTime + 5000;
    }
}

