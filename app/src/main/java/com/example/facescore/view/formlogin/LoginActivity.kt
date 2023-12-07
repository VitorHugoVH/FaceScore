package com.example.facescore.view.formlogin

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.example.facescore.R
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.facescore.view.recuperarsenha.RecuperarSenhaActivity
import com.example.facescore.view.formcadastro.CadastroActivity
import com.example.facescore.view.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

  private val db = FirebaseFirestore.getInstance()
  private val auth = FirebaseAuth.getInstance()
  private val storage = FirebaseStorage.getInstance()
  private val storageRef = storage.reference
  private var isPasswordVisible = false
  private lateinit var googleSignInClient: GoogleSignInClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken("9881181955-t1l1q6jh3htiut4s148n9csuar8v1jq1.apps.googleusercontent.com")
      .requestEmail()
      .build()

    googleSignInClient = GoogleSignIn.getClient(this, gso)

    val btnSingUp = findViewById<Button>(R.id.btn_singup)
    btnSingUp.setOnClickListener { view ->

      val email = findViewById<EditText>(R.id.edit_username).text.toString()
      val senha = findViewById<EditText>(R.id.edit_password).text.toString()

      // Verificação campos email e password preenchidos

      if (email.isEmpty() || senha.isEmpty()) {
        val snackbar = Snackbar.make(view, "Preencha todos os campos!", Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(Color.RED)
        snackbar.show()
      } else {
        auth.signInWithEmailAndPassword(email, senha).addOnCompleteListener { autenticacao ->
          if (autenticacao.isSuccessful) {
            homeNavigation()
          }
        }.addOnFailureListener {
          val snackbar =
            Snackbar.make(view, "Erro ao fazer o login do usuário!", Snackbar.LENGTH_SHORT)
          snackbar.setBackgroundTint(Color.RED)
          snackbar.show()
        }
      }
    }

    val loginWithGoogle =
      findViewById<com.google.android.gms.common.SignInButton>(R.id.btn_signGoogle)
    loginWithGoogle.setOnClickListener {
      singIn()
    }

    // Função de clique para levar o usuário para página de recuperar senha

    val recuperarSenhaLink = findViewById<TextView>(R.id.txt_recuperarSenhaLink)
    recuperarSenhaLink.setOnClickListener {
      val intent = Intent(this, RecuperarSenhaActivity::class.java)
      startActivity(intent)
    }

    // Função de clique para levar o usuário para a página de cadastro

    val cadastroLink = findViewById<TextView>(R.id.txt_semCadastroLink)
    cadastroLink.setOnClickListener {
      val intentCadastro = Intent(this, CadastroActivity::class.java)
      startActivity(intentCadastro)
    }

    // Função para visualizar a senha e esconder a senha

    val togglePasswordButton: ImageButton = findViewById(R.id.togglePassword)
    val passwordEditText: EditText = findViewById(R.id.edit_password)

    togglePasswordButton.setOnClickListener {
      isPasswordVisible = !isPasswordVisible

      if (isPasswordVisible) {
        Color.WHITE
        passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
      } else {
        Color.GRAY
        passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
      }

      passwordEditText.setSelection(passwordEditText.text.length)
    }
  }

  private fun homeNavigation() {
    val intent = Intent(this, HomeActivity::class.java)
    startActivity(intent)
    finish()
  }

  private fun loginComGoogle(token: String) {
    val credencial = GoogleAuthProvider.getCredential(token, null)
    auth.signInWithCredential(credencial).addOnCompleteListener(this) { task: Task<AuthResult> ->
      if (task.isSuccessful) {
        Toast.makeText(baseContext, "Autenticação efetuada com o Google", Toast.LENGTH_SHORT).show()

        // Verifica se o documento do usuário já existe
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid
        if (usuarioId != null) {
          val documentReference = db.collection("Usuarios").document(usuarioId)

          documentReference.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
              // O documento ainda não existe, então cria um
              criarDocumentoUsuario(usuarioId)
            }

            homeNavigation()
          }.addOnFailureListener { exception ->
            // Manipule falhas ao verificar a existência do documento
          }
        }
      } else {
        Toast.makeText(baseContext, "Erro na autenticação com o Google", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun criarDocumentoUsuario(usuarioId: String) {
    // Obtém informações do usuário do Google
    val usuarioGoogle = FirebaseAuth.getInstance().currentUser

    // Verifica se o usuário do Google está autenticado
    if (usuarioGoogle != null) {
      // Obtém as informações do usuário do Google
      val nomeGoogle = usuarioGoogle.displayName
      val emailGoogle = usuarioGoogle.email
      val photoPerfil = usuarioGoogle.photoUrl

      // Crie o documento com o mesmo ID de login do Authentication
      val dadosUsuario = mapOf(
        "nome" to (nomeGoogle ?: ""),
        "email" to (emailGoogle ?: ""),
        "urlImagemPerfil" to (photoPerfil?.toString() ?: ""),
        "contaGoogle" to "Conta Google"
      )

      // Adicione o documento ao Firestore
      db.collection("Usuarios").document(usuarioId)
        .set(dadosUsuario)
        .addOnSuccessListener {
          Log.d("LoginActivity", "Documento do usuário criado com sucesso")

          // Se a URL da imagem de perfil estiver disponível, faça o upload para o Firebase Storage
          if (photoPerfil != null) {
            fazerUploadImagemPerfil(usuarioId, photoPerfil)
          }
        }
        .addOnFailureListener { e ->
          Log.e("LoginActivity", "Erro ao criar documento do usuário", e)
        }
    }
  }

  private fun fazerUploadImagemPerfil(usuarioId: String, photoPerfil: Uri) {
    // Obter a extensão do arquivo da imagem
    val extensaoArquivo = photoPerfil.path?.substringAfterLast(".")
    Log.e("LoginActivity", "A extensão do arquivo é: $extensaoArquivo")

    // Verificar se a extensão é válida (pode adicionar mais validações se necessário)
    if (extensaoArquivo.isNullOrBlank()) {
      Log.e("LoginActivity", "Extensão do arquivo inválida")
      return
    }

    // Criar uma referência específica para o usuário e sua imagem com a extensão do arquivo
    val storageRef = storage.reference.child("imagens_perfil/$usuarioId.$extensaoArquivo")

    storageRef.putFile(photoPerfil)
      .addOnSuccessListener { taskSnapshot ->
        Log.d("LoginActivity", "Upload da imagem de perfil concluído com sucesso")
      }
      .addOnFailureListener { e ->
        Log.e("LoginActivity", "Erro ao fazer upload da imagem de perfil", e)
      }
  }

  // VERIFICAR SE O USUÁRIO ESTÁ LOGADO

  override fun onStart() {
    super.onStart()

    val usuarioAtual = FirebaseAuth.getInstance().currentUser

    if (usuarioAtual != null) {
      homeNavigation()
    }
  }

  private fun singIn() {
    val intent = googleSignInClient.signInIntent
    abreActivity.launch(intent)
  }

  var abreActivity = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result: ActivityResult ->

    if (result.resultCode == RESULT_OK) {
      val intent = result.data
      val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
      try {
        val conta = task.getResult(ApiException::class.java)
        loginComGoogle(conta.idToken!!)
      } catch (exception: ApiException) {

      }
    }
  }
}
