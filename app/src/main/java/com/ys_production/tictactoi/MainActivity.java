package com.ys_production.tictactoi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
CardView first_player,second_player;
private boolean player_state = false;
private  ArrayList<String> stringarray = new ArrayList<>();
String[] win_position = {"123","231","321","132","213","312",
        "789","798","879","897","978","987",
        "147","174","417","471","741","714",
        "258","285","528","582","852","825",
        "456","465","546","564","645","654",
        "369","396","693","639","963","936",
        "159","195","519","591","915","951",
        "357","375","537","573","753","735"};
private View
        item1 = null;
    private  View item2 = null;
    private  View item3 = null;
    private  View item4 = null;
    private  View item5 = null;
    private  View item6 = null;
    private  View item7 = null;
    private  View item8 = null;
    private  View item9 = null;
private StringBuilder first_point = new StringBuilder();
private StringBuilder second_point = new StringBuilder();
private ArrayList<View> viewList =  new ArrayList();
private boolean gameStatus = true;
private String winner_postion = null;
private Animation left_to_frontt,right_to_frontt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        item1 = findViewById(R.id.item1);
        item2 = findViewById(R.id.item2);
        item3= findViewById(R.id.item3);
        item4 = findViewById(R.id.item4);
        item5 = findViewById(R.id.item5);
        item6 = findViewById(R.id.item6);
        item7 = findViewById(R.id.item7);
        item8 = findViewById(R.id.item8);
        item9 = findViewById(R.id.item9);

        viewList.add(item1);
        viewList.add(item2);
        viewList.add(item3);
        viewList.add(item4);
        viewList.add(item5);
        viewList.add(item6);
        viewList.add(item7);
        viewList.add(item8);
        viewList.add(item9);
        Collections.addAll(stringarray, win_position);

        first_player = findViewById(R.id.xx_player);
        second_player = findViewById(R.id.oo_player);

        View.OnClickListener itemclick = v -> {
            if (gameStatus) {
                ImageView imageView = (ImageView) v;
//                Log.e("MainActivity", "onCreate: "+((ImageView) v).getDrawable()+"    2   "+ AppCompatResources.getDrawable(this,R.drawable.white_box)+"     3     "+ imageView.getDrawable()
//                +"     4   "+getResources().getDrawable(R.drawable.white_box));
                int a = viewList.indexOf(findViewById(v.getId()))+1;
                if (imageView.getTag().equals("w")) {
                    if (player_state) {
                        first_point.append(a);
                        imageView.setTag("o");
                        if (win_logic(String.valueOf(first_point)))
                            gameOver();
                        else{
                            state_logic();
                        }
                        imageView.setImageResource(R.drawable.oo);
                    } else {
                        second_point.append(a);
                        if (win_logic(String.valueOf(second_point)))
                            gameOver();
                        else{
                            state_logic();
                        }
                        imageView.setTag("x");
                        imageView.setImageResource(R.drawable.xx);
                    }
                }
            }
        };
//        if (gameStatus) {
            item1.setOnClickListener(itemclick);
            item2.setOnClickListener(itemclick);
            item3.setOnClickListener(itemclick);
            item4.setOnClickListener(itemclick);
            item5.setOnClickListener(itemclick);
            item6.setOnClickListener(itemclick);
            item7.setOnClickListener(itemclick);
            item8.setOnClickListener(itemclick);
            item9.setOnClickListener(itemclick);
            setTags_toImages();
//        }
        state_logic();
        findViewById(R.id.refresh_btn).setOnClickListener(v -> reset_Game());
        left_to_frontt = AnimationUtils.loadAnimation(MainActivity.this,R.anim.left_to_front);
        right_to_frontt = AnimationUtils.loadAnimation(MainActivity.this,R.anim.right_to_front);
    }

    private void gameOver() {
        gameStatus = false;
        TextView o_wint_txt = findViewById(R.id.o_winner);
        TextView x_wint_txt = findViewById(R.id.x_winner);
        if (winner_postion.equals(String.valueOf(first_point))){
            o_wint_txt.setAnimation(left_to_frontt);
            o_wint_txt.setVisibility(View.VISIBLE);
        }else if (winner_postion.equals(String.valueOf(second_point))){
            x_wint_txt.setAnimation(right_to_frontt);
            x_wint_txt.setVisibility(View.VISIBLE);
        }
    }

    public void state_logic(){
        if (player_state){
            second_player.setCardBackgroundColor(getResources().getColor(R.color.nonBack));
            first_player.setCardBackgroundColor(getResources().getColor(R.color.player_back));
            player_state = false;
        }else{
            first_player.setCardBackgroundColor(getResources().getColor(R.color.nonBack));
            second_player.setCardBackgroundColor(getResources().getColor(R.color.player_back));
            player_state = true;
        }
    }
    public boolean  win_logic(String player){
//        boolean status = false;
        for (int i = 0; i < stringarray.size(); i++) {
//            Log.e("MainActivity", "win_logic: i = "+i+"     stringarray = "+stringarray.get(i)+"    player = "+player );
//            status = player.matches(stringarray.get(i));
            int truecount = 0;
            for(int j = 0;j<player.length();j++){
                if (stringarray.get(i).contains(String.valueOf(player.charAt(j)))){
                    truecount++;
                }
            }
            if (truecount >= 3){
                winner_postion = player;
                return true;
            }
        }
        return false;
    }
    public void setTags_toImages(){
        //set Tags
        item1.setTag("w");
        item2.setTag("w");
        item3.setTag("w");
        item4.setTag("w");
        item5.setTag("w");
        item6.setTag("w");
        item7.setTag("w");
        item8.setTag("w");
        item9.setTag("w");
    }
    public void reset_Game(){
//        player_state = false;
//        first_point = new StringBuilder();
//        second_point = new StringBuilder();
//        gameStatus = true;
//        ((ImageView)item1).setImageResource(R.drawable.white_box);
//        ((ImageView)item2).setImageResource(R.drawable.white_box);
//        ((ImageView)item3).setImageResource(R.drawable.white_box);
//        ((ImageView)item4).setImageResource(R.drawable.white_box);
//        ((ImageView)item5).setImageResource(R.drawable.white_box);
//        ((ImageView)item6).setImageResource(R.drawable.white_box);
//        ((ImageView)item7).setImageResource(R.drawable.white_box);
//        ((ImageView)item8).setImageResource(R.drawable.white_box);
//        ((ImageView)item9).setImageResource(R.drawable.white_box);
//        state_logic();
        startActivity(new Intent(this,MainActivity.class),
                ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, Pair.create(item5,"t1"),Pair.create(item5,"t2")).toBundle());
        finish();
    }


}