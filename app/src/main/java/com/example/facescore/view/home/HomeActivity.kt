package com.example.facescore.view.home

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.example.facescore.R
import com.example.facescore.view.formlogin.LoginActivity

class HomeActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)

    val btnLeave = findViewById<ImageButton>(R.id.btn_leave)
    btnLeave.setOnClickListener{
      val intent = Intent(this, LoginActivity::class.java)
      startActivity(intent)
    }
  }
}