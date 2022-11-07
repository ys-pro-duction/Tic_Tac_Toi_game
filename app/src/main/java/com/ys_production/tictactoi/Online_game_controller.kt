package com.ys_production.tictactoi

import android.util.Log
import com.google.firebase.database.*

internal class Online_game_controller(context: Online_game) {
    internal interface Online_game {
        fun resultGameExist(exist: Boolean)
        interface Position_logic {
            fun setPosition(i: Int)
        }
    }

    private val database: FirebaseDatabase
    private val dataRef: DatabaseReference
    private var gamecode: String? = null
    private val online_game_check: Online_game
    fun checkGameExist(gameCode: String?) {
        Log.d(TAG, "checkGameExist: st")
        gamecode = gameCode
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                online_game_check.resultGameExist(snapshot.hasChild(gamecode!!))
                Log.d(TAG, "onDataChange: gamecode $gamecode")
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        dataRef.addListenerForSingleValueEvent(valueEventListener)
    }

    companion object {
        private const val TAG = "Online_game_controller"
    }

    init {
        Log.d(TAG, "Online_game_controller: st")
        online_game_check = context
        database = FirebaseDatabase.getInstance()
        dataRef = database.getReference("Game")
    }
}