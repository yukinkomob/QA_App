package jp.techacademy.yuki.nishimura.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mGenre = 0

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    private var snapShotListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            if (mGenre == 0) {
                Snackbar.make(
                    view,
                    getString(R.string.question_no_select_genre),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {

            }
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
        }

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.app_name,
            R.string.app_name
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        mDatabaseReference = FirebaseDatabase.getInstance().reference

        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (mGenre == 0) {
            onNavigationItemSelected(nav_view.menu.getItem(0))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.nav_hobby) {
            toolbar.title = getString(R.string.menu_hobby_label)
            mGenre = 1
        } else if (id == R.id.nav_life) {
            toolbar.title = getString(R.string.menu_life_label)
            mGenre = 2
        } else if (id == R.id.nav_health) {
            toolbar.title = getString(R.string.menu_health_label)
            mGenre = 3
        } else if (id == R.id.nav_computer) {
            toolbar.title = getString(R.string.menu_computer_label)
            mGenre = 4
        }
        drawer_layout.closeDrawer(GravityCompat.START)

        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        listView.adapter = mAdapter

        snapShotListener?.remove()

        snapShotListener = FirebaseFirestore.getInstance()
            .collection(ContentsPATH)
            .whereEqualTo("genre", mGenre)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    return@addSnapshotListener
                }
                var questions = listOf<Question>()
                val results = querySnapshot?.toObjects(FireStoreQuestion::class.java)
                results?.also {
                    questions = it.map { firestoreQuestion ->
                        val bytes =
                            if (firestoreQuestion.image.isNotEmpty()) {
                                Base64.decode(firestoreQuestion.image, Base64.DEFAULT)
                            } else {
                                byteArrayOf()
                            }
                        Question(firestoreQuestion.title, firestoreQuestion.body, firestoreQuestion.name, firestoreQuestion.uid, firestoreQuestion.id, firestoreQuestion.genre, bytes, firestoreQuestion.isFavorite, firestoreQuestion.answers)
                    }
                }
                mQuestionArrayList.clear()
                mQuestionArrayList.addAll(questions)
                mAdapter.notifyDataSetChanged()
            }
        return true
    }
}
