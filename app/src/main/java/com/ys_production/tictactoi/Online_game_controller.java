package com.ys_production.tictactoi;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

class Online_game_controller {
    private static final String TAG = "Online_game_controller";
    interface Online_game{
        void resultGameExist(boolean result);
        interface Position_logic{
            void setPosition(int i);
        }
    }
    private FirebaseDatabase database;
    private DatabaseReference dataRef;
    private String gamecode;
    private Online_game online_game_check;
    public Online_game_controller(Online_game context) {
        Log.d(TAG, "Online_game_controller: st" );
        online_game_check = context;
        this.database = FirebaseDatabase.getInstance();
        this.dataRef = database.getReference("Game");
    }

    public void checkGameExist(String gameCode) {
        Log.d(TAG, "checkGameExist: st");
        gamecode = gameCode;
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                online_game_check.resultGameExist(snapshot.hasChild(gamecode));
                Log.d(TAG, "onDataChange: gamecode "+gamecode);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        dataRef.addListenerForSingleValueEvent(valueEventListener);
    }

}
