package com.example.facescore.view.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.facescore.R
import com.example.facescore.view.config.SettingsActivity
import com.example.facescore.view.formlogin.LoginActivity
import com.example.facescore.view.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)

    // Função para deslogar do sistema

    val btnLeave = findViewById<ImageButton>(R.id.btn_leave)
    btnLeave.setOnClickListener{
      FirebaseAuth.getInstance().signOut()
      val voltarTelaLogin = Intent(this, LoginActivity::class.java)
      startActivity(voltarTelaLogin)
      finish()
    }

    // Função botão ir para tela de profile

    val btnProfile = findViewById<ImageButton>(R.id.btn_profille)
    btnProfile.setOnClickListener {
      val telaProfile = Intent(this, ProfileActivity::class.java)
      startActivity(telaProfile)
    }

    // Função botão ir para tela de configurações

    val btnConfig = findViewById<ImageButton>(R.id.btn_config)
    btnConfig.setOnClickListener {
      val telaConfig = Intent(this, SettingsActivity::class.java)
      startActivity(telaConfig)
    }
  }
}