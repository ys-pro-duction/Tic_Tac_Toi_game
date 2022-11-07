package com.ys_production.tictactoi

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.database.*
import com.ys_production.tictactoi.databinding.ActivityMainBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.RuntimeException
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), Uads, Online_game_controller.Online_game,
    Online_game_controller.Online_game.Position_logic {
    private lateinit var binding: ActivityMainBinding

    private lateinit var winnerPostion: String
    private var gameStatus = true
    private var playerState = 0
    private var resetGameAnimation = 2
    private val ooPoint: StringBuilder by lazy { StringBuilder() }
    private val xxPoint: StringBuilder by lazy { StringBuilder() }
    private var leftToFrontt: Animation? = null
    private var rightToFrontt: Animation? = null
    private var toRight: Animation? = null
    private var toLeft: Animation? = null
    private var toUp: Animation? = null
    private var toDown: Animation? = null
    private var toRightDown: Animation? = null
    private var toLeftDown: Animation? = null
    private var toRightUp: Animation? = null
    private var toLeftUp: Animation? = null
    private var sizeToZero: Animation? = null
    private lateinit var askPlayOnline: AlertDialog
    private var joiningGame = false
    private var isOnline = false
    private lateinit var code: String
    private lateinit var creatJoinDialog: AlertDialog
    private lateinit var valueEventListener: ValueEventListener
    private lateinit var database: FirebaseDatabase
    private var myTurn = false
    private lateinit var positionLogic: Online_game_controller.Online_game.Position_logic
    private var winnerHasSet = false
    private lateinit var myRef: DatabaseReference
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var progressdialog: AlertDialog
    private val imageList by lazy {
        arrayOf(binding.item1, binding.item2, binding.item3,
            binding.item4, binding.item5, binding.item6,
            binding.item7, binding.item8, binding.item9)
    }
    private val nItemarray by lazy {
        arrayOf(binding.item1n, binding.item2n, binding.item3n,
            binding.item4n, binding.item5n, binding.item6n,
            binding.item7n, binding.item8n, binding.item9n)
    }

    private var mInterstitialAd: InterstitialAd? = null
    private var i1 = false
    private var i1id: String = ""
    private var game = 15
    private var gameWins = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        positionLogic = this
        val sp = getSharedPreferences("UI_data", Context.MODE_PRIVATE)
        val executor = Executors.newSingleThreadExecutor()
//        val handler = Handler(Looper.getMainLooper())
//        Thread {
        executor.execute {
            ooPoint.clear()
            xxPoint.clear()

        leftToFrontt = AnimationUtils.loadAnimation(this@MainActivity, R.anim.left_to_front)
        rightToFrontt = AnimationUtils.loadAnimation(this@MainActivity, R.anim.right_to_front)

        database = FirebaseDatabase.getInstance()
        val appConfig: DatabaseReference = database.getReference("App_config")
        appConfig.addValueEventListener(appconfigValueListner(sp))
//        Thread {
            val stringBuilder = StringBuilder()
            try {
                val connection = URL("https://ys-pro-duction.github.io/appconfigs/tictactoe/data.json").openConnection()
                connection.connect()
                val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
                var line: String?
                while (null != reader.readLine().also { line = it }) {
                    stringBuilder.append(line).append("\n")
                }
                val jsonObject = JSONObject(stringBuilder.toString())
                i1 = jsonObject.getBoolean("I1")
                i1id = jsonObject.getString("I1id")
                game = jsonObject.getInt("game")
                runOnUiThread {
                    if (i1)
                    MobileAds.initialize(this)
                }
            } catch (e: IOException) {
                Log.d(TAG, "onCreate: jsonload IOExeption ${e.message}")
            } catch (e: JSONException) {
                Log.d(TAG, "onCreate: jsonload JSONException ${e.message}")
            }
//        }.start()
        }
        if (!sp.all?.isEmpty()!!) {
            try {

                val colorarray: Array<Int> = Array(9) { i ->
                    parseColors(sp.getString("${item_back}${i + 1}", null ) )
                }
//                for (i in nItemarray.indices){
//                    colorarray[i] = parseColors(sp.getString("${item_back}${i+1}", null))
//                }
                for (i in nItemarray.indices) {
                    nItemarray[i].setCardBackgroundColor(colorarray[i])
                }
            } catch (nullPointerException: NullPointerException) {
                nullPointerException.printStackTrace()
            }
        }
        binding.resetBtn.setOnClickListener { resetGame() }
        binding.swapBtn.setOnClickListener { if (isOnline) swapBtnONLINE() else stateLogic() }
        binding.onlineBtn.setOnClickListener {
            askPlayOnline = AlertDialog.Builder(this@MainActivity).setPositiveButton("Yes"
            ) { _: DialogInterface?, _: Int -> setOnline() }
                .setNegativeButton("No"
                ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
            askPlayOnline.setTitle("Play online")
            askPlayOnline.setMessage("Do you want to play online ?")
            askPlayOnline.setCancelable(true)
            askPlayOnline.show()
            progressdialog = createProgressDialog()
        }
        binding.textView.text =
            sp.getString("top_name",
                "")
        try {
            binding.mainConstrain.setBackgroundColor(parseColors(sp.getString("main_back",
                null)))
            binding.xxPlayer.setCardBackgroundColor(parseColors(
                sp.getString("x_back", null)))
            binding.ooPlayer.setCardBackgroundColor(parseColors(
                sp.getString("o_back", null)))
        }catch (e: NullPointerException){
            Log.e(TAG, "onCreate: ${e.message}" )
        }catch (e: RuntimeException){
            Log.e(TAG, "onCreate: ${e.message}" )
        }
        for (i in imageList) {
            i.setOnClickListener(itemClicklistner())
        }
        binding.exitOnlineBtn.setOnClickListener { exitOnlineBtnListener() }
        stateLogic()
    }
    private fun parseColors(color: String?): Int {
        return Color.parseColor(color)
    }
    private fun exitOnlineBtnListener(){
        val exitOnlineDialog = AlertDialog.Builder(this)
        exitOnlineDialog.setTitle("Exit online")
        exitOnlineDialog.setMessage("Do you want to exit online game?")
        exitOnlineDialog.setCancelable(true)
        exitOnlineDialog.setPositiveButton("Yes"
        ) { _: DialogInterface?, _: Int -> setEnviormentOFFLINE() }
            .setNegativeButton("No"
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .create().show()
//                }

    }
    private fun appconfigValueListner(sp: SharedPreferences): ValueEventListener{
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    Thread {
                        resetGameAnimation =
                            snapshot.child("Ranim").getValue(String::class.java)?.toInt() ?: 2
                        val topName: String = snapshot.child("top_name").getValue(
                            String::class.java)!!
                        val xBack: String = snapshot.child("x_back").getValue(
                            String::class.java)!!
                        val oBack: String = snapshot.child("o_back").getValue(
                            String::class.java)!!
                        val mainBack: String = snapshot.child("main_back").getValue(
                            String::class.java)!!
                        val snapColor =
                            arrayOf(snapshot.child("${item_back}1").getValue(String::class.java)!!,
                                snapshot.child("${item_back}2").getValue(String::class.java)!!,
                                snapshot.child("${item_back}3").getValue(String::class.java)!!,
                                snapshot.child("${item_back}4").getValue(String::class.java)!!,
                                snapshot.child("${item_back}5").getValue(String::class.java)!!,
                                snapshot.child("${item_back}6").getValue(String::class.java)!!,
                                snapshot.child("${item_back}7").getValue(String::class.java)!!,
                                snapshot.child("${item_back}8").getValue(String::class.java)!!,
                                snapshot.child("${item_back}9").getValue(String::class.java)!!)
//                        binding.textView.text = topName
                        val editor: SharedPreferences.Editor = sp.edit()!!
                        editor.putString("top_name", topName)
                        editor.putString("x_back", xBack)
                        editor.putString("o_back", oBack)
                        editor.putString("main_back", mainBack)
                        for (i in snapColor.indices) {
                            editor.putString("${item_back}${i + 1}", snapColor[i])
                        }
                        editor.apply()
                    }.start()
                } catch (nullPointerException: NullPointerException) {
                    nullPointerException.printStackTrace()
                } catch (nullPointerException: IllegalArgumentException) {
                    nullPointerException.printStackTrace()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
    }
    private fun loadInterstialAD(context: Context,unitID: String) {
        InterstitialAd.load(context,unitID, getAdRequest(), object: InterstitialAdLoadCallback(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                mInterstitialAd = null
                Log.d(TAG, "onAdFailedToLoad: loadfail")
            }
            override fun onAdLoaded(p0: InterstitialAd) {
                super.onAdLoaded(p0)
                mInterstitialAd = p0
                Log.d(TAG, "onAdLoaded: ad loaded")
            }
        })
    }
    private fun showInterestitialAD() {
        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                mInterstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                mInterstitialAd = null
            }
        }
        mInterstitialAd?.show(this) ?: Log.d(TAG, "gameOver: showInterestitialAD mInterstitial is NULL")
    }

    private fun swapBtnONLINE() {
        database.getReference("Game").child(code).child("move").child("oo")
            .setValue(!joiningGame)
        binding.swapBtn.visibility = View.GONE
    }

    private fun bubbleSound() {
        Thread {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.liquid_bubble)
            }
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    it.seekTo(0)
                }
                it.start()
            }
        }.start()
    }

    private fun itemClickOnline(v: View) {
        if (myTurn) {
            if ((v as ImageView).tag == "w") {
                val reference: DatabaseReference =
                    database.getReference("Game").child(code)
                        .child("move")
                reference.child(imageList.indexOf(v).toString()).setValue(playerState)
                reference.child("oo").setValue(!joiningGame)
                myTurn = false
            }
        }
    }

    private fun gameOver() {
        Thread { MediaPlayer.create(this@MainActivity, R.raw.winning).start() }.start()
        gameStatus = false
        if (winnerPostion == ooPoint.toString()) {
            binding.oWinner.startAnimation(leftToFrontt)
            binding.oWinner.visibility = View.VISIBLE
        } else if (winnerPostion == xxPoint.toString()) {
            binding.xWinner.text = getString(R.string.winnertxt)
            binding.xWinner.setShadowLayer(40f,
                0f,
                0f,
                getColor(R.color.winner_text_shadow))
            binding.xWinner.startAnimation(rightToFrontt)
            binding.xWinner.visibility = View.VISIBLE
        }
        if (i1) {
            if (gameWins != 0 && gameWins % game == game - 2) {
                if (mInterstitialAd == null) {
                    loadInterstialAD(this@MainActivity, i1id)
                }
            }else if (gameWins != 0 && gameWins % game == 0){
                showInterestitialAD()
            }
        }
        gameWins++
    }

    private fun stateLogic() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.live_dot_anim)
        if (!isOnline) {
            playerState = if (playerState == 0) {
                binding.xBack.startAnimation(anim)
                binding.xBack.visibility = View.VISIBLE
                binding.oBack.visibility = View.INVISIBLE
                binding.oBack.clearAnimation()
                1
            } else {
                binding.oBack.startAnimation(anim)
                binding.oBack.visibility = View.VISIBLE
                binding.xBack.visibility = View.INVISIBLE
                binding.xBack.clearAnimation()
                0
            }
        }
    }

    fun winLogic(player: String): Boolean {
        for (i in win_position.indices) {
            var truecount = 0
            for (j in player.indices) {
                if (win_position[i].contains(player[j].toString())) {
                    truecount++
                }
            }
            if (truecount == 3) {
                winnerPostion = player
                return true
            }
        }
        return false
    }

    private fun setTagstoImages() {
        //set Tags
        for (i in imageList) {
            i.tag = "w"
        }
    }

    fun resetGame() {
        Log.d(TAG, "resetGame: start")
        if (isOnline) {
            if (!joiningGame) {
                myRef.child(code).child("move").setValue("")
                myRef.child(code).child("move").child("reset").setValue(true)
                myRef.child(code).child("move").child("oo").setValue(false)
            } else {
                myRef.child(code).child("move").child("reset").setValue(false)
            }
        } else {
            stateLogic()
        }
        if (!joiningGame) {
            binding.swapBtn.visibility = View.VISIBLE
        }
        winnerHasSet = false
        ooPoint.clear()
        xxPoint.clear()

        if (sizeToZero == null) {
            initResetAnimation()
            resetGameAnim()
        }else{
            resetGameAnim()
        }
        Log.d(TAG, "resetGame: end")


    }
    private fun resetGameAnim(){
        if (resetGameAnimation == 1) {
            for (i in imageList.indices) {
                when (i) {
                    0 -> imageList[i].startAnimation(toRightDown)
                    1 -> imageList[i].startAnimation(toDown)
                    2 -> imageList[i].startAnimation(toLeftDown)
                    3 -> imageList[i].startAnimation(toRight)
                    4 -> imageList[i].startAnimation(sizeToZero)
                    5 -> imageList[i].startAnimation(toLeft)
                    6 -> imageList[i].startAnimation(toRightUp)
                    7 -> imageList[i].startAnimation(toUp)
                    8 -> imageList[i].startAnimation(toLeftUp)
                }
                imageList[i].visibility = View.INVISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    imageList[i].setImageResource(R.drawable.transperent_back)
                    imageList[i].visibility = View.VISIBLE
                    setTagstoImages()
                    gameStatus = true
                }, 500)
            }
        } else if (resetGameAnimation == 2) {
            for (i in imageList.indices) {
                imageList[i].startAnimation(sizeToZero)
                imageList[i].visibility = View.INVISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    imageList[i].setImageResource(R.drawable.transperent_back)
                    imageList[i].visibility = View.VISIBLE
                    setTagstoImages()
                    gameStatus = true
                }, 500)
            }
        }
        binding.xWinner.visibility = View.INVISIBLE
        binding.oWinner.visibility = View.INVISIBLE
    }

    private fun pdShow() {
        progressdialog.show()
    }

    private fun pdDismiss() {
        progressdialog.dismiss()
    }

    private fun setOnline() {
        val view: View = layoutInflater.inflate(R.layout.online_chooser, null)
        creatJoinDialog = AlertDialog.Builder(this@MainActivity).create()
        creatJoinDialog.setCancelable(true)
        creatJoinDialog.setView(view)
        creatJoinDialog.show()
        resetGame()
        view.findViewById<View>(R.id.create_server_btn).setOnClickListener {
            joiningGame = false
            code = (view.findViewById<EditText>(R.id.code_editText)).text.toString()
            if (code.isNotEmpty()) {
                pdShow()
                val onlineGameController = Online_game_controller(this@MainActivity)
                onlineGameController.checkGameExist(code)
            }
        }
        view.findViewById<View>(R.id.join_server_btn).setOnClickListener {
            joiningGame = true
            code = (view.findViewById<EditText>(R.id.code_editText)).text.toString()
            if (code.isNotEmpty()) {
                pdShow()
                val onlineGameController = Online_game_controller(this@MainActivity)
                onlineGameController.checkGameExist(code)
            }
        }
        view.findViewById<View>(R.id.close_alert_btn).setOnClickListener {
            creatJoinDialog.dismiss()
        }
    }


    override fun resultGameExist(exist: Boolean) {
        myRef = database.getReference("Game")
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isOnline) {
                    val tempO = StringBuilder()
                    val tempX = StringBuilder()
                    var imageView: ImageView?
                    if (snapshot.child("move")
                            .hasChild("reset")
                    ) if (joiningGame) if (snapshot.child("move").child("reset").getValue(
                            Boolean::class.java) == true
                    ) {
                        resetGame()
                    }
                    for (i in 0..8) {
                        if (snapshot.child("move").hasChild(i.toString())) {
                            imageView = imageList[i]
                            if (snapshot.child("move").child(i.toString()).getValue(
                                    Int::class.java) == 0
                            ) {
                                imageView.setImageResource(R.drawable.oo)
                                imageView.tag = "o"
                                tempO.append(i)
                            } else {
                                imageView.setImageResource(R.drawable.xx)
                                imageView.tag = "x"
                                tempX.append(i)
                            }
                        }
                    }
                    if (tempO.toString() != ooPoint.toString()) {
                        if (!joiningGame) {
                            bubbleSound()
                        }
                        ooPoint.clear()
                        ooPoint.append(tempO)
                        if (ooPoint.toString().length >= 3) {
                            if (winLogic(ooPoint.toString())) {
                                myRef.child(code).child("move").child("winner").setValue(0)
                                setWinnerOnline(0)
                            }
                        }
                    }
                    Log.d(TAG, "onDataChange: point oo: $ooPoint xx: $xxPoint")
                    if (tempX.toString() != xxPoint.toString()) {
                        if (joiningGame) {
                            bubbleSound()
                        }
                        xxPoint.clear()
                        xxPoint.append(tempX)
                        if (xxPoint.toString().length >= 3) {
                            if (winLogic(xxPoint.toString())) {
                                myRef.child(code).child("move").child("winner")
                                    .setValue(1)
                                setWinnerOnline(1)
                            }
                        }
                    }
                    if (snapshot.child("move").hasChild("oo")) {
                        if (joiningGame) {
                            if (snapshot.child("move").child("oo").getValue(Boolean::class.java) == true) {
                                myTurn = true
                                positionLogic.setPosition(0)
                            } else {
                                myTurn = false
                                positionLogic.setPosition(1)
                            }
                        } else {
                            if (tempX.toString().isNotEmpty() || tempO.toString().isNotEmpty()) {
                                if (binding.swapBtn.visibility == View.VISIBLE) {
                                    binding.swapBtn.visibility = View.GONE
                                }
                            }
                            if (!snapshot.child("move").child("oo")
                                    .getValue(Boolean::class.java)!!
                            ) {
                                myTurn = true
                                positionLogic.setPosition(2)
                            } else {
                                myTurn = false
                                positionLogic.setPosition(3)
                            }
                        }
                    }
                }
            }

            private fun setWinnerOnline(winner: Int) {
                gameStatus = false
                if (!winnerHasSet) {
                    winnerHasSet = true
                    Thread { MediaPlayer.create(this@MainActivity, R.raw.winning).start() }
                        .start()
                    if (winner == 0) {
                        if (joiningGame) {
                            binding.xWinner.setShadowLayer(40f,
                                0f,
                                0f,
                                getColor(R.color.winner_text_shadow))
                            binding.xWinner.text = getString(R.string.winnertxt)
                        } else {
                            binding.xWinner.setShadowLayer(40f,
                                0f,
                                0f,
                                getColor(R.color.x_player_back))
                            binding.xWinner.text = getString(R.string.loosertxt)
                        }
                    } else {
                        if (joiningGame) {
                            binding.xWinner.setShadowLayer(40f,
                                0f,
                                0f,
                                getColor(R.color.x_player_back))
                            binding.xWinner.text = getString(R.string.loosertxt)
                        } else {
                            binding.xWinner.setShadowLayer(40f,
                                0f,
                                0f,
                                getColor(R.color.winner_text_shadow))
                            binding.xWinner.text = getString(R.string.winnertxt)
                        }
                    }
                    binding.xWinner.startAnimation(rightToFrontt)
                    binding.xWinner.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        if (joiningGame) {
            if (exist) {
                myRef.child(code)
                    .addValueEventListener(valueEventListener)
                playerState = 1
                setEnviormentONLINE()
                creatJoinDialog.dismiss()
            } else {
                Toast.makeText(this@MainActivity, "Game does not exist", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (exist) {
                Toast.makeText(this@MainActivity, "Code already used", Toast.LENGTH_SHORT).show()
            } else {
                myRef.child(code).child("move").child("oo").setValue(false)
                    .addOnCompleteListener {
                        myRef.child(code)
                            .addValueEventListener(valueEventListener)
                    }
                playerState = 0
                setEnviormentONLINE()
                creatJoinDialog.dismiss()
            }
        }
        pdDismiss()
    }

    private fun setEnviormentONLINE() {
        if (joiningGame) {
            binding.resetBtn.visibility =
                View.GONE
            binding.swapBtn.visibility = View.GONE
            binding.xImage.setImageResource(R.drawable.oo)
            binding.xBack
                .setBackgroundColor(getColor(R.color.o_player_back))

        }
        isOnline = true
        binding.ooPlayer.visibility = View.INVISIBLE
        binding.onlineBtn.visibility = View.GONE
        binding.exitOnlineBtn.visibility = View.VISIBLE
    }

    private fun setEnviormentOFFLINE() {
        valueEventListener.let { myRef.removeEventListener(it) }
        playerState = 0
        isOnline = false
        resetGame()
        if (joiningGame) {
            binding.resetBtn.visibility = View.VISIBLE
            binding.xImage.setImageResource(R.drawable.xx)
            binding.xBack.setBackgroundColor(getColor(
                R.color.x_player_back))
        } else {
            code.let { myRef.child(it).removeValue() }
        }
        binding.swapBtn.visibility = View.VISIBLE
        binding.ooPlayer.visibility = View.VISIBLE
        binding.onlineBtn.visibility = View.VISIBLE
        binding.exitOnlineBtn.visibility = View.GONE
    }

    override fun setPosition(i: Int) {
        when (i) {
            0 -> {
                binding.xBack.visibility = View.VISIBLE
                playerState = 0
            }
            1 -> {
                binding.xBack.visibility = View.INVISIBLE
                playerState = 1
            }
            2 -> {
                binding.xBack.visibility = View.VISIBLE
                playerState = 1
            }
            3 -> {
                binding.xBack.visibility = View.INVISIBLE
                playerState = 0
            }
        }
    }

    private fun itemClicklistner(): View.OnClickListener {
        return View.OnClickListener { v: View ->
            if (gameStatus) {
                bubbleSound()
                if (isOnline) {
                    itemClickOnline(v)
                } else {
                    val imageView = v as ImageView
                    val a = imageList.indexOf(findViewById(v.getId()))
                    if (imageView.tag == "w") {
                        if (playerState == 0) {
                            ooPoint.append(a)
                            imageView.tag = "o"
                            if (ooPoint.length >= 3) {
                                if (winLogic(ooPoint.toString())) gameOver()
                            }
                            imageView.setImageResource(R.drawable.oo)
                        } else {
                            xxPoint.append(a)
                            imageView.tag = "x"
                            if (xxPoint.length >= 3) {
                                if (winLogic(xxPoint.toString())) gameOver()
                            }
                            imageView.setImageResource(R.drawable.xx)
                        }
                        stateLogic()
                    }
                }
            }
        }
    }

    private fun createProgressDialog(): AlertDialog {
        val alertDialog = AlertDialog.Builder(this@MainActivity)
        val progressBar = ProgressBar(this@MainActivity)
        val loadingTxtV = TextView(this@MainActivity)
        val linearlayout = LinearLayout(this@MainActivity)
        linearlayout.orientation = LinearLayout.HORIZONTAL
        linearlayout.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        linearlayout.gravity = Gravity.CENTER
        loadingTxtV.text = getString(R.string.loadingtxt)
        linearlayout.addView(progressBar)
        linearlayout.addView(loadingTxtV)
        alertDialog.setView(linearlayout)
        alertDialog.setCancelable(false)
        return alertDialog.create()
    }
    private fun initResetAnimation(){
        sizeToZero = AnimationUtils.loadAnimation(this, R.anim.size_to_zero)
        toRight = AnimationUtils.loadAnimation(this, R.anim.to_right)
        toLeft = AnimationUtils.loadAnimation(this, R.anim.to_left)
        toUp = AnimationUtils.loadAnimation(this, R.anim.to_up)
        toDown = AnimationUtils.loadAnimation(this, R.anim.to_down)
        toRightDown = AnimationUtils.loadAnimation(this, R.anim.to_right_down)
        toLeftDown = AnimationUtils.loadAnimation(this, R.anim.to_left_down)
        toRightUp = AnimationUtils.loadAnimation(this, R.anim.to_right_up)
        toLeftUp = AnimationUtils.loadAnimation(this, R.anim.to_left_up)
    }

    companion object {
        private val TAG by lazy { "MainActivity" }
        private val win_position by lazy {
            arrayOf(
                "$_0$_1$_2",
                "$_1$_2$_0",
                "$_2$_1$_0",
                "$_0$_2$_1",
                "$_1$_0$_2",
                "$_2$_0$_1",
                "$_6$_7$_8",
                "$_6$_8$_7",
                "$_7$_6$_8",
                "$_7$_8$_6",
                "$_8$_6$_7",
                "$_8$_7$_6",
                "$_0$_3$_6",
                "$_0$_6$_3",
                "$_3$_0$_6",
                "$_3$_6$_0",
                "$_6$_3$_0",
                "$_6$_0$_3",
                "$_1$_4$_7",
                "$_1$_7$_4",
                "$_4$_1$_7",
                "$_4$_7$_1",
                "$_7$_4$_1",
                "$_7$_1$_4",
                "$_3$_4$_5",
                "$_3$_5$_4",
                "$_4$_3$_5",
                "$_4$_5$_3",
                "$_5$_3$_4",
                "$_5$_4$_3",
                "$_2$_5$_8",
                "$_2$_8$_5",
                "$_5$_8$_2",
                "$_5$_2$_8",
                "$_8$_5$_2",
                "$_8$_2$_5",
                "$_0$_4$_8",
                "$_0$_8$_4",
                "$_4$_0$_8",
                "$_4$_8$_0",
                "$_8$_0$_4",
                "$_8$_4$_0",
                "$_2$_4$_6",
                "$_2$_6$_4",
                "$_4$_2$_6",
                "$_4$_6$_2",
                "$_6$_4$_2",
                "$_6$_2$_4")
        }

        private const val _0 = "0"
        private const val _1 = "1"
        private const val _2 = "2"
        private const val _3 = "3"
        private const val _4 = "4"
        private const val _5 = "5"
        private const val _6 = "6"
        private const val _7 = "7"
        private const val _8 = "8"
        private const val item_back = "item_back"
        private fun getAdRequest(): AdRequest {
            return AdRequest.Builder().build()
        }
    }
}