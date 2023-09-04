package com.example.facescore

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.facescore.view.formlogin.LoginActivity

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Aguarda 3 segundos e, em seguida, inicia a LoginActivity
    Handler(Looper.getMainLooper()).postDelayed({
      val intent = Intent(this@MainActivity, LoginActivity::class.java)
      startActivity(intent)
      finish() // Encerra a activity atual para que não seja possível voltar a ela
    }, 2000) // 2000 milissegundos = 2 segundos
  }
}
