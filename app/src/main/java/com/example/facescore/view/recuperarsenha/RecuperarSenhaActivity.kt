package com.example.facescore.view.recuperarsenha

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facescore.R
import com.example.facescore.view.formlogin.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class RecuperarSenhaActivity : AppCompatActivity() {

  private lateinit var etPassword: EditText
  private lateinit var btnResetPassword: Button
  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_recuperarsenha)

    // Funções para retornar para o login

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

    // Função para redefinir a senha

    etPassword = findViewById(R.id.input_email)
    btnResetPassword = findViewById(R.id.btn_email)

    auth = FirebaseAuth.getInstance()

    btnResetPassword.setOnClickListener {
      val sPassword = etPassword.text.toString()
      auth.sendPasswordResetEmail(sPassword)
        .addOnSuccessListener {
          Toast.makeText(this, "Por favor, verifique seu email.", Toast.LENGTH_LONG).show()
          val intent = Intent(this, LoginActivity::class.java)
          startActivity(intent)
        } .addOnFailureListener {
          Toast.makeText(this, "Erro ao enviar o email, usuário excluido ou inexistente.", Toast.LENGTH_SHORT).show()
        }
    }
  }
}
