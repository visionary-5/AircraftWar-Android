package edu.hitsz.dao;

import edu.hitsz.dto.ScoreRecord;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 得分数据访问对象实现类
 * @author hitsz
 */
public class ScoreDaoImpl implements ScoreDao {
    private final List<ScoreRecord> records;
    private static final String FILE_PATH_PREFIX = "scores_";
    private static final String FILE_PATH_SUFFIX = ".txt";

    public ScoreDaoImpl() {
        this.records = new ArrayList<>();
    }

    @Override
    public List<ScoreRecord> getAllScores(String difficulty) {
        loadFromFile(difficulty);
        // 按分数降序排序
        records.sort(Comparator.comparingInt(ScoreRecord::getScore).reversed());
        // 设置排名
        for (int i = 0; i < records.size(); i++) {
            records.get(i).setRank(i + 1);
        }
        return new ArrayList<>(records);
    }

    @Override
    public void insertScore(ScoreRecord record, String difficulty) {
        loadFromFile(difficulty);
        records.add(record);
        // 按分数降序排序
        records.sort(Comparator.comparingInt(ScoreRecord::getScore).reversed());
        // 设置排名
        for (int i = 0; i < records.size(); i++) {
            records.get(i).setRank(i + 1);
        }
        saveToFile(difficulty);
    }

    @Override
    public void deleteScore(String playerName, String difficulty) {
        // 此方法保留用于兼容，删除该玩家的所有记录
        loadFromFile(difficulty);
        records.removeIf(record -> record.getPlayerName().equals(playerName));
        // 重新设置排名
        records.sort(Comparator.comparingInt(ScoreRecord::getScore).reversed());
        for (int i = 0; i < records.size(); i++) {
            records.get(i).setRank(i + 1);
        }
        saveToFile(difficulty);
    }

    /**
     * 删除指定的得分记录（精确匹配）
     * @param playerName 玩家名称
     * @param score 分数
     * @param recordTime 记录时间
     * @param difficulty 游戏难度
     */
    public void deleteScoreByDetails(String playerName, int score, String recordTime, String difficulty) {
        loadFromFile(difficulty);
        // 通过玩家名、分数和时间三个字段精确匹配，只删除选中的那一条记录
        records.removeIf(record ->
            record.getPlayerName().equals(playerName) &&
            record.getScore() == score &&
            record.getRecordTime().equals(recordTime));
        // 重新设置排名
        records.sort(Comparator.comparingInt(ScoreRecord::getScore).reversed());
        for (int i = 0; i < records.size(); i++) {
            records.get(i).setRank(i + 1);
        }
        saveToFile(difficulty);
    }

    @Override
    public void updateScore(ScoreRecord record, String difficulty) {
        loadFromFile(difficulty);
        // 先删除旧记录，再添加新记录
        records.removeIf(r -> r.getPlayerName().equals(record.getPlayerName()));
        records.add(record);
        // 按分数降序排序
        records.sort(Comparator.comparingInt(ScoreRecord::getScore).reversed());
        // 设置排名
        for (int i = 0; i < records.size(); i++) {
            records.get(i).setRank(i + 1);
        }
        saveToFile(difficulty);
    }

    @Override
    public List<ScoreRecord> getTopScores(String difficulty, int limit) {
        loadFromFile(difficulty);
        // 按分数降序排序
        records.sort(Comparator.comparingInt(ScoreRecord::getScore).reversed());
        // 设置排名并返回前N条
        List<ScoreRecord> topRecords = records.stream()
                .limit(limit)
                .collect(Collectors.toList());
        for (int i = 0; i < topRecords.size(); i++) {
            topRecords.get(i).setRank(i + 1);
        }
        return topRecords;
    }

    /**
     * 从文件加载数据
     * @param difficulty 游戏难度
     */
    private void loadFromFile(String difficulty) {
        records.clear();
        String filePath = FILE_PATH_PREFIX + difficulty + FILE_PATH_SUFFIX;
        File file = new File(filePath);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 文件格式：playerName,score,recordTime
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String playerName = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    String recordTime = parts[2];
                    records.add(new ScoreRecord(playerName, score, recordTime));
                }
            }
        } catch (IOException e) {
            System.err.println("读取得分文件失败: " + e.getMessage());
        }
    }

    /**
     * 保存数据到文件
     * @param difficulty 游戏难度
     */
    private void saveToFile(String difficulty) {
        String filePath = FILE_PATH_PREFIX + difficulty + FILE_PATH_SUFFIX;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (ScoreRecord record : records) {
                // 文件格式：playerName,score,recordTime
                writer.write(record.getPlayerName() + "," +
                           record.getScore() + "," +
                           record.getRecordTime());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("保存得分文件失败: " + e.getMessage());
        }
    }
}
