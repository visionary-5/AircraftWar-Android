package edu.hitsz.dao;

import edu.hitsz.dto.ScoreRecord;
import java.util.List;

/**
 * 得分数据访问对象接口
 * @author hitsz
 */
public interface ScoreDao {
    /**
     * 获取所有得分记录
     * @param difficulty 游戏难度
     * @return 得分记录列表
     */
    List<ScoreRecord> getAllScores(String difficulty);

    /**
     * 插入得分记录
     * @param record 得分记录
     * @param difficulty 游戏难度
     */
    void insertScore(ScoreRecord record, String difficulty);

    /**
     * 删除得分记录
     * @param playerName 玩家名称
     * @param difficulty 游戏难度
     */
    void deleteScore(String playerName, String difficulty);

    /**
     * 更新得分记录
     * @param record 得分记录
     * @param difficulty 游戏难度
     */
    void updateScore(ScoreRecord record, String difficulty);

    /**
     * 获取前N名得分记录
     * @param difficulty 游戏难度
     * @param limit 返回记录数量
     * @return 得分记录列表
     */
    List<ScoreRecord> getTopScores(String difficulty, int limit);
}

