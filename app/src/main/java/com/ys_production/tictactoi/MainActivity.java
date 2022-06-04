package com.ys_production.tictactoi;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements Uads, Uads.Uads2 {
    private static final String TAG = "MainActivity";
    private String InterstitialID ;
    private boolean AC;
    private int AdT = 60000,ACT = 40000;
    private String url;
    CardView first_player, second_player;
    String[] win_position = {"123", "231", "321", "132", "213", "312",
            "789", "798", "879", "897", "978", "987",
            "147", "174", "417", "471", "741", "714",
            "258", "285", "528", "582", "852", "825",
            "456", "465", "546", "564", "645", "654",
            "369", "396", "693", "639", "963", "936",
            "159", "195", "519", "591", "915", "951",
            "357", "375", "537", "573", "753", "735"};
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
    private Animation left_to_frontt, right_to_frontt;
    private int loadTime = 0;
    private Animation size_to_zero;
    TextView o_wint_txt,x_wint_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        first_player = findViewById(R.id.xx_player);
        second_player = findViewById(R.id.oo_player);

        View.OnClickListener itemclick = v -> {
            if (gameStatus) {
                new Thread(() ->
                        MediaPlayer.create(MainActivity.this, R.raw.liquid_bubble).start()
                ).start();
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
                    state_logic();
                }
            }
        };
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
            state_logic();
            for (int i = 0; viewList.size() > i; i++) {
                viewList.get(i).setOnClickListener(itemclick);
            }
            first_point = new StringBuilder();
            second_point = new StringBuilder();
            size_to_zero = AnimationUtils.loadAnimation(this,R.anim.size_to_zero);
            x_wint_txt = findViewById(R.id.x_winner);
            o_wint_txt = findViewById(R.id.o_winner);
        }).start();

//        if (gameStatus) {
//        item1.setOnClickListener(itemclick);
//        item2.setOnClickListener(itemclick);
//        item3.setOnClickListener(itemclick);
//        item4.setOnClickListener(itemclick);
//        item5.setOnClickListener(itemclick);
//        item6.setOnClickListener(itemclick);
//        item7.setOnClickListener(itemclick);
//        item8.setOnClickListener(itemclick);
//        item9.setOnClickListener(itemclick);
        findViewById(R.id.refresh_btn).setOnClickListener(v -> reset_Game());
        findViewById(R.id.swap_btn).setOnClickListener(v -> state_logic());
        left_to_frontt = AnimationUtils.loadAnimation(MainActivity.this, R.anim.left_to_front);
        right_to_frontt = AnimationUtils.loadAnimation(MainActivity.this, R.anim.right_to_front);
        url = UrlBuilder();
        BackgroundTask backgroundTask = new BackgroundTask(this);
        backgroundTask.execute(url);
    }

    private String UrlBuilder() {
        String domain,uname,repo,path,filename,lastobject;
        domain = "https://github.com/";
        uname = "ys-pro-duction/";
        repo = "Tic_Tac_Toi_game";
        path = " /raw/master/app/src/main/res/raw/";
        filename = " status.json";
        lastobject = "?=true";
      return domain+uname+repo+path+filename+lastobject;
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
            second_player.setCardBackgroundColor(getResources().getColor(R.color.nonBack));
            first_player.setCardBackgroundColor(getResources().getColor(R.color.x_player_back));
            player_state = false;
        } else {
            first_player.setCardBackgroundColor(getResources().getColor(R.color.nonBack));
            second_player.setCardBackgroundColor(getResources().getColor(R.color.o_player_back));
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
        for (int i = 0;i < viewList.size();i++){
            ((ImageView)viewList.get(i)).startAnimation(size_to_zero);
            ((ImageView)viewList.get(i)).setVisibility(View.INVISIBLE);
            int finalI = i;
            new Handler().postDelayed(() -> {
                ((ImageView)viewList.get(finalI)).setImageResource(R.drawable.white_box);
                ((ImageView)viewList.get(finalI)).setVisibility(View.VISIBLE);
            },500);
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
                    Log.d(TAG, "closeAd: "+e.getMessage());
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

    @Override
    public void runUnityAdsInitialization(String data) {
        try {
            String  fjkdjfk;
            boolean dlkfjkldj,AT;
            if (data != null) {
                JSONObject jsonObject = new JSONObject(data);
                dlkfjkldj = jsonObject.getBoolean("ads");
                AC = jsonObject.getBoolean("autoC");
                AT = jsonObject.getBoolean("testAd");
                AdT = Integer.parseInt(jsonObject.getString("adsT"));
                ACT = Integer.parseInt(jsonObject.getString("autoCT"));
                fjkdjfk = jsonObject.getString("GID");
                InterstitialID = jsonObject.getString("IAID");

                if (dlkfjkldj) {
                    if (!UnityAds.isInitialized()) {
                        UnityAds.initialize(MainActivity.this, fjkdjfk, AT);
                    }
                    new Handler().postDelayed(this::loadIntstitialAD, 1000);
                }
            }else{
                new Handler().postDelayed(() -> new BackgroundTask(MainActivity.this).execute(url),10000);
            }
        }catch (JSONException jsonException){
            Log.d(TAG, "runUnityAdsInitialization: "+jsonException.getMessage());
        }
    }

    static class BackgroundTask extends AsyncTask<String, Void, String> {
        Uads2 uads2;

        public BackgroundTask(Uads2 uads2) {
            this.uads2 = uads2;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            uads2.runUnityAdsInitialization(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder data = new StringBuilder();
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while (null != (line = reader.readLine())) {
                    data.append(line).append("\n");
                }
                reader.close();
                return data.toString();
            } catch (IOException ioException) {
                Log.d(TAG, "doInBackground: "+ioException.getMessage());
                return null;
            }
        }
    }
}