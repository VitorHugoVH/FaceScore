package com.example.facescore.view.face

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.facescore.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.facescore.view.home.HomeActivity
import com.example.facescore.view.profile.ProfileActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import kotlin.math.roundToInt

class FaceActivity : AppCompatActivity() {
  private val txtPontuacao: TextView by lazy { findViewById(R.id.txtPontuacao) }
  private val REQUEST_CAMERA_PERMISSION = 123
  private lateinit var imgPerfil: ImageView
  private lateinit var imgReturn: ImageView
  private lateinit var imgBit: ImageView
  private lateinit var txtRanking: TextView

  private val captureImageLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == RESULT_OK) {
        val imageBitmap = result.data?.extras?.get("data") as Bitmap
        processarComDetectorDeRostos(imageBitmap)
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_face)

    FirebaseApp.initializeApp(this)

    val btnCapture = findViewById<Button>(R.id.btnCapture)

    btnCapture.setOnClickListener {
      capturarFoto()
    }

    imgPerfil = findViewById(R.id.img_perfil)  // Adicione esta linha
    carregarImagemPerfil()

    // Função retornar menu seta

    imgReturn = findViewById(R.id.img_return)
    imgReturn.setOnClickListener {
      val VoltarHome = Intent(this, HomeActivity::class.java)
      startActivity(VoltarHome)
    }

    // Função ir para tela perfil

    imgPerfil.setOnClickListener {
      val IrPerfil = Intent(this, ProfileActivity::class.java)
      startActivity(IrPerfil)
    }

    // Carregar a última pontuação do usuário

    carregarUltimaPontuacao()
    carregarRanking()
    txtRanking = findViewById(R.id.txtRanking)
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

  private fun carregarUltimaPontuacao() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val docRef = FirebaseFirestore.getInstance().collection("Usuarios").document(userId!!)

    docRef.get()
      .addOnSuccessListener { document ->
        if (document.exists()) {
          val ultimaPontuacao = document.getDouble("pontuacaoRosto")
          if (ultimaPontuacao != null) {
            // Se há uma pontuação, atualize o texto
            txtPontuacao.text = "Pontuação: $ultimaPontuacao"
          } else {
            // Se não há pontuação, defina o texto apropriado
            txtPontuacao.text = "Nenhuma pontuação."
          }
        } else {
          Log.d("Firestore", "O documento não existe")
        }
      }
      .addOnFailureListener { exception ->
        Log.e("Firestore", "Falha ao obter documento: $exception")
      }
  }

  private fun carregarRanking() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val docRef = FirebaseFirestore.getInstance().collection("Usuarios").document(userId!!)

    docRef.get()
      .addOnSuccessListener { document ->
        if (document.exists()) {
          val privado = document.getBoolean("privado") ?: false
          val ranking = document.getLong("ranking")
          val pontuacao = document.getDouble("pontuacaoRosto")

          if (privado) {
            // Se a conta é privada, defina o texto apropriado
            txtRanking.text = "Conta privada"
          } else if (ranking != null) {
            // Se há uma colocação (ranking), atualize o texto
            txtRanking.text = "Colocação: $ranking"
          } else if (pontuacao != null) {
            // Se não há colocação, mas há uma pontuação, exiba a pontuação
            txtRanking.text = "Pontuação: $pontuacao"
          } else {
            // Se não há nem colocação nem pontuação, defina o texto apropriado
            txtRanking.text = "Sem colocação ou pontuação"
          }
        } else {
          Log.d("Firestore", "O documento não existe")
        }
      }
      .addOnFailureListener { exception ->
        Log.e("Firestore", "Falha ao obter documento: $exception")
      }
  }

  private fun processarComDetectorDeRostos(imageBitmap: Bitmap) {
    val image = FirebaseVisionImage.fromBitmap(imageBitmap)

    val options = FirebaseVisionFaceDetectorOptions.Builder()
      .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
      .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
      .setMinFaceSize(0.1f)
      .build()

    val faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options)

    faceDetector.detectInImage(image)
      .addOnSuccessListener { faces ->
        // Desenhe as marcações na imagem
        val imagemComMarcacoes = desenharNaFace(imageBitmap, faces)

        // Exiba a imagem com as marcações na interface do usuário
        exibirImagemComMarcacoes(imagemComMarcacoes)

        for (face in faces) {
          val pontuacao = calcularPontuacaoRosto(face)
          txtPontuacao.text = pontuacao
          Log.d("PontuacaoRosto", "Pontuação: $pontuacao")

          // Converta a pontuação para BigDecimal antes de chamar a função
          val pontuacaoBigDecimal = pontuacao.substringAfter(": ").toBigDecimal()

          // Atualizar a pontuação no Firestore
          atualizarPontuacaoRosto(pontuacaoBigDecimal)

          // Verificar e recalcular o ranking
          verificarRanking()
        }
      }
      .addOnFailureListener { e ->
        Log.e("ErroDetectorRosto", "Erro na detecção de rosto: ${e.message}")
      }
  }

  private fun atualizarPontuacaoRosto(pontuacao: BigDecimal) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val usuariosRef = FirebaseFirestore.getInstance().collection("Usuarios")

    // Converter BigDecimal para Double
    val pontuacaoDouble = pontuacao.toDouble()

    // Atualizar o campo pontuacaoRosto
    usuariosRef.document(userId!!)
      .update("pontuacaoRosto", pontuacaoDouble)
      .addOnSuccessListener {
        Log.d("Firestore", "Pontuação do rosto atualizada com sucesso!")
        carregarRanking()
        carregarUltimaPontuacao()
      }
      .addOnFailureListener { exception ->
        Log.e("Firestore", "Falha ao atualizar pontuação do rosto: $exception")
      }
  }

  private fun verificarRanking() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val usuariosRef = FirebaseFirestore.getInstance().collection("Usuarios")

    usuariosRef
      .whereEqualTo("privado", false)
      .orderBy("pontuacaoRosto", Query.Direction.DESCENDING)
      .get()
      .addOnSuccessListener { result ->
        if (!result.isEmpty) {
          recalcularRankings(result)
        }
      }
      .addOnFailureListener { exception ->
        Log.e("Firestore", "Falha ao verificar ranking: $exception")
      }
  }

  private fun desenharNaFace(imageBitmap: Bitmap, faces: List<FirebaseVisionFace>): Bitmap {
    val mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)

    // Configure a paint para o contorno do rosto em azul
    val paintContorno = Paint()
    paintContorno.color = Color.BLUE
    paintContorno.style = Paint.Style.STROKE
    paintContorno.strokeWidth = 5.0f

    // Configure a paint para as landmarks em vermelho
    val paintLandmarks = Paint()
    paintLandmarks.color = Color.RED
    paintLandmarks.style = Paint.Style.STROKE
    paintLandmarks.strokeWidth = 5.0f

    for (face in faces) {
      val boundingBox = face.boundingBox

      // Desenhe o contorno do rosto em azul
      canvas.drawRect(boundingBox, paintContorno)

      // Desenhe as landmarks (marcadores) faciais em vermelho
      val landmarks = listOf(
        FirebaseVisionFaceLandmark.LEFT_EYE,
        FirebaseVisionFaceLandmark.RIGHT_EYE,
        FirebaseVisionFaceLandmark.NOSE_BASE,
        FirebaseVisionFaceLandmark.MOUTH_LEFT,
        FirebaseVisionFaceLandmark.MOUTH_RIGHT,
        FirebaseVisionFaceLandmark.LEFT_EAR,
        FirebaseVisionFaceLandmark.RIGHT_EAR,
        FirebaseVisionFaceLandmark.LEFT_CHEEK,
        FirebaseVisionFaceLandmark.RIGHT_CHEEK
        // Adicione mais landmarks conforme necessário
      )

      for (landmarkType in landmarks) {
        val landmark = face.getLandmark(landmarkType)
        desenharLandmark(canvas, paintLandmarks, landmark)
      }
    }

    return mutableBitmap
  }

  private fun desenharLandmark(canvas: Canvas, paint: Paint, landmark: FirebaseVisionFaceLandmark?) {
    if (landmark != null) {
      val point = landmark.position
      canvas.drawPoint(point.x, point.y, paint)
    }
  }

  private fun exibirImagemComMarcacoes(imagemComMarcacoes: Bitmap) {
    // Exiba a imagem com as marcações na interface do usuário (por exemplo, em um ImageView)
    imgBit = findViewById(R.id.img_bitmap)
    imgBit.setImageBitmap(imagemComMarcacoes)
    // Além disso, você pode mostrar os botões "Ok" e "Tentar Novamente" aqui
  }

  private fun calcularPontuacaoRosto(face: FirebaseVisionFace): String {
    val simetriaFacial = calcularSimetriaFacial(face)
    val formatoRosto = calcularFormatoRosto(face)

    val simetriaNormalizada = maxOf(0.0f, minOf(1.0f, simetriaFacial))
    val formatoNormalizado = maxOf(0.0f, minOf(1.0f, formatoRosto))

    val pontuacaoTotal = simetriaNormalizada + formatoNormalizado

    val pontuacaoNormalizada = pontuacaoTotal.coerceIn(0.0f, 1.0f)

    val pontuacaoFinal = pontuacaoNormalizada * 10.0f

    val pontuacaoArredondado = pontuacaoFinal.toBigDecimal().setScale(1, RoundingMode.HALF_UP)

    return "Pontuação: $pontuacaoArredondado"
  }

  private fun recalcularRankings(result: QuerySnapshot) {
    val usuariosOrdenados = result.documents.sortedWith(compareByDescending<DocumentSnapshot> {
      it.getDouble("pontuacaoRosto") ?: 0.0
    }.thenBy {
      it.getDate("dataCriacao") ?: Date(0)
    })

    var ranking = 1
    for (usuario in usuariosOrdenados) {
      val userId = usuario.id
      atualizarPosicaoRanking(userId, ranking)
      ranking++
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

  private fun calcularSimetriaFacial(face: FirebaseVisionFace): Float {
    val leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
    val rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)
    val noseBase = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)

    if (leftEye != null && rightEye != null && noseBase != null) {
      val distanciaHorizontalOlhos = rightEye.position.x - leftEye.position.x
      val distanciaVerticalNariz = noseBase.position.y - (leftEye.position.y + rightEye.position.y) / 2.0f

      val simetriaHorizontal = 1.0f / distanciaHorizontalOlhos
      val simetriaVertical = 1.0f / distanciaVerticalNariz

      // Considere normalizar e ponderar os valores conforme necessário

      return (simetriaHorizontal + simetriaVertical) / 2.0f
    }

    return 0.0f
  }

  private fun calcularFormatoRosto(face: FirebaseVisionFace): Float {
    val noseBase = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)
    val mouthLeft = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT)
    val mouthRight = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT)

    if (noseBase != null && mouthLeft != null && mouthRight != null) {
      val distanciaNariz =
        noseBase.position.x - (mouthLeft.position.x + mouthRight.position.x) / 2.0f
      return 1.0f / distanciaNariz
    }

    return 0.0f
  }

  private fun capturarFoto() {
    if (ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      iniciarCapturaFoto()
    } else {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.CAMERA),
        REQUEST_CAMERA_PERMISSION
      )
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    when (requestCode) {
      REQUEST_CAMERA_PERMISSION -> {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          iniciarCapturaFoto()
        } else {
          Toast.makeText(
            this,
            "A permissão da câmera foi negada. Você pode conceder a permissão nas configurações do aplicativo.",
            Toast.LENGTH_LONG
          ).show()
        }
      }
    }
  }

  private fun iniciarCapturaFoto() {
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (takePictureIntent.resolveActivity(packageManager) != null) {
      captureImageLauncher.launch(takePictureIntent)
    }
  }
}
