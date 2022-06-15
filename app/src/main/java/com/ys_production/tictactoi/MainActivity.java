package com.ys_production.tictactoi;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class MainActivity extends AppCompatActivity implements Uads, Online_game_controller.Online_game, Online_game_controller.Online_game.Position_logic {
    private static final String TAG = "MainActivity";
    CardView xx_player, oo_player;
    String[] win_position = {"123", "231", "321", "132", "213", "312",
            "789", "798", "879", "897", "978", "987",
            "147", "174", "417", "471", "741", "714",
            "258", "285", "528", "582", "852", "825",
            "456", "465", "546", "564", "645", "654",
            "369", "396", "693", "639", "963", "936",
            "159", "195", "519", "591", "915", "951",
            "357", "375", "537", "573", "753", "735"};
    TextView o_wint_txt, x_wint_txt;
    private String InterstitialID, winner_postion = null;
    private boolean AC, adisRunning = false, gameStatus = true;
    private int player_state = 0, AdT = 60000, ACT = 40000, loadTime = 0, resetGameAnimation = 2;
    private ArrayList<String> win_code_array;
    private View item1 = null, item2 = null, item3 = null, item4 = null, item5 = null, item6 = null, item7 = null, item8 = null, item9 = null;
    private StringBuilder oo_point, xx_point;
    private ArrayList<View> viewList;
    private Animation left_to_frontt, right_to_frontt, to_right, to_left, to_up, to_down, to_right_down, to_left_down, to_right_up, to_left_up, size_to_zero;
    private SharedPreferences sp;
    private AlertDialog.Builder ask_play_online;
    private boolean joining_game, isOnline = false;
    private Editable code = null;
    private ProgressDialog pd;
    private AlertDialog creat_join_dialog;
    private ValueEventListener valueEventListener;
    private FirebaseDatabase database;
    private String gameCode;
    private boolean myTurn = false;
    private Position_logic position_logic;
    private boolean winnerHasSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        position_logic = this;
        xx_player = findViewById(R.id.xx_player);
        oo_player = findViewById(R.id.oo_player);
        final MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.liquid_bubble);
        View.OnClickListener itemclick = v -> {
            if (gameStatus) {
                if (isOnline) {
                    itemClick_online(v);
                } else {
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
                        if (player_state == 0) {
                            oo_point.append(a);
                            imageView.setTag("o");
                            if (oo_point.length() >= 3) {
                                if (win_logic(String.valueOf(oo_point)))
                                    gameOver();
                            }
                            imageView.setImageResource(R.drawable.oo);
                        } else {
                            xx_point.append(a);
                            imageView.setTag("x");
                            if (xx_point.length() >= 3) {
                                if (win_logic(String.valueOf(xx_point)))
                                    gameOver();
                            }
                            imageView.setImageResource(R.drawable.xx);
                        }
                        Log.d(TAG, "item click int : " + a);
                        state_logic();
                    }
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
            win_code_array = new ArrayList<>();
            Collections.addAll(win_code_array, win_position);
            setTags_toImages();
            runOnUiThread(this::state_logic);

            oo_point = new StringBuilder();
            xx_point = new StringBuilder();
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
                    runOnUiThread(() -> ((TextView) findViewById(R.id.textView)).setText(sp.getString("top_name", null)));
                } catch (NullPointerException nullPointerException) {
                    nullPointerException.printStackTrace();
                }
            }
            for (int i = 0; viewList.size() > i; i++) {
                viewList.get(i).setOnClickListener(itemclick);
            }
            ask_play_online = new AlertDialog.Builder(MainActivity.this);
            ask_play_online.setTitle("Play online");
            ask_play_online.setMessage("Do you want to play online ?");
            ask_play_online.setCancelable(true);
            ask_play_online.setPositiveButton("Yes", (dialog, which) -> setOnline()).setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            runOnUiThread(() -> {
                pd = new ProgressDialog(this);
                pd.setMessage("Please wait...");
                pd.create();
            });
            ((Button) findViewById(R.id.exit_online_btn)).setOnClickListener(v -> set_enviorment_OFFLINE());
        }).start();
        findViewById(R.id.reset_btn).setOnClickListener(v -> reset_Game());
        findViewById(R.id.swap_btn).setOnClickListener(v -> state_logic());
        findViewById(R.id.online_btn).setOnClickListener(v -> ask_play_online.create().show());
        left_to_frontt = AnimationUtils.loadAnimation(MainActivity.this, R.anim.left_to_front);
        right_to_frontt = AnimationUtils.loadAnimation(MainActivity.this, R.anim.right_to_front);
        database = FirebaseDatabase.getInstance();
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
                    } else adisRunning = false;
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

    private void itemClick_online(View v) {
        if (myTurn) {
            if (((ImageView) v).getTag().equals("w")) {
                DatabaseReference reference = database.getReference("Game/" + gameCode);
//                if (xx_player.getVisibility() == View.VISIBLE){
                reference.child(String.valueOf(viewList.indexOf(v))).setValue(player_state);
//                }else{
//                    reference.child(String.valueOf(viewList.indexOf(v))).setValue(0);
//                }

                reference.child("oo").setValue(!joining_game);
                myTurn = false;
            }
        }

    }

    private void gameOver() {
        new Thread(() ->
                MediaPlayer.create(MainActivity.this, R.raw.winning).start()
        ).start();
        gameStatus = false;
        if (winner_postion.equals(String.valueOf(oo_point))) {
            o_wint_txt.startAnimation(left_to_frontt);
            o_wint_txt.setVisibility(View.VISIBLE);
        } else if (winner_postion.equals(String.valueOf(xx_point))) {
            x_wint_txt.startAnimation(right_to_frontt);
            x_wint_txt.setVisibility(View.VISIBLE);

        }
    }

    public void state_logic() {
        if (isOnline) {
//            position_logic.setPosition();
        } else {
            if (player_state == 0) {
                ((ConstraintLayout) findViewById(R.id.x_back)).setVisibility(View.VISIBLE);
                ((ConstraintLayout) findViewById(R.id.o_back)).setVisibility(View.INVISIBLE);
                player_state = 1;
            } else {
                ((ConstraintLayout) findViewById(R.id.o_back)).setVisibility(View.VISIBLE);
                ((ConstraintLayout) findViewById(R.id.x_back)).setVisibility(View.INVISIBLE);
                player_state = 0;
            }
        }
    }

    public boolean win_logic(String player) {
//        boolean status = false;//
        for (int i = 0; i < win_code_array.size(); i++) {
            int truecount = 0;
            for (int j = 0; j < player.length(); j++) {
                if (win_code_array.get(i).contains(String.valueOf(player.charAt(j)))) {
                    truecount++;
                }
            }
            if (truecount == 3) {
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
        player_state = 1;
        oo_point = new StringBuilder();
        xx_point = new StringBuilder();
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

    void pdShow() {
        if (!pd.isShowing()) pd.show();
    }

    void pdDismiss() {
        if (pd.isShowing()) pd.dismiss();
    }

    void setOnline() {
//        CardView constraintLayout = findViewById(R.id.materialCardView);
//        CardView cardView = (CardView) getLayoutInflater().inflate(R.layout.online_chooser
//                ,constraintLayout,false);
//
//        constraintLayout.addView(cardView,1);
//        Button close_btn = findViewById(R.id.close_alert_btn);
//        close_btn.setOnClickListener(v -> constraintLayout.removeView(cardView));
        View view = getLayoutInflater().inflate(R.layout.online_chooser, null);
        creat_join_dialog = new AlertDialog.Builder(MainActivity.this).create();
        creat_join_dialog.setCancelable(true);
        creat_join_dialog.setView(view);
        creat_join_dialog.show();

        view.findViewById(R.id.create_server_btn).setOnClickListener(v -> {
            joining_game = false;
            code = ((EditText) view.findViewById(R.id.code_editText)).getText();
            if (code != null) {
                pdShow();
                Online_game_controller onlineGameController = new Online_game_controller(MainActivity.this);
                onlineGameController.checkGameExist(code.toString());
            }
        });
        view.findViewById(R.id.join_server_btn).setOnClickListener(v -> {
            joining_game = true;
            code = ((EditText) view.findViewById(R.id.code_editText)).getText();
            if (code != null) {
                pdShow();
                Online_game_controller onlineGameController = new Online_game_controller(MainActivity.this);
                onlineGameController.checkGameExist(code.toString());
            }
        });
        view.findViewById(R.id.close_alert_btn).setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Close click", Toast.LENGTH_SHORT).show();
            creat_join_dialog.dismiss();
        });

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


    @Override
    public void resultGameExist(boolean exist) {
        Log.d(TAG, "resultGameExist: " + exist);
        DatabaseReference myRef = database.getReference("Game");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder tempO = new StringBuilder();
                StringBuilder tempX = new StringBuilder();
                ImageView imageView;
                for (int i = 0; i < 9; i++) {
                    if (snapshot.hasChild(String.valueOf(i))) {
                        imageView = (ImageView) viewList.get(i);
                        if (snapshot.child(String.valueOf(i)).getValue(Integer.class).equals(0)) {
                            imageView.setImageResource(R.drawable.oo);
                            imageView.setTag("o");
                            tempO.append(i);
//                            if (!tempO.toString().equals(oo_point.toString())) {
//                                Log.d(TAG, "onDataChange !tempO.toString().equals(oo_point.toString()): TRUE O:"+oo_point.toString());
//                                oo_point.append(i);
//                                if (oo_point.toString().length() >= 3) {
//                                    if (win_logic(oo_point.toString())) {
//                                        Log.d(TAG, "onDataChange win_logic(oo_point.toString()): TRUE O:"+oo_point.toString());
//                                        myRef.child(gameCode).child("winner").setValue(0);
//                                        setWinner_online(0);
//                                    }
//                                }
//                            }
                        } else {
                            imageView.setImageResource(R.drawable.xx);
                            imageView.setTag("x");
                            tempX.append(i);
//                            if (!tempX.toString().equals(xx_point.toString())) {
//                                Log.d(TAG, "onDataChange !tempX.toString().equals(xx_point.toString()): TRUE X:"+xx_point.toString());
//                                xx_point.append(i);
//                                if (xx_point.toString().length() >= 3) {
//                                    if (win_logic(xx_point.toString())) {
//                                        Log.d(TAG, "onDataChange win_logic(xx_point.toString()): TRUE X:"+xx_point.toString());
//                                        myRef.child(gameCode).child("winner").setValue(1);
//                                        setWinner_online(1);
//                                    }
//                                }
//                            }
                        }
                    }
                }
                if (!tempO.toString().equals(oo_point.toString())) {
                    Log.d(TAG, "onDataChange !tempO.toString().equals(oo_point.toString()): TRUE O:" + oo_point.toString());
//                    oo_point.append(i);
                    oo_point = tempO;
                    if (oo_point.toString().length() >= 3) {
                        if (win_logic(oo_point.toString())) {
                            Log.d(TAG, "onDataChange win_logic(oo_point.toString()): TRUE O:" + oo_point.toString());
                            myRef.child(gameCode).child("winner").setValue(0);
                            setWinner_online(0);
                        }
                    }
                }
                if (!tempX.toString().equals(xx_point.toString())) {
                    Log.d(TAG, "onDataChange !tempX.toString().equals(xx_point.toString()): TRUE X:" + xx_point.toString());
//                    xx_point.append(i);
                    xx_point = tempX;
                    if (xx_point.toString().length() >= 3) {
                        if (win_logic(xx_point.toString())) {
                            Log.d(TAG, "onDataChange win_logic(xx_point.toString()): TRUE X:" + xx_point.toString());
                            myRef.child(gameCode).child("winner").setValue(1);
                            setWinner_online(1);
                        }
                    }
                }
                if (joining_game) {
                    if (snapshot.child("oo").getValue(Boolean.class)) {
                        myTurn = true;
                        position_logic.setPosition(0);

                    } else {
                        myTurn = false;
                        position_logic.setPosition(1);
                    }
                } else {
                    if (!snapshot.child("oo").getValue(Boolean.class)) {
                        myTurn = true;
                        position_logic.setPosition(2);
                    } else {
                        myTurn = false;
                        position_logic.setPosition(3);
                    }
                }
            }

            private void setWinner_online(int winner) {
                gameStatus = false;
                if (!winnerHasSet) {
                    winnerHasSet = true;
                    new Thread(() ->
                            MediaPlayer.create(MainActivity.this, R.raw.winning).start()
                    ).start();
                    if (winner == 0) {
                        if (joining_game) {
                            x_wint_txt.setShadowLayer(40, 0, 0, getResources().getColor(R.color.winner_text_shadow));
                            x_wint_txt.setText("Winner!!");
                        } else {
                            x_wint_txt.setShadowLayer(40, 0, 0, getResources().getColor(R.color.x_player_back));
                            x_wint_txt.setText("Looser!!");
                        }
                    } else {
                        if (joining_game) {
                            x_wint_txt.setShadowLayer(40, 0, 0, getResources().getColor(R.color.x_player_back));
                            x_wint_txt.setText("Looser!!");
                        } else {
                            x_wint_txt.setShadowLayer(40, 0, 0, getResources().getColor(R.color.winner_text_shadow));
                            x_wint_txt.setText("Winner!!");
                        }
                    }

                    x_wint_txt.startAnimation(right_to_frontt);
                    x_wint_txt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        if (joining_game) {
            Log.d(TAG, "resultGameExist: joining_game " + true);
            if (exist) {
                set_enviorment_ONLINE();
                ((ImageView) findViewById(R.id.x_image)).setVisibility(View.GONE);
                ((ConstraintLayout) findViewById(R.id.x_back)).setBackgroundColor(getResources().getColor(R.color.o_player_back));
                Log.d(TAG, "resultGameExist: exist " + true);
//                myRef.child(code.toString()).setValue("").addOnCompleteListener(task -> {
                gameCode = code.toString();
                DatabaseReference moves = myRef.child(code.toString());
                moves.addValueEventListener(valueEventListener);
                player_state = 1;
                state_logic();
                creat_join_dialog.dismiss();
                pdDismiss();
            } else {
                Log.d(TAG, "resultGameExist: exist " + exist);
                Toast.makeText(MainActivity.this, "Game does not exist", Toast.LENGTH_SHORT).show();
            }
            pdDismiss();
        } else {
            Log.d(TAG, "resultGameExist: joining_game " + joining_game);
            if (exist) {
                Log.d(TAG, "resultGameExist: exist " + true);
                pdDismiss();
                Toast.makeText(MainActivity.this, "Code already used", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "resultGameExist: exist " + exist);
                gameCode = code.toString();
                myRef.child(code.toString()).child("oo").setValue(false).addOnCompleteListener(task -> {
                    player_state = 0;
                    DatabaseReference moves = myRef.child(code.toString());
                    moves.addValueEventListener(valueEventListener);
                });
                creat_join_dialog.dismiss();
                pdDismiss();
            }
        }
    }

    void set_enviorment_ONLINE() {
        isOnline = true;
        ((Button) findViewById(R.id.swap_btn)).setVisibility(View.GONE);
        reset_Game();
        oo_player.setVisibility(View.INVISIBLE);
        ((Button) findViewById(R.id.online_btn)).setVisibility(View.GONE);
        ((Button) findViewById(R.id.exit_online_btn)).setVisibility(View.VISIBLE);
    }

    void set_enviorment_OFFLINE() {
        isOnline = false;
        ((Button) findViewById(R.id.swap_btn)).setVisibility(View.VISIBLE);
        reset_Game();
        oo_player.setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.online_btn)).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.exit_online_btn)).setVisibility(View.GONE);
    }

//    private boolean win_logic_Online(String positions) {
////        String ooo = oo_point.toString();
////        String xxx = xx_point.toString();
//        if (positions.length() < 3){
//            for (int i = 0; i < win_code_array.size(); i++) {
//                int truecount = 0;
//                for (int j = 0; j < oo_point.length(); j++) {
//                    if (win_code_array.get(i).contains(String.valueOf(position.charAt(j)))) {
//                        truecount++;
//                    }
//                }
//                if (truecount >= 3) {
//                    winner_postion = positions;
//                }
//            }
//        }
//        return false;
//    }

    @Override
    public void setPosition(int position) {
        if (position == 0) {
            ((ConstraintLayout) findViewById(R.id.x_back)).setVisibility(View.VISIBLE);
            player_state = 0;
        } else if (position == 1) {
            ((ConstraintLayout) findViewById(R.id.x_back)).setVisibility(View.INVISIBLE);
            player_state = 1;

        } else if (position == 2) {
            ((ConstraintLayout) findViewById(R.id.x_back)).setVisibility(View.VISIBLE);
            player_state = 1;
        } else if (position == 3) {
            ((ConstraintLayout) findViewById(R.id.x_back)).setVisibility(View.INVISIBLE);
            player_state = 0;
        }

//        else{
//            ((ConstraintLayout) findViewById(R.id.x_back)).setVisibility(View.VISIBLE);
//            ((ConstraintLayout) findViewById(R.id.o_back)).setVisibility(View.INVISIBLE);
//            player_state = false;
//        }
    }
}


//    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this,"12")
//            .setSmallIcon(R.drawable.oxox_logo)
//            .setContentTitle("My notification")
//            .setContentText("Much longer text that cannot fit one line...")
//            .setStyle(new NotificationCompat.BigTextStyle()
//                    .bigText("Much longer text that cannot fit one line..."))
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
//                    notificationManager.notify(2, builder.build());