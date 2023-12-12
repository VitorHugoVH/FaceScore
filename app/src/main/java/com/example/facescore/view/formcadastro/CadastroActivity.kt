package com.example.facescore.view.formcadastro

import CepMaskWatcher
import DateMaskWatcher
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.facescore.R
import com.example.facescore.view.formlogin.LoginActivity
import com.example.facescore.view.recuperarsenha.RecuperarSenhaActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CadastroActivity : AppCompatActivity() {

  private val auth = FirebaseAuth.getInstance()
  private val db =  FirebaseFirestore.getInstance()
  private var isPasswordVisible = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_cadastro)

    // Funções de retorno

    val ImgReturn = findViewById<ImageView>(R.id.img_return)
    ImgReturn.setOnClickListener {

      val intentImgReturn = Intent(this, LoginActivity::class.java)
      startActivity(intentImgReturn)
    }

    // Aplicando função campo senha

    val inputPassword = findViewById<EditText>(R.id.input_password)
    val imageEye = findViewById<ImageButton>(R.id.img_eye)

    imageEye.setOnClickListener{
      isPasswordVisible = !isPasswordVisible

      if(isPasswordVisible) {
        inputPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
      } else {
        inputPassword.transformationMethod = PasswordTransformationMethod.getInstance()
      }

      inputPassword.setSelection(inputPassword.text.length)
    }

    // Aplicando máscara CEP

    val inputCep = findViewById<EditText>(R.id.input_cep)
    inputCep.addTextChangedListener(CepMaskWatcher(inputCep))

    // Aplicando máscara Data Aniversário

    val inputAniversario = findViewById<EditText>(R.id.input_aniversario)
    inputAniversario.addTextChangedListener(DateMaskWatcher(inputAniversario))

    // Função fazer login

    val BtnSingUp = findViewById<Button>(R.id.btn_singup)
    BtnSingUp.setOnClickListener {view ->

      val firstName = findViewById<EditText>(R.id.input_first_name).text.toString()

      val spinner = findViewById<Spinner>(R.id.input_pais)
      val selectedCountry = spinner.selectedItem.toString()

      val genero = findViewById<Spinner>(R.id.input_genero)
      val selectedGenero = genero.selectedItem.toString()

      val cep = findViewById<EditText>(R.id.input_cep).text.toString()
      val state = findViewById<EditText>(R.id.input_state).text.toString()
      val city = findViewById<EditText>(R.id.input_city).text.toString()

      val email = findViewById<EditText>(R.id.input_email).text.toString()
      val password = findViewById<EditText>(R.id.input_password).text.toString()

      val dateOfBirth = findViewById<EditText>(R.id.input_aniversario).text.toString()

      val checkbox = findViewById<CheckBox>(R.id.check_declaracao).isChecked

      if(firstName.isEmpty() || selectedCountry.isEmpty() || selectedGenero.isEmpty() || cep.isEmpty() || state.isEmpty() || city.isEmpty() || email.isEmpty() || password.isEmpty() || dateOfBirth.isEmpty() || !checkbox){
        val snackbar = Snackbar.make(view, "Preencha todos os campos!", Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(Color.MAGENTA)
        snackbar.show()
      }else {

        val usuariosMap = hashMapOf(
          "nome" to firstName,
          "pais" to selectedCountry,
          "cep" to cep,
          "estado" to state,
          "cidade" to city,
          "email" to email,
          "senha" to password,
          "nascimento" to dateOfBirth,
          "genero" to selectedGenero,
          "dataCriacao" to FieldValue.serverTimestamp(),
          "privado" to false
        )

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ cadastro ->
          if (cadastro.isSuccessful){

            val usuarioId = FirebaseAuth.getInstance().currentUser?.uid

            if (usuarioId != null) {
              db.collection("Usuarios").document(usuarioId)
                .set(usuariosMap)
                .addOnCompleteListener { task ->
                  if (task.isSuccessful) {
                    Log.d("db", "Sucesso ao salvar os dados do usuário")
                  } else {
                    Log.e("db", "Erro ao salvar dados do usuário", task.exception)
                  }
                }
            }

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
