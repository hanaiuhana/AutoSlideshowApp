package jp.techacademy.yui.tanakayui.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URI

class MainActivity : AppCompatActivity() {
    private var startStopButtonText :String = ""
    private var startButtonText :String = ""
    private var stopButtonText :String = ""

    private val PERMISSIONS_REQUEST_CODE = 100

    private var count :Int = 0
    private var showPictureNum = 0
    private val uriArrayList = arrayListOf<Uri>()
    private var pictureNum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //再生・停止ボタンの表示する文字列を取得
        startButtonText = resources.getString(R.string.start_button_text)
        stopButtonText = resources.getString(R.string.stop_button_text)

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
                start_stop_button.setText(R.string.stop_button_text)
                forward_button.isClickable = false
                back_button.isClickable = false
            } else if (startStopButtonText == stopButtonText) {
                start_stop_button.setText(R.string.start_button_text)
                forward_button.isClickable = true
                back_button.isClickable = true
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
                    //ボタンを押すと、アプリが落ちるので、対応策を考える
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
            picture.setImageURI((uriArrayList[0]))
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
}
