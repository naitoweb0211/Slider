package jp.techacademy.yuki.naito.slider

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Handler
import java.util.*
class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100
    private var mTimer: Timer? = null
    private var mHandler = Handler()
    var fieldIndex = 0
    var id: Long = 0
    var position = 0
    private var cursor: Cursor? = null
    var imageUri: Uri? = null
    // タイマー用の時間のための変数
    var mTimerSec = 0.0
    // タイマー用の時間のための変数
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTimer = Timer()
        start_button.setOnClickListener {
            // Android 6.0以降の場合
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // パーミッションの許可状態を確認する
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    getContentsInfo()
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                }
                // Android 5系以下の場合
            } else {
            }
        }

        next_button.setOnClickListener {
            getNextContentsInfo()
        }

        back_button.setOnClickListener {
            getPreviousContentsInfo()
        }
    }

    // requestPermissionsの後に呼び出される
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // 許可した後の処理
            getContentsInfo()
        } else {
            return
        }
    }
    private fun getNextContentsInfo() {
        if (mTimer != null){
            mTimer!!.cancel()
            mTimer = null
        }
        /*
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        var imageUri = ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            id
        )
        imageView.setImageURI(imageUri)
        cursor!!.moveToPosition(position)
        cursor.moveToNext()
        position = cursor!!.getPosition()
        id ++*/
        cursor!!.moveToPosition(position)
        if (cursor!!.isLast) {
            cursor!!.moveToFirst()
            position = cursor!!.getPosition()
            fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            id = cursor!!.getLong(fieldIndex)
            imageUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            imageView.setImageURI(imageUri)
        }else{
            cursor!!.moveToNext()
            position = cursor!!.getPosition()
            fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            id = cursor!!.getLong(fieldIndex)
            imageUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            imageView.setImageURI(imageUri)
        }
    }

    private fun getPreviousContentsInfo() {
        if (mTimer != null){
            mTimer!!.cancel()
            mTimer = null
        }
        cursor!!.moveToPosition(position)
        if (cursor!!.isFirst) {
            cursor!!.moveToLast()
            position = cursor!!.getPosition()
            fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            id = cursor!!.getLong(fieldIndex)
            imageUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            imageView.setImageURI(imageUri)
        }else{
            cursor!!.moveToPrevious()
            position = cursor!!.getPosition()
            fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            id = cursor!!.getLong(fieldIndex)
            imageUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            imageView.setImageURI(imageUri)
        }
    }

    private fun getContentsInfo() {
        if (start_button.text.equals("再生")) {
            start_button.setText("停止")
            next_button.isClickable = false
            back_button.isClickable = false
            // タイマーの始動
            val resolver = contentResolver
            cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目（null = 全項目）
                null, // フィルタ条件（null = フィルタなし）
                null, // フィルタ用パラメータ
                null // ソート (nullソートなし）
            )
            Log.d("fieldIndex", "再生を始めました")
            if (fieldIndex == 0)
                cursor!!.moveToFirst()
            else
                cursor!!.moveToPosition(position)
            mTimer = Timer()
            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mTimerSec += 1
                    mHandler.post {
                        // indexからIDを取得し、そのIDから画像のURIを取得する
                        fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                        id = cursor!!.getLong(fieldIndex)
                        imageUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        Log.d("ANDROID", "URI : " + imageUri.toString())
                        imageView.setImageURI(imageUri)
                        Log.d("fieldIndex", "id: " + id)
                        id += 1
                        if (!cursor!!.isLast())
                            cursor!!.moveToNext()
                        else cursor!!.moveToFirst()
                        position = cursor!!.getPosition()
                    }
                }
            }, 1000, 1000) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒
        }else{
            mTimer!!.cancel()
            mTimer = null
            start_button.setText("再生")
            next_button.isClickable = true
            back_button.isClickable = true
            if (!cursor!!.isFirst())
                cursor!!.moveToPrevious()
            else cursor!!.moveToLast(

            )
            position = cursor!!.getPosition()
        }
    }
        //cursor.close()
}