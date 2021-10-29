package jp.techacademy.yuki.nishimura.qa_app

import java.io.Serializable

class Question(
    val title: String,
    val body: String,
    val name: String,
    val uid: String,
    val questionUid: String,
    val genre: Int,
    val bytes: ByteArray,
    val isFavorite: Boolean,
    val answers: ArrayList<Answer>
) : Serializable {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}
