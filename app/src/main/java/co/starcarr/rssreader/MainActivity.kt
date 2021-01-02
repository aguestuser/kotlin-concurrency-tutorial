package co.starcarr.rssreader

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
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

    private val factory = DocumentBuilderFactory.newInstance()
    private val feeds = listOf(
        "https://www.npr.org/rss/rss.php?id=1001",
        "http://rss.cnn.com/rss/cnn_topstories.rss",
        "http://feeds.foxnews.com/foxnews/politics?format=xml",
        "htt:myNewsFeed",
    )

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        asyncLoadNews()
    }


    private fun asyncLoadNews(dispatcher: CoroutineDispatcher = IO) = GS.launch(dispatcher) {
        // fetch data
        val requests = feeds
            .mapTo(mutableListOf<Deferred<List<String>>>()) { fetchHeadlinesAsync(it, dispatcher) }
            .onEach { it.join() }

        // process data
        val headlines = requests
            .filter { !it.isCancelled }
            .flatMap { it.getCompleted() }

        val (numFailed, numFetched) = requests
            .filter { it.isCancelled }
            .size
            .let { (it to requests.size - it) }

        // display data
        val newsCount = findViewById<TextView>(R.id.newsCount)
        val warnings = findViewById<TextView>(R.id.warnings)
        launch(Dispatchers.Main) {
            newsCount.text = "Found ${headlines.size} News in $numFetched feed(s)"
            if (numFailed > 0) {
                warnings.text = "Failed to fetch $numFailed feed(s)"
            }
        }
    }

    private fun fetchHeadlinesAsync(feed: String, dispatcher: CoroutineDispatcher) = GS.async(dispatcher) {
        val builder = factory.newDocumentBuilder()
        val xml = builder.parse(feed)
        val news = xml.getElementsByTagName("channel").item(0)
        (0 until news.childNodes.length)
            .map { news.childNodes.item(it) }
            .filter { it.nodeType == Node.ELEMENT_NODE }
            .map { it as Element }
            .filter { it.tagName == "item" }
            .map {
                it.getElementsByTagName("title").item(0).textContent
            }
    }
}



