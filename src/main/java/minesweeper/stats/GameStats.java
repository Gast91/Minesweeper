package minesweeper.stats;

import minesweeper.difficulty.DifficultyPreset;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.util.Optional;

public class GameStats implements Serializable {

    private static final String path = "Stats.mine";
    private static GameStats instance = null;

    private DifficultyStats beginnerStats = new DifficultyStats();
    private DifficultyStats intermediateStats = new DifficultyStats();
    private DifficultyStats expertStats = new DifficultyStats();

    private GameStats() {
        load();
    }

    public static GameStats getInstance() {
        if (instance == null)
            instance = new GameStats();

        return instance;
    }

    public DifficultyStats getBeginnerStats() {
        return beginnerStats;
    }

    public DifficultyStats getIntermediateStats() {
        return intermediateStats;
    }

    public DifficultyStats getExpertStats() {
        return expertStats;
    }

    public DifficultyStats getStats(DifficultyPreset difficultyPreset) {
        return switch (difficultyPreset) {
            case BEGINNER -> beginnerStats;
            case INTERMEDIATE -> intermediateStats;
            case EXPERT -> expertStats;
            default -> null;
        };
    }

    public void save() {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(path));
            objectOutputStream.writeObject(this);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException ignored) {}
    }

    public void load() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(path));
            instance = (GameStats) objectInputStream.readObject();
            objectInputStream.close();
            beginnerStats = instance.beginnerStats;
            intermediateStats = instance.intermediateStats;
            expertStats = instance.expertStats;
        } catch (IOException | ClassNotFoundException e) {
            resetStats();
        }
    }

    public void updateStats(boolean won, DifficultyPreset difficulty, Duration timeTaken) {
        if (difficulty == DifficultyPreset.CUSTOM) return;

        Optional<DifficultyStats> difficultyStats = Optional.empty();
        switch (difficulty) {
            case BEGINNER -> difficultyStats = Optional.of(beginnerStats);
            case INTERMEDIATE -> difficultyStats = Optional.of(intermediateStats);
            case EXPERT -> difficultyStats = Optional.of(expertStats);
        }

        difficultyStats.ifPresent(stats -> {
            stats.incrementGamesPlayed();
            if (won) {
                stats.incrementGamesWon();
                if (isBestTime(timeTaken, stats.getBestTime())) {
                    stats.setBestTime(timeTaken);
                }
            }
            save();
        });
    }

    public void resetStats(DifficultyPreset difficulty) {
        switch (difficulty) {
            case BEGINNER -> beginnerStats.reset();
            case INTERMEDIATE -> intermediateStats.reset();
            case EXPERT -> expertStats.reset();
        }
        save();
    }

    public void resetStats() {
        beginnerStats.reset();
        intermediateStats.reset();
        expertStats.reset();
        save();
    }

    private boolean isBestTime(Duration currentTimeTaken, Duration previousBestTime) {
        if (previousBestTime == Duration.ZERO) return true;
        return currentTimeTaken.compareTo(previousBestTime) < 0;
    }
}
