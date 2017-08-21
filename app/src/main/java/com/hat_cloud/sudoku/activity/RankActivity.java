package com.hat_cloud.sudoku.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hat_cloud.sudoku.entry.Rank;
import com.hat_cloud.sudoku.iface.IGame;
import com.hat_cloud.sudoku.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 游戏排名
 */
public class RankActivity extends BaseActivity {
    static final String TAG= "RankActivity1";
    private List<Rank> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        ListView lv = (ListView) findViewById(R.id.lv);
       SharedPreferences preferences =  getSharedPreferences("rank",MODE_ENABLE_WRITE_AHEAD_LOGGING );
        Map<String ,String> map = (Map<String, String>) preferences.getAll();
        Log.i(TAG, "map: "+map);
        if(map==null||map.size()==0){
            showToast(R.string.no_rank);
            return;
        }
        for(String key:map.keySet()){
            Log.i(TAG, "key: "+key);
            Log.i(TAG, "value: "+map.get(key));
            if(Rank.isValid(map.get(key))) {
                list.add(new Rank(map.get(key)));
            }
        }
        Log.i(TAG, "onCreate: "+list);
        lv.setAdapter(new MyRankAdapter());

    }
    class MyRankAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
           ViewHolder holer = null;
            if (view == null) {
                view = LayoutInflater.from(RankActivity.this).inflate(R.layout.rank_item, null, false);
                holer = new ViewHolder();
                holer.name = (TextView) view.findViewById(R.id.name);
                holer.time = (TextView) view.findViewById(R.id.time);
                holer.type = (TextView) view.findViewById(R.id.type);
                view.setTag(holer);
            } else {
                holer = (ViewHolder) view.getTag();
            }
            holer.name.setText(list.get(i).getName());
            holer.time.setText(list.get(i).getTime());
            switch (list.get(i).getType()){
                case IGame.GAME_PK_TIME:
                    holer.type.setText(R.string.pk_type_time);
                    break;
                case IGame.GAME_PK_COMPERTITION:
                    holer.type.setText(R.string.pk_type_comp);
                    break;
                case IGame.GAME_PK_TCOMMUNICATION:
                    holer.type.setText(R.string.pk_type_comm);
                    break;
                default:
                    holer.type.setText(R.string.sudoku_new_game);

            }
            return view;
        }

    }
    public static class ViewHolder {
        TextView name;
        TextView time;
        TextView type;
    }
}
