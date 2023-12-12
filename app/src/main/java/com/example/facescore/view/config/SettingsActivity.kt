package com.example.facescore.view.config;

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.firestore.Query
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.facescore.R
import com.example.facescore.view.formlogin.LoginActivity
import com.example.facescore.view.home.HomeActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.checkerframework.checker.units.qual.Length

class SettingsActivity : AppCompatActivity() {

  private val db = FirebaseFirestore.getInstance()
  private lateinit var switchPrivacidade: Switch
  private lateinit var switchNotificacoes: Switch

  @SuppressLint("MissingInflatedId", "WrongViewCast")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_settings)

    // Receber id do usuário
    val usuarioAtual = FirebaseAuth.getInstance().currentUser

    // Inicializar Switches
    switchPrivacidade = findViewById(R.id.switch_privacidade)
    switchNotificacoes = findViewById(R.id.switch_notificacoes)

    // Função botão retornar para a página Home
    val btnReturn = findViewById<ImageView>(R.id.img_return_home)
    btnReturn.setOnClickListener {
      val voltarHome = Intent(this, HomeActivity::class.java)
      startActivity(voltarHome)
    }

    // Função habilitar privacidade de conta
    switchPrivacidade.setOnCheckedChangeListener { _, isChecked ->
      atualizarPrivacidade(usuarioAtual?.uid, isChecked)
    }

    // Função para habilitar notificações
    switchNotificacoes.setOnCheckedChangeListener { _, isChecked ->
      atualizarNotificacoes(usuarioAtual?.uid, isChecked)
    }

    // Carregar e definir os estados iniciais dos Switches
    carregarEstadosSwitches(usuarioAtual?.uid)

    val btn_excluir = findViewById<ImageButton>(R.id.btn_excluir_conta)
    btn_excluir.setOnClickListener {
      val builder = AlertDialog.Builder(this)
      builder.setTitle("Confirmação")
      builder.setMessage("Tem certeza que deseja excluir sua conta? Esta ação não pode ser desfeita.")

      builder.setPositiveButton("Sim") {_, _ ->
        excluirConta(usuarioAtual)
      }

      builder.setNegativeButton("Não") {_, _ ->

      }

      val dialog = builder.create()
      dialog.show()
    }
  }

  private fun carregarEstadosSwitches(usuarioId: String?) {
    usuarioId?.let { uid ->
      val documentReference = db.collection("Usuarios").document(uid)

      documentReference.get()
        .addOnSuccessListener { documentSnapshot ->
          if (documentSnapshot.exists()) {
            val privacidade = documentSnapshot.getBoolean("privado") ?: false
            val notificacao = documentSnapshot.getBoolean("notificacao") ?: false

            // Definir estados iniciais dos Switches
            switchPrivacidade.isChecked = privacidade
            switchNotificacoes.isChecked = notificacao
          }
        }
        .addOnFailureListener { e ->
          Log.e("SettingsActivity", "Erro ao carregar estados dos Switches: ${e.message}")
          Toast.makeText(this, "Erro ao carregar estados dos Switches", Toast.LENGTH_SHORT).show()
        }
    }
  }

  private fun recalcularRankings() {
    val usuariosRef = db.collection("Usuarios")

    usuariosRef
      .whereEqualTo("privado", false)
      .orderBy("pontuacaoRosto", Query.Direction.DESCENDING)
      .get()
      .addOnSuccessListener { result ->
        if (!result.isEmpty) {
          val usuariosOrdenados = result.documents.sortedByDescending {
            it.getDouble("pontuacaoRosto") ?: 0.0
          }

          var ranking = 1
          for (usuario in usuariosOrdenados) {
            val userId = usuario.id
            atualizarPosicaoRanking(userId, ranking)
            ranking++
          }

          Log.d("SettingsActivity", "Rankings recalculados com sucesso")
        }
      }
      .addOnFailureListener { exception ->
        Log.e("SettingsActivity", "Falha ao recalcular rankings: $exception")
      }
  }

  private fun atualizarPosicaoRanking(userId: String, novaPosicao: Int) {
    val userRef = FirebaseFirestore.getInstance().collection("Usuarios").document(userId)

    // Atualizar o campo ranking
    userRef.update("ranking", novaPosicao)
      .addOnSuccessListener {
        Log.d("Firestore", "Posição do ranking atualizada com sucesso para $novaPosicao!")
      }
      .addOnFailureListener { exception ->
        Log.e("Firestore", "Falha ao atualizar posição do ranking: $exception")
      }
  }

  private fun atualizarPrivacidade(usuarioId: String?, privado: Boolean) {
    usuarioId?.let { uid ->
      val documentReference = db.collection("Usuarios").document(uid)

      // Atualiza o campo "privado"
      documentReference.update("privado", privado)
        .addOnSuccessListener {
          val mensagem = if (privado) "Sua conta está privada" else "Sua conta está pública"
          val toastPrivacidade = Toast.makeText(this, mensagem, Toast.LENGTH_SHORT)
          toastPrivacidade.show()

          // Recalcula os rankings
          recalcularRankings()

          // Se a conta estava privada e agora é pública, remove o campo "ranking"
          if (privado) {
            documentReference.update("ranking", FieldValue.delete())
              .addOnSuccessListener {
                Log.d("SettingsActivity", "Campo 'ranking' removido com sucesso")
              }
              .addOnFailureListener { e ->
                Log.e("SettingsActivity", "Erro ao remover campo 'ranking': ${e.message}")
              }
          }
        }
        .addOnFailureListener { e ->
          val toastErro = Toast.makeText(this, "Erro ao realizar a alteração", Toast.LENGTH_SHORT)
          toastErro.show()
        }
    }
  }

  private fun atualizarNotificacoes(usuarioId: String?, notificacao: Boolean) {
    usuarioId?.let { uid ->
      val documentReference = db.collection("Usuarios").document(uid)

      documentReference.update("notificacao", notificacao)
        .addOnSuccessListener {
          if(notificacao) {
            val toastNotificacaoAtivado = Toast.makeText(this, "Notificações permitidas", Toast.LENGTH_SHORT)
            toastNotificacaoAtivado.show()
          } else {
            val toastNotificacaoDesativado = Toast.makeText(this, "Notificações desabilitadas", Toast.LENGTH_SHORT)
            toastNotificacaoDesativado.show()
          }
        }
        .addOnFailureListener { e ->
          val toastErro = Toast.makeText(this, "Erro ao realizar a alteração", Toast.LENGTH_SHORT)
          toastErro.show()
        }
    }
  }

  private fun excluirConta(usuario: FirebaseUser?) {
    usuario?.let { user ->
      // Se o usuário tem uma senha, tenta reautenticar e excluir os dados
      if (user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID } && user.email != null) {
        // Verifica se o usuário possui uma senha
        user.getIdToken(false).addOnSuccessListener { result ->
          val hasPassword = result.claims["hasPassword"] as? Boolean ?: false

          if (hasPassword) {
            val credential = EmailAuthProvider.getCredential(user.email!!, "senha_atual_do_usuario")
            user.reauthenticate(credential)
              .addOnSuccessListener {
                Log.d("ExcluirConta", "Reautenticação bem-sucedida")
                excluirDados(user)
              }
              .addOnFailureListener { e ->
                Log.e("ExcluirConta", "Erro ao reautenticar usuário: ${e.message}")
                Toast.makeText(this, "Erro ao reautenticar usuário: ${e.message}", Toast.LENGTH_SHORT).show()
              }
          } else {
            // Se o usuário não tem senha associada, não precisa reautenticar
            Log.d("ExcluirConta", "Usuário não possui senha associada")
            excluirDados(user)
          }
        }
      } else {
        // Se o usuário não tem provedor de e-mail associado, não precisa reautenticar
        Log.d("ExcluirConta", "Usuário não possui provedor de e-mail associado")
        excluirDados(user)
      }
    }
  }

  private fun desativarConta(usuario: FirebaseUser) {
    usuario.updateProfile(UserProfileChangeRequest.Builder().setDisplayName("deleted_user").build())
      .addOnCompleteListener { task ->
        if (task.isSuccessful) {
          Log.d("ExcluirConta", "Conta desativada com sucesso")
        } else {
          Log.e("ExcluirConta", "Erro ao desativar conta: ${task.exception?.message}")
        }
      }
  }

  private fun excluirDados(usuario: FirebaseUser) {
    // Excluir dados do Firebase Firestore
    db.collection("Usuarios").document(usuario.uid)
      .delete()
      .addOnSuccessListener {
        Log.d("ExcluirConta", "Dados do Firestore excluídos com sucesso")

        // Recalcula os rankings após excluir os dados do Firestore
        recalcularRankings()

        usuario.delete()
          .addOnSuccessListener {
            Log.d("ExcluirConta", "Conta excluída com sucesso")
            Toast.makeText(this, "Conta excluída com sucesso", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
          }
          .addOnFailureListener { e ->
            Log.e("ExcluirConta", "Erro ao excluir conta: ${e.message}")
            Toast.makeText(this, "Erro ao excluir conta: ${e.message}", Toast.LENGTH_SHORT).show()
          }
      }
      .addOnFailureListener { e ->
        Log.e("ExcluirConta", "Erro ao excluir dados do Firestore: ${e.message}")
        Toast.makeText(this, "Erro ao excluir conta: ${e.message}", Toast.LENGTH_SHORT).show()
      }
  }
}
