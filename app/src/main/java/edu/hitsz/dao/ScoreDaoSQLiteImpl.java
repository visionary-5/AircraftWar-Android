package edu.hitsz.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.dto.ScoreRecord;

/**
 * 使用 SQLite 存储得分数据的 DAO 实现
 */
public class ScoreDaoSQLiteImpl implements ScoreDao {
    private final ScoreDBHelper dbHelper;

    public ScoreDaoSQLiteImpl(Context context) {
        this.dbHelper = new ScoreDBHelper(context.getApplicationContext());
    }

    @Override
    public List<ScoreRecord> getAllScores(String difficulty) {
        List<ScoreRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ScoreDBHelper.TABLE_NAME, null,
                "difficulty=?", new String[]{difficulty},
                null, null, "score DESC");
        int rank = 1;
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String playerName = cursor.getString(cursor.getColumnIndexOrThrow("player_name"));
            int score = cursor.getInt(cursor.getColumnIndexOrThrow("score"));
            String recordTime = cursor.getString(cursor.getColumnIndexOrThrow("record_time"));
            ScoreRecord record = new ScoreRecord(playerName, score, recordTime);
            record.setRowId(id);
            record.setRank(rank++);
            records.add(record);
        }
        cursor.close();
        return records;
    }

    @Override
    public void insertScore(ScoreRecord record, String difficulty) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("player_name", record.getPlayerName());
        values.put("score", record.getScore());
        values.put("record_time", record.getRecordTime());
        values.put("difficulty", difficulty);
        db.insert(ScoreDBHelper.TABLE_NAME, null, values);
    }

    @Override
    public void deleteScore(String playerName, String difficulty) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(ScoreDBHelper.TABLE_NAME, "player_name=? AND difficulty=?",
                new String[]{playerName, difficulty});
    }

    public void deleteScoreById(long rowId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(ScoreDBHelper.TABLE_NAME, "id=?", new String[]{String.valueOf(rowId)});
    }

    @Override
    public void updateScore(ScoreRecord record, String difficulty) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("score", record.getScore());
        values.put("record_time", record.getRecordTime());
        db.update(ScoreDBHelper.TABLE_NAME, values,
                "player_name=? AND difficulty=?",
                new String[]{record.getPlayerName(), difficulty});
    }

    @Override
    public List<ScoreRecord> getTopScores(String difficulty, int limit) {
        List<ScoreRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ScoreDBHelper.TABLE_NAME, null,
                "difficulty=?", new String[]{difficulty},
                null, null, "score DESC", String.valueOf(limit));
        int rank = 1;
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String playerName = cursor.getString(cursor.getColumnIndexOrThrow("player_name"));
            int score = cursor.getInt(cursor.getColumnIndexOrThrow("score"));
            String recordTime = cursor.getString(cursor.getColumnIndexOrThrow("record_time"));
            ScoreRecord record = new ScoreRecord(playerName, score, recordTime);
            record.setRowId(id);
            record.setRank(rank++);
            records.add(record);
        }
        cursor.close();
        return records;
    }
}
