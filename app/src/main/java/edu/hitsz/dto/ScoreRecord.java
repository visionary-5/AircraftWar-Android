package edu.hitsz.dto;

/**
 * 得分记录数据传输对象
 * @author hitsz
 */
public class ScoreRecord {
    private int rank;
    private final String playerName;
    private final int score;
    private final String recordTime;

    public ScoreRecord(String playerName, int score, String recordTime) {
        this.playerName = playerName;
        this.score = score;
        this.recordTime = recordTime;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public String getRecordTime() {
        return recordTime;
    }

    @Override
    public String toString() {
        return "第" + rank + "名: " + playerName + "," + score + "," + recordTime;
    }
}

