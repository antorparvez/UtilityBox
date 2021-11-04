package com.antorparvez.uiutils

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.antorparvez.utilitybox.UtilityBox.displayImage
import com.antorparvez.utilitybox.UtilityBox.loadImage

class MainActivity : AppCompatActivity() {
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv= findViewById<ImageView>(R.id.tv)

        tv.loadImage(this,"https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg",R.mipmap.image_foreground,R.mipmap.ic_launcher)
    }
}