package jp.techacademy.yuki.nishimura.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.list_question_detail.view.*

class QuestionDetailListAdapter(context: Context, private val mQuestion: Question) : BaseAdapter() {

    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null

    private var mCurrentIsFavorite: Boolean = mQuestion.isFavorite

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + mQuestion.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return mQuestion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        var convertView = view

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }
            val body = mQuestion.body
            val name = mQuestion.name

            val bodyTextView = convertView.bodyTextView as TextView
            bodyTextView.text = body

            val nameTextView = convertView.nameTextView as TextView
            nameTextView.text = name

            val bytes = mQuestion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }

            val favoriteImageView = convertView.favoriteImageView as ImageView
            favoriteImageView.setImageResource(if (mCurrentIsFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
            favoriteImageView.setOnClickListener {
                Snackbar.make(convertView!!, "お気に入りボタンが押されました", Snackbar.LENGTH_LONG).show()

                val newIsFavorite = !mCurrentIsFavorite
                FirebaseFirestore.getInstance()
                    .collection(ContentsPATH)
                    .document(mQuestion.questionUid)
                    .update("isFavorite", newIsFavorite)
                    .addOnSuccessListener {
                        mCurrentIsFavorite = newIsFavorite
                        notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                        Snackbar.make(convertView!!, "お気に入りの更新に失敗しました", Snackbar.LENGTH_LONG).show()
                    }
            }
        } else {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQuestion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.bodyTextView as TextView
            bodyTextView.text = body

            val nameTextView = convertView.nameTextView as TextView
            nameTextView.text = name
        }

        return convertView
    }
}