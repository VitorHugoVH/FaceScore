package com.example.facescore.view.classificacao

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.facescore.R
import com.example.facescore.model.UsuarioModel

class Adapter : RecyclerView.Adapter<Adapter.MyViewHolder>() {

  private var userList = mutableListOf<UsuarioModel>()

  fun atualizarLista(novaLista: List<UsuarioModel>) {
    userList.clear()
    userList.addAll(novaLista)
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
    return MyViewHolder(view)
  }

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    val usuario = userList[position]

    // Configure aqui as Views do layout item_usuarios.xml com os dados do usuário
    holder.bind(usuario)
  }

  override fun getItemCount(): Int {
    return userList.size
  }

  class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imgPerfil: ImageView = itemView.findViewById(R.id.photo_person)
    private val posicaoNumber: TextView = itemView.findViewById(R.id.posicao_number)
    private val nomeUsuario: TextView = itemView.findViewById(R.id.nome_number4)
    private val pontuacaoRosto: TextView = itemView.findViewById(R.id.pontuacao_placar)

    fun bind(usuario: UsuarioModel) {
      // Configurar aqui as Views do layout item_usuarios.xml com os dados do usuário
      posicaoNumber.text = usuario.posicao.toString()
      nomeUsuario.text = usuario.nome
      pontuacaoRosto.text = usuario.pontuacao.toString()

      if (!usuario.nome.isNullOrBlank()) {
        val palavras = usuario.nome.split(" ")
        val duasPrimeirasPalavras = palavras.take(2).joinToString(" ")
        nomeUsuario.text = duasPrimeirasPalavras
      }

      Glide.with(itemView.context)
        .load(usuario.imagemUrl)
        .override(100, 100)
        .transform(CenterCrop(), RoundedCorners(50))
        .into(imgPerfil)
    }
  }
}
