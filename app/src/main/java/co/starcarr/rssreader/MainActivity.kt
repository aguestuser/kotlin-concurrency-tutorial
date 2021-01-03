package co.starcarr.rssreader

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.starcarr.rssreader.adapter.ArticleAdapter
import co.starcarr.rssreader.adapter.ArticleLoader
import co.starcarr.rssreader.producer.ArticleProducer
import kotlinx.coroutines.*
import javax.xml.parsers.DocumentBuilderFactory


typealias GS = GlobalScope

@ExperimentalCoroutinesApi
@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity(), ArticleLoader {

    private val viewAdapter: ArticleAdapter by lazy { ArticleAdapter(this) }
    private val viewManager: RecyclerView.LayoutManager by lazy  { LinearLayoutManager(this) }

    private val factory = DocumentBuilderFactory.newInstance()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<RecyclerView>(R.id.articles).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
        GS.launch(Dispatchers.IO) {
            loadMore()
        }
    }

    override suspend fun loadMore() {
        val producer = ArticleProducer.producer
        if(!producer.isClosedForReceive) {
            // NOTE: we fetch an entire feed -- ie: a `List<Article>`-- when a user gets to the
            // bottom of the list (we will likely make this more granular in subsequent chapters!)
            val articles = producer.receive()
            GS.launch(Dispatchers.Main) {
                findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                viewAdapter.add(articles)
            }
        }

    }
}



