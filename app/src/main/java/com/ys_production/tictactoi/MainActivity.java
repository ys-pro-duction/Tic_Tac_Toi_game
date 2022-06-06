package com.ys_production.tictactoi;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements Uads {
    private static final String TAG = "MainActivity";
    CardView first_player, second_player;
    String[] win_position = {"123", "231", "321", "132", "213", "312",
            "789", "798", "879", "897", "978", "987",
            "147", "174", "417", "471", "741", "714",
            "258", "285", "528", "582", "852", "825",
            "456", "465", "546", "564", "645", "654",
            "369", "396", "693", "639", "963", "936",
            "159", "195", "519", "591", "915", "951",
            "357", "375", "537", "573", "753", "735"};
    TextView o_wint_txt, x_wint_txt;
    private String InterstitialID;
    private boolean AC;
    private int AdT = 60000, ACT = 40000;
    private boolean adisRunning = false;
    private boolean player_state = false;
    private ArrayList<String> stringarray;
    private View
            item1 = null;
    private View item2 = null;
    private View item3 = null;
    private View item4 = null;
    private View item5 = null;
    private View item6 = null;
    private View item7 = null;
    private View item8 = null;
    private View item9 = null;
    private StringBuilder first_point;
    private StringBuilder second_point;
    private ArrayList<View> viewList;
    private boolean gameStatus = true;
    private String winner_postion = null;
    private Animation left_to_frontt, right_to_frontt, to_right, to_left, to_up, to_down;
    private Animation to_right_down, to_left_down, to_right_up, to_left_up;
    private int loadTime = 0;
    private Animation size_to_zero;
    private SharedPreferences sp;
    private int resetGameAnimation = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        first_player = findViewById(R.id.xx_player);
        second_player = findViewById(R.id.oo_player);
        final MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.liquid_bubble);
        View.OnClickListener itemclick = v -> {
            if (gameStatus) {
                new Thread(() -> {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        mediaPlayer.seekTo(0);
                    }
                    mediaPlayer.start();
                }).start();
                ImageView imageView = (ImageView) v;
                int a = viewList.indexOf(findViewById(v.getId())) + 1;
                if (imageView.getTag().equals("w")) {
                    if (player_state) {
                        first_point.append(a);
                        imageView.setTag("o");
                        if (first_point.length() >= 3) {
                            if (win_logic(String.valueOf(first_point)))
                                gameOver();
                        }
                        imageView.setImageResource(R.drawable.oo);
                    } else {
                        second_point.append(a);
                        imageView.setTag("x");
                        if (second_point.length() >= 3) {
                            if (win_logic(String.valueOf(second_point)))
                                gameOver();
                        }
                        imageView.setImageResource(R.drawable.xx);
                    }
                    Log.d(TAG, "item click int : " + a);
                    state_logic();
                }
            }
        };
        sp = getSharedPreferences("UI_data", MODE_PRIVATE);
        new Thread(() -> {
            item1 = findViewById(R.id.item1);
            item2 = findViewById(R.id.item2);
            item3 = findViewById(R.id.item3);
            item4 = findViewById(R.id.item4);
            item5 = findViewById(R.id.item5);
            item6 = findViewById(R.id.item6);
            item7 = findViewById(R.id.item7);
            item8 = findViewById(R.id.item8);
            item9 = findViewById(R.id.item9);

            viewList = new ArrayList<>();
            viewList.add(item1);
            viewList.add(item2);
            viewList.add(item3);
            viewList.add(item4);
            viewList.add(item5);
            viewList.add(item6);
            viewList.add(item7);
            viewList.add(item8);
            viewList.add(item9);
            stringarray = new ArrayList<>();
            Collections.addAll(stringarray, win_position);
            setTags_toImages();
            runOnUiThread(this::state_logic);

            first_point = new StringBuilder();
            second_point = new StringBuilder();
            size_to_zero = AnimationUtils.loadAnimation(this, R.anim.size_to_zero);
            to_right = AnimationUtils.loadAnimation(this, R.anim.to_right);
            to_left = AnimationUtils.loadAnimation(this, R.anim.to_left);
            to_up = AnimationUtils.loadAnimation(this, R.anim.to_up);
            to_down = AnimationUtils.loadAnimation(this, R.anim.to_down);
            to_right_down = AnimationUtils.loadAnimation(this, R.anim.to_right_down);
            to_left_down = AnimationUtils.loadAnimation(this, R.anim.to_left_down);
            to_right_up = AnimationUtils.loadAnimation(this, R.anim.to_right_up);
            to_left_up = AnimationUtils.loadAnimation(this, R.anim.to_left_up);


            x_wint_txt = findViewById(R.id.x_winner);
            o_wint_txt = findViewById(R.id.o_winner);
            if (!sp.getAll().isEmpty()) {
                try {
                    findViewById(R.id.mainConstrain).setBackgroundColor(Color.parseColor(sp.getString("main_back", null)));
                    ((CardView) findViewById(R.id.xx_player)).setCardBackgroundColor(Color.parseColor(sp.getString("x_back", null)));
                    ((CardView) findViewById(R.id.oo_player)).setCardBackgroundColor(Color.parseColor(sp.getString("o_back", null)));
                    ((CardView) findViewById(R.id.item1n)).setCardBackgroundColor(Color.parseColor(sp.getString("item_back1", null)));
                    ((CardView) findViewById(R.id.item2n)).setCardBackgroundColor(Color.parseColor(sp.getString("item_back2", null)));
                    ((CardView) findViewById(R.id.item3n)).setCardBackgroundColor(Color.parseColor(sp.getString("item_back3", null)));
                    ((CardView) findViewById(R.id.item4n)).setCardBackgroundColor(Color.parseColor(sp.getString("item_back4", null)));
                    ((CardView) findViewById(R.id.item5n)).setCardBackgroundColor(Color.parseColor(sp.getString("item_back5", null)));
                    ((CardView) findViewById(R.id.item6n)).setCardBackgroundColor(Color.parseColor(sp.getString("item_back6", null)));
                    ((CardView) findViewById(R.id.item7n)).setCardBackgroundColor(Color.parseColor(sp.getString("item_back7", null)));
                    ((CardView) findViewById(R.id.item8n)).setCardBackgroundColor(Color.parseColor(sp.getString("item_back8", null)));
                    ((CardView) findViewById(R.id.item9n)).setCardBackgroundColor(Color.parseColor(sp.getString("item_back9", null)));
                    ((TextView) findViewById(R.id.textView)).setText(sp.getString("top_name", null));
                } catch (NullPointerException nullPointerException) {
                    nullPointerException.printStackTrace();
                }
            }
            for (int i = 0; viewList.size() > i; i++) {
                viewList.get(i).setOnClickListener(itemclick);
            }
        }).start();
        findViewById(R.id.refresh_btn).setOnClickListener(v -> reset_Game());
        findViewById(R.id.swap_btn).setOnClickListener(v -> state_logic());
        left_to_frontt = AnimationUtils.loadAnimation(MainActivity.this, R.anim.left_to_front);
        right_to_frontt = AnimationUtils.loadAnimation(MainActivity.this, R.anim.right_to_front);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference appConfig = database.getReference("App_config");
        appConfig.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Log.d(TAG, "onDataChange: start");
                    InterstitialID = snapshot.child("IAID").getValue(String.class);
                    if (snapshot.child("ads").getValue(Boolean.class)) {
                        if (!adisRunning) {
                            if (!UnityAds.isInitialized()) {
                                UnityAds.initialize(MainActivity.this, snapshot.child("GID").getValue(String.class)
                                        , snapshot.child("testAd").getValue(Boolean.class));
                                adisRunning = true;
                                new Handler().postDelayed(() -> loadIntstitialAD(), 5000);
                            } else loadIntstitialAD();
                        }
                    }
                    resetGameAnimation = Integer.parseInt(snapshot.child("Ranim").getValue(String.class));
                    AdT = Integer.parseInt(snapshot.child("adsT").getValue(String.class));
                    AC = snapshot.child("autoC").getValue(Boolean.class);
                    ACT = Integer.parseInt(snapshot.child("autoCT").getValue(String.class));
                    String top_name = snapshot.child("top_name").getValue(String.class);
                    String x_back = snapshot.child("x_back").getValue(String.class);
                    String o_back = snapshot.child("o_back").getValue(String.class);
                    String mainBack = snapshot.child("main_back").getValue(String.class);
                    String color1 = snapshot.child("item_back1").getValue(String.class);
                    String color2 = snapshot.child("item_back2").getValue(String.class);
                    String color3 = snapshot.child("item_back3").getValue(String.class);
                    String color4 = snapshot.child("item_back4").getValue(String.class);
                    String color5 = snapshot.child("item_back5").getValue(String.class);
                    String color6 = snapshot.child("item_back6").getValue(String.class);
                    String color7 = snapshot.child("item_back7").getValue(String.class);
                    String color8 = snapshot.child("item_back8").getValue(String.class);
                    String color9 = snapshot.child("item_back9").getValue(String.class);
                    findViewById(R.id.mainConstrain).setBackgroundColor(Color.parseColor(mainBack));
                    ((CardView) findViewById(R.id.xx_player)).setCardBackgroundColor(Color.parseColor(x_back));
                    ((CardView) findViewById(R.id.oo_player)).setCardBackgroundColor(Color.parseColor(o_back));
                    ((CardView) findViewById(R.id.item1n)).setCardBackgroundColor(Color.parseColor(color1));
                    ((CardView) findViewById(R.id.item2n)).setCardBackgroundColor(Color.parseColor(color2));
                    ((CardView) findViewById(R.id.item3n)).setCardBackgroundColor(Color.parseColor(color3));
                    ((CardView) findViewById(R.id.item4n)).setCardBackgroundColor(Color.parseColor(color4));
                    ((CardView) findViewById(R.id.item5n)).setCardBackgroundColor(Color.parseColor(color5));
                    ((CardView) findViewById(R.id.item6n)).setCardBackgroundColor(Color.parseColor(color6));
                    ((CardView) findViewById(R.id.item7n)).setCardBackgroundColor(Color.parseColor(color7));
                    ((CardView) findViewById(R.id.item8n)).setCardBackgroundColor(Color.parseColor(color8));
                    ((CardView) findViewById(R.id.item9n)).setCardBackgroundColor(Color.parseColor(color9));
                    ((TextView) findViewById(R.id.textView)).setText(top_name);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("top_name", top_name);
                    editor.putString("x_back", x_back);
                    editor.putString("o_back", o_back);
                    editor.putString("main_back", mainBack);
                    editor.putString("item_back1", color1);
                    editor.putString("item_back2", color2);
                    editor.putString("item_back3", color3);
                    editor.putString("item_back4", color4);
                    editor.putString("item_back5", color5);
                    editor.putString("item_back6", color6);
                    editor.putString("item_back7", color7);
                    editor.putString("item_back8", color8);
                    editor.putString("item_back9", color9);
                    editor.apply();
                    Log.d(TAG, "onDataChange: end");
                } catch (NullPointerException nullPointerException) {
                    nullPointerException.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: start");
            }
        });
    }

    private void gameOver() {
        new Thread(() ->
                MediaPlayer.create(MainActivity.this, R.raw.winning).start()
        ).start();
        gameStatus = false;
        if (winner_postion.equals(String.valueOf(first_point))) {
            o_wint_txt.startAnimation(left_to_frontt);
            o_wint_txt.setVisibility(View.VISIBLE);
        } else if (winner_postion.equals(String.valueOf(second_point))) {
            x_wint_txt.startAnimation(right_to_frontt);
            x_wint_txt.setVisibility(View.VISIBLE);

        }
    }

    public void state_logic() {
        if (player_state) {

            ((ConstraintLayout) findViewById(R.id.x_back)).setVisibility(View.VISIBLE);
            ((ConstraintLayout) findViewById(R.id.o_back)).setVisibility(View.INVISIBLE);
            player_state = false;
        } else {

            ((ConstraintLayout) findViewById(R.id.o_back)).setVisibility(View.VISIBLE);
            ((ConstraintLayout) findViewById(R.id.x_back)).setVisibility(View.INVISIBLE);
            player_state = true;
        }
    }

    public boolean win_logic(String player) {
//        boolean status = false;
        for (int i = 0; i < stringarray.size(); i++) {
            int truecount = 0;
            for (int j = 0; j < player.length(); j++) {
                if (stringarray.get(i).contains(String.valueOf(player.charAt(j)))) {
                    truecount++;
                }
            }
            if (truecount >= 3) {
                winner_postion = player;
                return true;
            }
        }
        return false;
    }

    public void setTags_toImages() {
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

    public void reset_Game() {
        player_state = false;
        first_point = new StringBuilder();
        second_point = new StringBuilder();
        setTags_toImages();
        if (resetGameAnimation == 1) {
            for (int i = 0; i < viewList.size(); i++) {
                switch (i) {
                    case 0:
                        viewList.get(i).startAnimation(to_right_down);
                        break;
                    case 1:
                        viewList.get(i).startAnimation(to_down);
                        break;
                    case 2:
                        viewList.get(i).startAnimation(to_left_down);
                        break;
                    case 3:
                        viewList.get(i).startAnimation(to_right);
                        break;
                    case 4:
                        viewList.get(i).startAnimation(size_to_zero);
                        break;
                    case 5:
                        viewList.get(i).startAnimation(to_left);
                        break;
                    case 6:
                        viewList.get(i).startAnimation(to_right_up);
                        break;
                    case 7:
                        viewList.get(i).startAnimation(to_up);
                        break;
                    case 8:
                        viewList.get(i).startAnimation(to_left_up);
                        break;
                }
//                if (i == 0 || i == 1) {
//                    viewList.get(i).startAnimation(to_right_down);
//                } else if (i == 2 || i == 5) {
//                    viewList.get(i).startAnimation(to_down);
//                } else if (i == 8 || i == 7) {
//                    viewList.get(i).startAnimation(to_left);
//                } else if (i == 6 || i == 3) {
//                    viewList.get(i).startAnimation(to_up);
//                } else viewList.get(i).startAnimation(size_to_zero);
                viewList.get(i).setVisibility(View.INVISIBLE);
                int finalI = i;
                new Handler().postDelayed(() -> {
                    ((ImageView) viewList.get(finalI)).setImageResource(R.drawable.transperent_back);
                    ((ImageView) viewList.get(finalI)).setVisibility(View.VISIBLE);
                }, 500);
            }
        } else if (resetGameAnimation == 2) {
            for (int i = 0; i < viewList.size(); i++) {
                viewList.get(i).startAnimation(size_to_zero);
                viewList.get(i).setVisibility(View.INVISIBLE);
                int finalI = i;
                new Handler().postDelayed(() -> {
                    ((ImageView) viewList.get(finalI)).setImageResource(R.drawable.transperent_back);
                    ((ImageView) viewList.get(finalI)).setVisibility(View.VISIBLE);
                }, 500);
            }
        }

        x_wint_txt.setVisibility(View.INVISIBLE);
        o_wint_txt.setVisibility(View.INVISIBLE);

//        startActivity(new Intent(this, MainActivity.class)
//                , ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, Pair.create(item3, "t1"), Pair.create(item7, "t2")).toBundle()
//        );
//        finish();
        state_logic();
        gameStatus = true;
    }

    private void loadIntstitialAD() {
        if (adisRunning) {
            Log.d(TAG, "loadIntstitialAD: ");
            new Handler().postDelayed(() -> runOnUiThread(() -> {
                if (loadTime < 5) {
                    if (UnityAds.isInitialized()) {
                        loadTime = 0;
                        UnityAds.load(InterstitialID, new IUnityAdsLoadListener() {
                            @Override
                            public void onUnityAdsAdLoaded(String s) {
                                Log.d(TAG, "onUnityAdsAdLoaded:  start");
                                new Handler().postDelayed(() -> runShowAD(), 5000);
                            }

                            @Override
                            public void onUnityAdsFailedToLoad(String s, UnityAds.UnityAdsLoadError unityAdsLoadError, String s1) {
                                Log.d(TAG, "onUnityAdsFailedToLoad:  " + unityAdsLoadError + " string s1: " + s1);
                                runInitializeAD();
                            }
                        });
                    } else {
                        loadTime++;
                        runInitializeAD();
                    }
                }
            }), 5000);
        }
    }

    public void showAd() {
        Log.d(TAG, "showAd:  start");
        UnityAds.show(MainActivity.this, InterstitialID, new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
                Log.d(TAG, "onUnityAdsShowFailure:  start");
                runInitializeAD();
            }

            @Override
            public void onUnityAdsShowStart(String s) {
                Log.d(TAG, "onUnityAdsShowStart: ");
                closeAd();
            }

            @Override
            public void onUnityAdsShowClick(String s) {
                Log.d(TAG, "onUnityAdsShowClick: ");
            }

            @Override
            public void onUnityAdsShowComplete(String s, UnityAds.UnityAdsShowCompletionState unityAdsShowCompletionState) {
                Log.d(TAG, "onUnityAdsShowComplete:  start");
                new Handler().postDelayed(() -> runInitializeAD(), AdT);
            }
        });
    }

    void closeAd() {
        if (AC) {
            new Handler().postDelayed(() -> {
                try {
                    Runtime.getRuntime().exec("input tap 1060 20");
                    closeAd();
                } catch (IOException e) {
                    closeAd();
                    Log.d(TAG, "closeAd: " + e.getMessage());
                }
            }, ACT);
        }
    }

    @Override
    public void runInitializeAD() {
        Log.d(TAG, "runInitializeAD: ");
        loadIntstitialAD();
    }

    @Override
    public void runShowAD() {
        Log.d(TAG, "runShowAD: ");
        showAd();
    }
}