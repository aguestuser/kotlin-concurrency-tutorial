package co.starcarr.rssreader

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.starcarr.rssreader.adapter.ArticleAdapter
import co.starcarr.rssreader.model.Article
import co.starcarr.rssreader.model.Feed
import kotlinx.coroutines.*
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers.IO
import org.w3c.dom.Element
import org.w3c.dom.Node

import javax.xml.parsers.DocumentBuilderFactory


typealias GS = GlobalScope

@ExperimentalCoroutinesApi
@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private val viewAdapter: ArticleAdapter by lazy { ArticleAdapter() }
    private val viewManager: RecyclerView.LayoutManager by lazy  { LinearLayoutManager(this) }

    private val factory = DocumentBuilderFactory.newInstance()
    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn", "http://rss.cnn.com/rss/cnn_topstories.rss"),
        Feed("fox", "http://feeds.foxnews.com/foxnews/politics?format=xml"),
        Feed("inv", "htt:myNewsFeed"),
    )

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<RecyclerView>(R.id.articles).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        loadNewsAsync()
    }


    private fun loadNewsAsync(dispatcher: CoroutineDispatcher = IO) = GS.launch(dispatcher) {
        // fetch data
        val requests = feeds
            .mapTo(mutableListOf()) { fetchArticlesAsync(it, dispatcher) }
            .onEach { it.join() }

        // process data
        val articles = requests
            .filter { !it.isCancelled }
            .flatMap { it.getCompleted() }

        // display data
        launch(Dispatchers.Main) {
            findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
            viewAdapter.add(articles)
        }
    }



    private fun fetchArticlesAsync(
        feed: Feed,
        dispatcher: CoroutineDispatcher
    ): Deferred<List<Article>> = GS.async(dispatcher) {

        delay(1000)

        val builder = factory.newDocumentBuilder()
        val xml = builder.parse(feed.url)
        val news = xml.getElementsByTagName("channel").item(0)

        news.getElements()
            .map {
                Article(
                    feed = feed.name,
                    title = it.getTextContentByTag("title"),
                    summary = sanitize(it.getTextContentByTag("description")),
                )
            }
    }

    private fun Node.getElements(): List<Element> =
        (0 until this.childNodes.length)
            .map { this.childNodes.item(it) }
            .filter { it.nodeType == Node.ELEMENT_NODE }
            .map { it as Element }
            .filter { it.tagName == "item" }

    private fun Element.getTextContentByTag(tagName: String): String =
        this.getElementsByTagName(tagName).item(0).textContent

    private fun sanitize(summary: String) =
        if (!summary.startsWith("div") && summary.contains("<div"))
            summary.substring(0, summary.indexOf(("<div")))
        else summary
}



