package com.example.contentproviderandroid

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.SearchView
import android.widget.SimpleCursorAdapter
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //contentResolver.queryで抽出したい項目
    var cols = listOf<String>(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone._ID
    ).toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * パーミッションの許可が無い場合、許可ダイアログを表示
         */
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                Array(1) { Manifest.permission.READ_CONTACTS },
                111
            )
        } else {
            readContact()
        }
    }

    /**
     * 許可ダイアログの結果を受け取る
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            readContact()
        }
    }

    /**
     * 電話帳を読み込んでListViewへ渡す
     */
    private fun readContact() {
        //SQLiteのCursor
        //(検索したいURI, 抽出したい項目, 絞り込み条件, 絞り込みパラメータ, ソート)
        var rs = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            cols, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )

        //表示するColumn
        val from = listOf<String>(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ).toTypedArray()

        //バインドするID
        val to = intArrayOf(android.R.id.text1, android.R.id.text2)

        //ListViewへ渡す
        //(コンテキスト,表示するレイアウト,SQLiteのCursor,表示するColumn,バインドするID,？)
        val adapter =
            SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, rs, from, to, 0)
        listview1.adapter = adapter

        /**
         * 入力イベントの処理
         */
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            /**
             * 検索ボタンが押下された時
             */
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            /**
             * テキストが変更された時
             */
            override fun onQueryTextChange(p0: String?): Boolean {
                rs = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    cols,
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                    Array(1) { "%$p0%" },
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
                //変更をアダプタに通知
                adapter.changeCursor(rs)
                return false
            }
        })
    }
}

