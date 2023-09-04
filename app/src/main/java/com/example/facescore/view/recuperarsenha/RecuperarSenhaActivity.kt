package com.example.facescore.view.recuperarsenha

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.facescore.R
import com.example.facescore.view.formlogin.LoginActivity

class RecuperarSenhaActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_recuperarsenha)

    val imgRetornarLogin = findViewById<ImageView>(R.id.img_return)
    imgRetornarLogin.setOnClickListener{
      val intent = Intent(this, LoginActivity::class.java)
      startActivity(intent)
    }

    val txtLogIn = findViewById<TextView>(R.id.txt_LogIn)
    txtLogIn.setOnClickListener{
      val intentLogin = Intent(this, LoginActivity::class.java)
      startActivity(intentLogin)
    }
  }
}
