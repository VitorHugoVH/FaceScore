package com.example.facescore.view.formcadastro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.facescore.R
import com.example.facescore.view.formlogin.LoginActivity
import com.example.facescore.view.recuperarsenha.RecuperarSenhaActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class CadastroActivity : AppCompatActivity() {

  private val auth = FirebaseAuth.getInstance()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_cadastro)

    val ImgReturn = findViewById<ImageView>(R.id.img_return)
    ImgReturn.setOnClickListener {

      val intentImgReturn = Intent(this, LoginActivity::class.java)
      startActivity(intentImgReturn)
    }

    val BtnSingUp = findViewById<Button>(R.id.btn_singup)
    BtnSingUp.setOnClickListener {view ->

      val firstName = findViewById<EditText>(R.id.input_first_name).text.toString()
      val lastName = findViewById<EditText>(R.id.input_last_name).text.toString()

      val spinner = findViewById<Spinner>(R.id.input_pais)
      val selectedCountry = spinner.selectedItem.toString()

      val cep = findViewById<EditText>(R.id.input_cep).text.toString()
      val state = findViewById<EditText>(R.id.input_state).text.toString()
      val city = findViewById<EditText>(R.id.input_city).text.toString()

      val email = findViewById<EditText>(R.id.input_email).text.toString()
      val password = findViewById<EditText>(R.id.input_password).text.toString()

      val dateOfBirth = findViewById<EditText>(R.id.input_aniversario).text.toString()

      val checkbox = findViewById<CheckBox>(R.id.check_declaracao).isChecked

      if(firstName.isEmpty() || lastName.isEmpty() || selectedCountry.isEmpty() || cep.isEmpty() || state.isEmpty() || city.isEmpty() || email.isEmpty() || password.isEmpty() || dateOfBirth.isEmpty() || !checkbox){
        val snackbar = Snackbar.make(view, "Preencha todos os campos!", Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(Color.MAGENTA)
        snackbar.show()
      }else {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ cadastro ->
          if (cadastro.isSuccessful){
            val snackbarSuccess = Snackbar.make(view, "Usuário cadastrado com sucesso!", Snackbar.LENGTH_SHORT)
            snackbarSuccess.setBackgroundTint(Color.GREEN)
            snackbarSuccess.show()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
          }
        }.addOnFailureListener{ exception ->

          val mensagemErro = when(exception){
            is FirebaseAuthWeakPasswordException -> "Digite uma senha com no mínimo 6 caracteres!"
            is FirebaseAuthInvalidCredentialsException -> "Digite um email válido!"
            is FirebaseAuthUserCollisionException -> "Esta conta já foi cadastrada!"
            is FirebaseNetworkException -> "Sem conexão com a internet!"
            else -> "Erro ao cadastrar usuário!"
          }
          val snackbarErro = Snackbar.make(view, mensagemErro, Snackbar.LENGTH_SHORT)
          snackbarErro.setBackgroundTint(Color.RED)
          snackbarErro.show()
        }
      }
    }
  }
}
