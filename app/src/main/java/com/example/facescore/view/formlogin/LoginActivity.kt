package com.example.facescore.view.formlogin

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.example.facescore.R
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import com.example.facescore.view.recuperarsenha.RecuperarSenhaActivity
import com.example.facescore.view.formcadastro.CadastroActivity
import com.example.facescore.view.home.HomeActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

  private val auth = FirebaseAuth.getInstance()
  private var isPasswordVisible = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    val btnSingUp = findViewById<Button>(R.id.btn_singup)
    btnSingUp.setOnClickListener{ view ->

      val email = findViewById<EditText>(R.id.edit_username).text.toString()
      val senha = findViewById<EditText>(R.id.edit_password).text.toString()

      // Verificação campos email e password preenchidos

      if(email.isEmpty() || senha.isEmpty()){
        val snackbar = Snackbar.make(view, "Preencha todos os campos!", Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(Color.RED)
        snackbar.show()
      }else {
        auth.signInWithEmailAndPassword(email, senha).addOnCompleteListener { autenticacao ->
          if (autenticacao.isSuccessful){
            homeNavigation()
          }
        }.addOnFailureListener {
          val snackbar = Snackbar.make(view, "Erro ao fazer o login do usuário!", Snackbar.LENGTH_SHORT)
          snackbar.setBackgroundTint(Color.RED)
          snackbar.show()
        }
      }
    }

    // Função de clique para levar o usuário para página de recuperar senha

    val recuperarSenhaLink = findViewById<TextView>(R.id.txt_recuperarSenhaLink)
    recuperarSenhaLink.setOnClickListener{
      val intent = Intent(this, RecuperarSenhaActivity::class.java)
      startActivity(intent)
    }

    // Função de clique para levar o usuário para a página de cadastro

    val cadastroLink = findViewById<TextView>(R.id.txt_semCadastroLink)
    cadastroLink.setOnClickListener{
      val intentCadastro = Intent(this, CadastroActivity::class.java)
      startActivity(intentCadastro)
    }

    // Função para visualizar a senha e esconder a senha

    val togglePasswordButton: ImageButton = findViewById(R.id.togglePassword)
    val passwordEditText: EditText = findViewById(R.id.edit_password)

    togglePasswordButton.setOnClickListener{
      isPasswordVisible = !isPasswordVisible

      if(isPasswordVisible) {
        Color.WHITE
        passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
      } else {
        Color.GRAY
        passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
      }

      passwordEditText.setSelection(passwordEditText.text.length)
    }

  }

  private fun homeNavigation(){
    val intent = Intent(this, HomeActivity::class.java)
    startActivity(intent)
    finish()
  }
}
