package com.example.facescore.view.classificacao

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.facescore.R
import com.example.facescore.model.UsuarioModel
import com.example.facescore.view.home.HomeActivity
import com.example.facescore.view.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GlobalActivity : AppCompatActivity() {

  private lateinit var imgPerfil: ImageView
  private lateinit var imgReturn: ImageView
  private lateinit var imgNumber1: ImageView
  private lateinit var imgNumber2: ImageView
  private lateinit var imgNumber3: ImageView
  private lateinit var pontuacao1: TextView
  private lateinit var pontuacao2: TextView
  private lateinit var pontuacao3: TextView
  private lateinit var nomeNumber1: TextView
  private lateinit var nomeNumber2: TextView
  private lateinit var nomeNumber3: TextView
  private lateinit var adapter: Adapter
  private lateinit var recyclerView: RecyclerView

  @SuppressLint("MissingInflatedId")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_global)

    // Inicializar as variáveis
    imgPerfil = findViewById(R.id.img_perfil)
    imgReturn = findViewById(R.id.img_return)
    imgNumber1 = findViewById(R.id.img_number1)
    imgNumber2 = findViewById(R.id.img_number2)
    imgNumber3 = findViewById(R.id.img_number3)
    pontuacao1 = findViewById(R.id.pontuacao_1)
    pontuacao2 = findViewById(R.id.pontuacao_2)
    pontuacao3 = findViewById(R.id.pontuacao_3)
    nomeNumber1 = findViewById(R.id.nome_number1)
    nomeNumber2 = findViewById(R.id.nome_number2)
    nomeNumber3 = findViewById(R.id.nome_number3)

    recyclerView = findViewById(R.id.recyclerViewUsuarios)
    adapter = Adapter()
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)

    // Carregar imagem do perfil
    carregarImagemPerfil()

    // Ir para tela de Perfil
    imgPerfil.setOnClickListener {
      val irPerfil = Intent(this, ProfileActivity::class.java)
      startActivity(irPerfil)
    }

    // Ir para tela de Home
    imgReturn.setOnClickListener {
      val irHome = Intent(this, HomeActivity::class.java)
      startActivity(irHome)
    }

    // Carregar informações para o top 3
    carregarTop3Usuarios()

    carregarOutrosUsuarios()
  }

  private fun carregarImagemPerfil() {
    // Obter a referência do documento do usuário atual do Firestore
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val docRef = FirebaseFirestore.getInstance().collection("Usuarios").document(userId!!)

    // Obter a URL da imagem do Firestore
    docRef.get()
      .addOnSuccessListener { document ->
        if (document.exists()) {
          val urlImagemPerfil = document.getString("urlImagemPerfil")
          if (!urlImagemPerfil.isNullOrBlank()) {
            // Usar Glide para carregar a imagem no ImageView, rotacionar e aplicar o raio de borda
            Glide.with(this)
              .load(urlImagemPerfil)
              .transform(CenterCrop(), RoundedCorners(50))
              .into(imgPerfil)
          }
        } else {
          Log.d("Firestore", "O documento não existe")
        }
      }
      .addOnFailureListener { exception ->
        Log.d("Firestore", "Falha ao obter documento: $exception")
      }
  }

  private fun carregarTop3Usuarios() {
    // Adquira a referência dos usuários no top 3 no Firestore
    val db = FirebaseFirestore.getInstance()
    val query = db.collection("Usuarios")
      .orderBy("ranking")
      .limit(3)

    query.get()
      .addOnSuccessListener { documents ->
        if (!documents.isEmpty) {
          for ((index, document) in documents.withIndex()) {
            val urlImagemUsuario = document.getString("urlImagemPerfil")
            val pontuacaoRostoUsuario = document.getDouble("pontuacaoRosto")
            val nomeUsuario = document.getString("nome")

            // Carregar imagem no ImageView correspondente
            val imageView = when (index) {
              0 -> imgNumber1
              1 -> imgNumber2
              2 -> imgNumber3
              else -> null
            }

            imageView?.let {
              if (!urlImagemUsuario.isNullOrBlank()) {
                Glide.with(this)
                  .load(urlImagemUsuario)
                  .transform(CenterCrop(), RoundedCorners(50))
                  .into(it)
              }
            }

            // Exibir pontuação no TextView correspondente
            val pontuacaoTextView = when (index) {
              0 -> pontuacao1
              1 -> pontuacao2
              2 -> pontuacao3
              else -> null
            }

            pontuacaoTextView?.let {
              if (pontuacaoRostoUsuario != null) {
                it.text = pontuacaoRostoUsuario.toString()
              }
            }

            // Exibir o nome no TextView correspondente (apenas a primeira palavra)
            val nomeTextView = when (index) {
              0 -> nomeNumber1
              1 -> nomeNumber2
              2 -> nomeNumber3
              else -> null
            }

            nomeTextView?.let {
              if (!nomeUsuario.isNullOrBlank()) {
                val primeiraPalavra = nomeUsuario.split(" ")[0]
                it.text = primeiraPalavra
              }
            }
          }
        } else {
          Log.d("Firestore", "Nenhum usuário encontrado.")
        }
      }
      .addOnFailureListener { exception ->
        Log.d("Firestore", "Falha ao obter usuários: $exception")
      }
  }

  private fun carregarOutrosUsuarios() {
    Log.d("Firestore", "Iniciando carregarOutrosUsuarios()")
    // Adquira a referência dos usuários a partir do ranking 4 no Firestore
    val db = FirebaseFirestore.getInstance()
    val query = db.collection("Usuarios")
      .orderBy("ranking")
      .startAt(4)

    query.get()
      .addOnSuccessListener { documents ->
        if (!documents.isEmpty) {
          val userList = mutableListOf<UsuarioModel>()

          for (document in documents) {
            val urlImagemUsuario = document.getString("urlImagemPerfil") ?: ""
            val pontuacaoRostoUsuario = document.getDouble("pontuacaoRosto") ?: 0.0
            val nomeUsuario = document.getString("nome") ?: ""
            val rankingUsuario = document.getLong("ranking")?.toInt() ?: 0

            val usuario = UsuarioModel(rankingUsuario, urlImagemUsuario, nomeUsuario, pontuacaoRostoUsuario)
            userList.add(usuario)
          }

          // Atualizar o Adapter com a nova lista de usuários
          adapter.atualizarLista(userList)
        }

        Log.d("Firestore", "carregarOutrosUsuarios() concluído com sucesso.")
      }
  }
}
