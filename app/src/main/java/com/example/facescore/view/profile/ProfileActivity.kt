package com.example.facescore.view.profile

import DateMaskWatcher
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.RoundedCorner
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.facescore.R
import com.example.facescore.view.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

  private val db = FirebaseFirestore.getInstance()
  private val storage = FirebaseStorage.getInstance()
  private val storageRef = storage.reference
  private lateinit var nomeUsuario: TextView
  private lateinit var nomeCompleto: EditText
  private lateinit var emailUsuario: EditText
  private lateinit var cidadeUsuario: EditText
  private lateinit var nascimentoUsuario: EditText
  private lateinit var generoMas: Switch
  private lateinit var generoFem: Switch
  private lateinit var imgPerfil: ImageView
  private lateinit var btnSalvar: Button
  private lateinit var progressBar: ProgressBar

  private val PICK_IMAGE_REQUEST = 1
  private var imagemUri: Uri? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_profile)

    // Armazenar campos perfil em variáveis
    nomeUsuario = findViewById<TextView>(R.id.txt_nome_user)
    nomeCompleto = findViewById<EditText>(R.id.input_username)
    emailUsuario = findViewById<EditText>(R.id.input_email)
    cidadeUsuario = findViewById<EditText>(R.id.input_telefone)
    nascimentoUsuario = findViewById<EditText>(R.id.input_date)
    generoMas = findViewById<Switch>(R.id.switch_male)
    generoFem = findViewById<Switch>(R.id.switch_female)
    imgPerfil = findViewById<ImageView>(R.id.img_photo_perfil)
    btnSalvar = findViewById<Button>(R.id.btn_salvarData)
    progressBar = findViewById(R.id.progressBar)

    // Função para retornar home
    val imgReturn = findViewById<ImageView>(R.id.img_return)
    imgReturn.setOnClickListener {
      val intent = Intent(this, HomeActivity::class.java)
      startActivity(intent)
    }

    // Configura listeners para os Switches
    generoMas.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        generoFem.isChecked = false
      }
    }

    generoFem.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        generoMas.isChecked = false
      }
    }

    btnSalvar.setOnClickListener {

      btnSalvar.isEnabled = false
      progressBar.visibility = View.VISIBLE

      // Obter os novos valores dos campos
      val novoNomeCompleto = nomeCompleto.text.toString()
      val novoEmail = emailUsuario.text.toString()
      val novaCidade = cidadeUsuario.text.toString()
      val novoNascimento = nascimentoUsuario.text.toString()
      val novoGenero = if (generoMas.isChecked) "Masculino" else "Feminino"

      // Atualizar os dados no Firestore
      atualizarDadosUsuario(novoNomeCompleto, novoEmail, novaCidade, novoNascimento, novoGenero)
    }

    // Configura listener para a ImageView de perfil
    imgPerfil.setOnClickListener {
      abrirGaleria()
    }

    // Aplicar máscara no EditText aniversário
    val inputAniversario = findViewById<EditText>(R.id.input_date)
    inputAniversario.addTextChangedListener(DateMaskWatcher(inputAniversario))

    // Resgatar dados do usuário
    obterDadosUsuario()
  }

  private fun abrirGaleria() {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    startActivityForResult(intent, PICK_IMAGE_REQUEST)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
      val imageUri: Uri? = data.data
      if (imageUri != null) {
        imgPerfil.setImageURI(imageUri)
        this.imagemUri = imageUri // Certifique-se de definir a variável global
      } else {
        Log.e("ProfileActivity", "A URI da imagem está nula.")
      }
    }
  }

  private fun atualizarDadosUsuario(
    novoNome: String,
    novoEmail: String,
    novaCidade: String,
    novoNascimento: String,
    novoGenero: String
  ) {
    val usuarioAtual = FirebaseAuth.getInstance().currentUser

    usuarioAtual?.let { usuario ->
      val uid = usuario.uid
      val documentReference = db.collection("Usuarios").document(uid)

      // Mapa contendo os novos valores a serem atualizados no Firestore
      val dadosAtualizados = mapOf(
        "nome" to novoNome,
        "email" to novoEmail,
        "cidade" to novaCidade,
        "nascimento" to novoNascimento,
        "genero" to novoGenero,
        "urlImagemPerfil" to "" // Inicializa o campo urlImagemPerfil com uma string vazia
      )

      // Atualizar os dados no Firestore
      documentReference.update(dadosAtualizados)
        .addOnSuccessListener {
          Log.d("ProfileActivity", "Dados do usuário atualizados com sucesso")

          // Atualizar o e-mail no Firebase Authentication
          usuario.updateEmail(novoEmail)
            .addOnSuccessListener {
              Log.d("ProfileActivity", "E-mail do usuário atualizado com sucesso")

              // Fazer upload da imagem se uma nova imagem foi selecionada
              Log.d("ProfileActivity", "Chamando fazerUploadImagem")
              Log.d("ProfileActivity", "o valor de imagemUri: $imagemUri")
              if (imagemUri != null) {
                fazerUploadImagem(uid)
              }

              btnSalvar.isEnabled = true
              progressBar.visibility = View.INVISIBLE
            }
            .addOnFailureListener { e ->
              Log.e("ProfileActivity", "Erro ao atualizar e-mail do usuário", e)
              btnSalvar.isEnabled = true
              progressBar.visibility = View.INVISIBLE
            }
        }
        .addOnFailureListener { e ->
          Log.e("ProfileActivity", "Erro ao atualizar dados do usuário", e)
          btnSalvar.isEnabled = true
          progressBar.visibility = View.INVISIBLE
        }
    }
  }

  private fun fazerUploadImagem(usuarioId: String) {
    // Obter a extensão do arquivo da imagem
    val extensaoArquivo = imagemUri?.lastPathSegment?.substringAfterLast(".")
    Log.e("ProfileActivity", "A extensão do arquivo é: $extensaoArquivo")

    // Verificar se a extensão é válida (pode adicionar mais validações se necessário)
    if (extensaoArquivo.isNullOrBlank()) {
      Log.e("ProfileActivity", "Extensão do arquivo inválida")
      return
    }

    Log.e("ProfileActivity", "Entrou na função fazerUploadImagem")

    // Criar uma referência específica para o usuário e sua imagem com a extensão do arquivo
    val storageRef = storage.reference.child("imagens_perfil/$usuarioId.$extensaoArquivo")

    storageRef.putFile(imagemUri!!)
      .addOnSuccessListener { taskSnapshot ->
        // Obter a URL da imagem após o upload bem-sucedido
        storageRef.downloadUrl.addOnSuccessListener { uri ->
          // Criar um novo mapa apenas com a URL da imagem
          val dadosImagem = mapOf("urlImagemPerfil" to uri.toString())

          // Atualizar apenas o campo "urlImagemPerfil" no Firestore
          db.collection("Usuarios").document(usuarioId)
            .set(dadosImagem, SetOptions.merge())
            .addOnSuccessListener {
              Log.d("ProfileActivity", "URL da imagem atualizada no Firestore com sucesso")
            }
            .addOnFailureListener { e ->
              Log.e("ProfileActivity", "Erro ao atualizar URL da imagem no Firestore", e)
            }
        }
      }
      .addOnFailureListener { e ->
        Log.e("ProfileActivity", "Erro ao fazer upload da imagem", e)
      }
  }

  private fun obterDadosUsuario() {
    val usuarioId = FirebaseAuth.getInstance().currentUser?.uid

    usuarioId?.let { uid ->
      Log.d("ProfileActivity", "UID coletado: $uid")

      val documentReference = db.collection("Usuarios").document(uid)

      documentReference.addSnapshotListener { documentSnapshot, e ->
        if (e != null) {
          Log.e("ProfileActivity", "Erro ao ler dados do Firestore", e)
          return@addSnapshotListener
        }

        if (documentSnapshot != null && documentSnapshot.exists()) {

          val nome = documentSnapshot.getString("nome")
          val email = documentSnapshot.getString("email")
          val cidade = documentSnapshot.getString("cidade")
          val nascimento = documentSnapshot.getString("nascimento")
          val genero = documentSnapshot.getString("genero")
          val urlImagemPerfil = documentSnapshot.getString("urlImagemPerfil")

          nomeUsuario.text = nome
          nomeCompleto.setText(nome)
          emailUsuario.setText(email)
          cidadeUsuario.setText(cidade)
          nascimentoUsuario.setText(nascimento)

          generoMas.isChecked = genero == "Masculino"
          generoFem.isChecked = genero == "Feminino"

          // Carregar a imagem no ImageView
          if (!urlImagemPerfil.isNullOrBlank()) {
            Glide.with(this)
              .load(urlImagemPerfil)
              .transform(CenterCrop(), RoundedCorners(100))
              .into(imgPerfil)
          }

        } else {
          Log.w("ProfileActivity", "Documento não existe ou está vazio")
        }
      }
    }
  }
}
