package com.ys_production.tictactoi

import androidx.appcompat.app.AppCompatActivity
import com.ys_production.tictactoi.Uads
import com.ys_production.tictactoi.Online_game_controller.Online_game
import com.ys_production.tictactoi.Online_game_controller.Online_game.Position_logic
import androidx.cardview.widget.CardView
import android.widget.TextView
import android.view.animation.Animation
import android.content.SharedPreferences
import android.text.Editable
import android.app.ProgressDialog
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import android.media.MediaPlayer
import android.os.Bundle
import com.ys_production.tictactoi.R
import com.ys_production.tictactoi.MainActivity
import android.content.DialogInterface
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import org.json.JSONObject
import org.json.JSONException
import android.content.Intent
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.EditText
import com.ys_production.tictactoi.Online_game_controller
import com.google.android.gms.tasks.OnCompleteListener

interface Uads {
    interface Uads2 {
        fun runUnityAdsInitialization(jsonData: String?)
    }
}