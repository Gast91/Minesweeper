package minesweeper.stats;

import java.io.Serializable;
import java.time.Duration;

public class DifficultyStats implements Serializable {

    private static final String baseStatsText = """
                                           Games Played:\t\s\s\s\s%d
                                           Games Won:\t\s\s\s\s%d
                                           Win Percentage:\s\s\s%.2f%%
                                           Best Time:\t\s\s\s\s%02d:%02d""";
    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private double winPercent = 0.0;
    private Duration bestTime = Duration.ZERO;

    public DifficultyStats(int gamesPlayed, int gamesWon, double winPercent, Duration bestTime) {
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.winPercent = winPercent;
        this.bestTime = bestTime;
    }

    public DifficultyStats() {
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void incrementGamesPlayed() {
        ++gamesPlayed;
        winPercent = ((double) gamesWon/ (double) gamesPlayed) * 100;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void incrementGamesWon() {
        ++gamesWon;
        winPercent = ((double) gamesWon/ (double) gamesPlayed) * 100;
    }

    public double getWinPercent() {
        return winPercent;
    }

    public Duration getBestTime() {
        return bestTime;
    }

    public void setBestTime(Duration bestTime) {
        this.bestTime = bestTime;
    }

    public void reset() {
        gamesPlayed = 0;
        gamesWon = 0;
        winPercent = 0.0;
        bestTime = Duration.ZERO;
    }

    @Override
    public String toString() {
        return String.format(baseStatsText,
                gamesPlayed,
                gamesWon,
                winPercent,
                bestTime.toMinutesPart(),
                bestTime.toSecondsPart());
    }
}
