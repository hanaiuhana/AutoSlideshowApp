package jp.techacademy.yui.tanakayui.autoslideshowapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.content.ContextCompat.getColor
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private var startStopButtonText :String = ""
    private var startButtonText :String = ""
    private var stopButtonText :String = ""

    private val PERMISSIONS_REQUEST_CODE = 100

    private var showPictureNum = 0
    private val uriArrayList = arrayListOf<Uri>()
    private var pictureNum = 0

    private var mTimer: Timer? = null
    private var mHandler = Handler()
    private var color = 0

    private var startUpFirst = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //再生・停止ボタンの表示する文字列を取得
        startButtonText = resources.getString(R.string.start_button_text)
        stopButtonText = resources.getString(R.string.stop_button_text)
        color= getColor(this, R.color.color_blue_slideshow)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }


        //再生・停止ボタンの切り替え
        start_stop_button.setOnClickListener {
            startStopButtonText = start_stop_button.text.toString()
            if (startStopButtonText == startButtonText) {
                //再生ボタン押下時
                clickStart()
            } else if (startStopButtonText == stopButtonText) {
                //停止ボタン押下時
                clickStop()
            }
        }
        //進むボタンクリック時の処理
        forward_button.setOnClickListener {
            Log.d("testtest","クリック！")
            showNextPhoto()
        }
        //戻るボタンクリック時の処理
        back_button.setOnClickListener {
            Log.d("testtest","クリック！")
            showPreviousPhoto()
        }

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PERMISSION_GRANTED) {
                    Log.d("ANDROID", "許可された")
                    getContentsInfo()
                } else {
                    Log.d("ANDROID", "許可されなかった")
                }
        }
    }

    //Galleyの写真取得
    private fun getContentsInfo() {
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )
        pictureNum = cursor.count
        Log.d("testtest", "写真の全体枚数：" + pictureNum)


        if (cursor!!.moveToFirst()) {
            var count: Int = 0
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                Log.d("testtest", "URI : " + imageUri.toString())

                //取得したURIを配列へ追加
                uriArrayList.add(imageUri)
                Log.d("testtest", "count" + count.toString())
                count++
            } while (cursor.moveToNext())

            //1枚目の画像をImageViewにをセットする
            if (startUpFirst == true) {
                picture.setImageURI((uriArrayList[0]))
            }
        }
        cursor.close()
    }
    //次の写真を呼び出す
    private fun showNextPhoto(){
        if (showPictureNum < (pictureNum - 1)) {
            showPictureNum++
        } else {
            showPictureNum = 0
        }
        Log.d("testtest",(showPictureNum+ 1).toString() + "枚目")
        picture.setImageURI(uriArrayList[showPictureNum])
    }
    //前の写真を呼び出す
    private fun showPreviousPhoto(){
        if (showPictureNum > 0) {
            showPictureNum--
        } else {
            showPictureNum = (pictureNum - 1)
        }
        Log.d("testtest",(showPictureNum+ 1).toString() + "枚目")
        picture.setImageURI(uriArrayList[showPictureNum])
    }

    //再生ボタン押下時の処理
    private fun clickStart(){
        start_stop_button.setText(R.string.stop_button_text)
        forward_button.isEnabled = false
        back_button.isEnabled = false
        activity_background.setBackgroundColor(color)
        setTimer()
    }
    //停止ボタン押下時の処理
    private fun clickStop(){
        start_stop_button.setText(R.string.start_button_text)
        forward_button.isEnabled = true
        back_button.isEnabled = true
        var color= getColor(this, R.color.color_pink_background)
        activity_background.setBackgroundColor(color)
        mTimer!!.cancel()
    }
    //タイマー処理
    private fun setTimer(){
        //タイマー処理
        mTimer = Timer()
        // タイマーの始動
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                mHandler.post {
                    showNextPhoto()
                }
            }
        }, 2000, 2000)
    }

    //画面の回転時に呼び出される（データを保存する）
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mTimer != null) {
            mTimer!!.cancel()
        }

        //保存領域に「CONTENT_VALUE」というキーの数値を保存
        outState.putInt("COUNT_VALUE", showPictureNum)
        outState.putString("START_STOP_BUTTON_TEXT", start_stop_button.text.toString())
        outState.putBoolean("START_UP_FIRST", false)
    }

    //アクティビティが再起動するときに呼び出される（データ読み出し）
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        //保存領域から「CONTENT_VALUE」というキーの数値を取得
        showPictureNum = savedInstanceState.getInt("COUNT_VALUE")
        startStopButtonText = savedInstanceState.getString("START_STOP_BUTTON_TEXT")
        startUpFirst = savedInstanceState.getBoolean("START_UP_FIRST")
        if (startStopButtonText == startButtonText) {
            //再生ボタン表示時
//            clickStop()
        } else if (startStopButtonText == stopButtonText) {
            //停止ボタン表示時
//            clickStop()
            clickStart()
        }
    }
}
