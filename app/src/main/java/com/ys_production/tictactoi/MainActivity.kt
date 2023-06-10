package com.ys_production.tictactoi

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.ys_production.tictactoi.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var lottieanim1: LottieAnimationView
    private lateinit var lottieanim2: LottieAnimationView
    private lateinit var binding: ActivityMainBinding
    private var winnerPostion: String = "w"
    private var gameStatus = true
    private var thisOturn = false
    private var leftToFrontt: Animation? = null
    private var rightToFrontt: Animation? = null
    private var frontToRight: Animation? = null
    private var frontToLeft: Animation? = null
    private var sizeToZero: Animation? = null
    private val sizeToOne by lazy { AnimationUtils.loadAnimation(applicationContext, R.anim.size_to_one) }
    private val anim by lazy { AnimationUtils.loadAnimation(applicationContext, R.anim.live_dot_anim) }
    private val imageList by lazy {
        arrayOf(
            binding.item1, binding.item2, binding.item3,
            binding.item4, binding.item5, binding.item6,
            binding.item7, binding.item8, binding.item9
        )
    }
    private val nItemarray by lazy {
        arrayOf(
            binding.item1n, binding.item2n, binding.item3n,
            binding.item4n, binding.item5n, binding.item6n,
            binding.item7n, binding.item8n, binding.item9n
        )
    }

    private var mInterstitialAd: InterstitialAd? = null
    private var game = 10
    private var gameWins = 0
    private val viewModel by lazy {
        ViewModelProvider(this)[MainActivityViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        binding = ActivityMainBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        lottieanim1 = getAnimationGlitter()
        lottieanim2 = getAnimationGlitter()
        leftToFrontt = AnimationUtils.loadAnimation(applicationContext, R.anim.left_to_front)
        rightToFrontt = AnimationUtils.loadAnimation(applicationContext, R.anim.right_to_front)
        binding.resetBtn.setOnClickListener { resetGame() }
        binding.swapBtn.setOnClickListener { stateLogic() }
        for (i in imageList) {
            i.setOnClickListener(itemClicklistner())
        }
        stateLogic()
        MobileAds.initialize(applicationContext) {
            loadInterstialAD(applicationContext)
        }
        Firebase.remoteConfig.fetch(60)
        updateUIfromServer()
    }

    private fun updateUIfromServer(){
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                Firebase.remoteConfig.let { config ->
                    CoroutineScope(Dispatchers.IO).launch {
                        game = config.getLong("ad_delay").toInt()
                        val colordata = config.getString("item_color")
                        val ja = JSONArray(colordata)
                        val colors = ArrayList<Int>()
                        for (i in 0 until 9) {
                            val color = parseColors(ja.getJSONObject(i).getString("color"))
                            colors.add(color)
                        }
                        val mainBackColor = parseColors(config.getString("main_back"))
                        val playerBackColor = parseColors(config.getString("player_back"))
                        val topName = config.getString("top_name")
                        launch(Dispatchers.Main) {
                            for (i in 0 until colors.size) {
                                nItemarray[i].setCardBackgroundColor(colors[i])
                            }
                            binding.mainConstrain.setBackgroundColor(mainBackColor)
                            binding.ooPlayer.setCardBackgroundColor(playerBackColor)
                            binding.xxPlayer.setCardBackgroundColor(playerBackColor)
                            binding.textView.text = topName
                        }
                    }

                }
            }
        }
    }
    private fun getAnimationGlitter() = LottieAnimationView(applicationContext).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1F
        )
        repeatCount = 0
        setAnimation(R.raw.glitter)
    }
    private fun parseColors(color: String?): Int {
        return Color.parseColor(color)
    }
    private fun showInterestitialAD() {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                mInterstitialAd = null
                loadInterstialAD(applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                mInterstitialAd = null
                loadInterstialAD(applicationContext)

            }
        }
        mInterstitialAd?.show(this)
            ?: Timber.d("gameOver: showInterestitialAD mInterstitial is NULL")
    }

    private fun gameOver() {
        CoroutineScope(Dispatchers.Default).launch { MediaPlayer.create(applicationContext, R.raw.winning).start() }
        gameStatus = false
        if (winnerPostion == "o") {
            binding.oWinner.startAnimation(leftToFrontt)
            binding.oWinner.visibility = View.VISIBLE
            binding.oWinFrame.addView(lottieanim1.apply { playAnimation() })
            binding.oWinFrame.addView(lottieanim2.apply { playAnimation() })
        }
        if (winnerPostion == "x") {
            binding.xWinner.startAnimation(rightToFrontt)
            binding.xWinner.visibility = View.VISIBLE
            binding.xWinFrame.addView(lottieanim1.apply { playAnimation() })
            binding.xWinFrame.addView(lottieanim2.apply { playAnimation() })
        }
        if (gameWins != 0 && gameWins % game == 0) {
            showInterestitialAD()
        }
        gameWins++
    }

    private fun stateLogic() {
        if (thisOturn) {
            binding.xBack.startAnimation(anim)
            binding.xBack.visibility = View.VISIBLE
            binding.oBack.visibility = View.INVISIBLE
            binding.oBack.clearAnimation()
            thisOturn = false
        } else {
            binding.oBack.startAnimation(anim)
            binding.oBack.visibility = View.VISIBLE
            binding.xBack.visibility = View.INVISIBLE
            binding.xBack.clearAnimation()
            thisOturn = true
        }
    }

    private fun winCheck(line: String): Boolean {
        Timber.d("winCheck: $line")
        return if (line.contains("ooo")) {
            winnerPostion = "o"
            true
        } else if (line.contains("xxx")) {
            winnerPostion = "x"
            true
        } else {
            false
        }
    }

    private fun winLogic(): Boolean {
        val allPosition = StringBuilder()
        allPosition
            .append(imageList[0].tag).append(imageList[1].tag).append(imageList[2].tag).append(" ")
            .append(imageList[3].tag).append(imageList[4].tag).append(imageList[5].tag).append(" ")
            .append(imageList[6].tag).append(imageList[7].tag).append(imageList[8].tag).append(" ")
            .append(imageList[0].tag).append(imageList[3].tag).append(imageList[6].tag).append(" ")
            .append(imageList[1].tag).append(imageList[4].tag).append(imageList[7].tag).append(" ")
            .append(imageList[2].tag).append(imageList[5].tag).append(imageList[8].tag).append(" ")
            .append(imageList[0].tag).append(imageList[4].tag).append(imageList[8].tag).append(" ")
            .append(imageList[2].tag).append(imageList[4].tag).append(imageList[6].tag)
        return (winCheck(allPosition.toString()))
    }

    private fun setTagstoImages() {
        //set Tags
        for (i in imageList) {
            i.tag = "w"
        }
    }

    private fun resetGame() {
        Timber.d("resetGame: start")
        stateLogic()
        if (winnerPostion == "o") binding.oWinFrame.removeAllViews() else binding.xWinFrame.removeAllViews()
        if (sizeToZero == null) {
            sizeToZero = AnimationUtils.loadAnimation(applicationContext, R.anim.size_to_zero)
        }
        if (frontToLeft == null) frontToLeft = AnimationUtils.loadAnimation(applicationContext,R.anim.front_to_left)
        if (frontToRight == null) frontToRight = AnimationUtils.loadAnimation(applicationContext,R.anim.front_to_right)
        resetGameAnim()
        binding.swapBtn.apply {
            if (visibility == View.INVISIBLE) visibility = View.VISIBLE
        }
        Timber.d("resetGame: end")
    }

    private fun resetGameAnim() {
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
        binding.xWinner.apply {
            if (visibility == View.VISIBLE){
                startAnimation(frontToLeft)
                visibility = View.INVISIBLE
            }
        }
        binding.oWinner.apply {
            if (visibility == View.VISIBLE){
                startAnimation(frontToRight)
                visibility = View.INVISIBLE
            }
        }
    }

    private fun itemClicklistner(): View.OnClickListener {
        return View.OnClickListener { v: View ->
            if (gameStatus) {
                viewModel.bubbleSound(applicationContext)
                val imageView = v as ImageView
                if (imageView.tag == "w") {
                    if (thisOturn) {
                        imageView.tag = "o"
                        imageView.setImageResource(R.drawable.oo)
                    } else {
                        imageView.tag = "x"
                        imageView.setImageResource(R.drawable.xx)
                    }
                    imageView.startAnimation(sizeToOne)
                    binding.swapBtn.apply {
                        if (visibility == View.VISIBLE) visibility = View.INVISIBLE
                    }
                    stateLogic()
                    CoroutineScope(Dispatchers.Default).launch {
                        if (winLogic()) {
                            CoroutineScope(Dispatchers.Main).launch { gameOver() }
                        }
                    }
                }
            }
        }
    }

    private fun loadInterstialAD(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = AdRequest.Builder().build()
            launch(Dispatchers.Main) {
                InterstitialAd.load(
                    context,
                    "ca-app-pub-1051000134555897/8731550532",
                    request,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            mInterstitialAd = null
                            Timber.d("onAdFailedToLoad: loadfail")
                        }

                        override fun onAdLoaded(p0: InterstitialAd) {
                            super.onAdLoaded(p0)
                            mInterstitialAd = p0
                            Timber.d("onAdLoaded: ad loaded")
                        }
                    })
            }

        }

    }
}
