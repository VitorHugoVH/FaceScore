import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class CepMaskWatcher(private val editText: EditText) : TextWatcher {

  private var isFormatting: Boolean = false

  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

  override fun afterTextChanged(editable: Editable?) {
    if (isFormatting) {
      return
    }

    isFormatting = true

    // Remove qualquer traço ou espaço em branco do texto
    val cleanText = editable.toString().replace("[^\\d]".toRegex(), "")

    if (cleanText.length >= 5) {
      val formattedCep = "${cleanText.substring(0, 5)}-${cleanText.substring(5)}"
      editText.setText(formattedCep)
      editText.setSelection(formattedCep.length)
    } else {
      editText.setText(cleanText)
      editText.setSelection(cleanText.length)
    }

    isFormatting = false
  }
}
