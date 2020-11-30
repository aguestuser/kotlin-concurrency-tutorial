package co.starcarr.rssreader

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node

import javax.xml.parsers.DocumentBuilderFactory


class MainActivity : AppCompatActivity() {
    private val factory = DocumentBuilderFactory.newInstance()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        asyncLoadNews()
    }

    private fun asyncLoadNews(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) = GlobalScope.launch(dispatcher) {
        val headlines = fetchRssHeadlines()
        val newsCount = findViewById<TextView>(R.id.newsCount)
        launch(Dispatchers.Main) {
            newsCount.text = "Found ${headlines.size} News"
        }
    }

    private fun fetchRssHeadlines(): List<String> {
        val builder = factory.newDocumentBuilder()
        val xml = builder.parse("https://www.npr.org/rss/rss.php?id=1001")
        val news = xml.getElementsByTagName("channel").item(0)
        return (0 until news.childNodes.length)
            .map { news.childNodes.item(it) }
            .filter { it.nodeType == Node.ELEMENT_NODE }
            .map { it as Element }
            .filter { it.tagName == "item" }
            .map {
                it.getElementsByTagName("title").item(0).textContent
            }
    }
}