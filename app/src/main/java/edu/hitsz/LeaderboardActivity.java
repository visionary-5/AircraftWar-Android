package edu.hitsz;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import edu.hitsz.dao.ScoreDaoSQLiteImpl;
import edu.hitsz.dto.ScoreRecord;

public class LeaderboardActivity extends AppCompatActivity {
    public static final String EXTRA_DIFFICULTY = "difficulty";

    private ScoreDaoSQLiteImpl scoreDao;
    private ListView listView;
    private String difficulty;
    private List<ScoreRecord> records;
    private ScoreAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        if (difficulty == null) difficulty = "EASY";

        scoreDao = new ScoreDaoSQLiteImpl(this);
        listView = findViewById(R.id.lv_scores);

        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("排行榜 - " + difficulty);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        records = scoreDao.getAllScores(difficulty);
        adapter = new ScoreAdapter(this);
        listView.setAdapter(adapter);
    }

    private class ScoreAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;

        ScoreAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return records.size();
        }

        @Override
        public Object getItem(int position) {
            return records.get(position);
        }

        @Override
        public long getItemId(int position) {
            return records.get(position).getRowId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_score, null);
                holder = new ViewHolder();
                holder.tvRank = convertView.findViewById(R.id.tv_rank);
                holder.tvName = convertView.findViewById(R.id.tv_name);
                holder.tvScore = convertView.findViewById(R.id.tv_score);
                holder.tvTime = convertView.findViewById(R.id.tv_time);
                holder.btnDelete = convertView.findViewById(R.id.btn_delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ScoreRecord record = records.get(position);
            holder.tvRank.setText("#" + record.getRank());
            holder.tvName.setText(record.getPlayerName());
            holder.tvScore.setText(String.valueOf(record.getScore()));
            holder.tvTime.setText(record.getRecordTime());
            holder.btnDelete.setFocusable(false);
            holder.btnDelete.setOnClickListener(v -> {
                scoreDao.deleteScoreById(record.getRowId());
                records = scoreDao.getAllScores(difficulty);
                notifyDataSetChanged();
            });

            return convertView;
        }
    }

    private static class ViewHolder {
        TextView tvRank, tvName, tvScore, tvTime;
        Button btnDelete;
    }
}
