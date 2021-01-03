package co.starcarr.rssreader.producer

import co.starcarr.rssreader.GS
import co.starcarr.rssreader.model.Article
import co.starcarr.rssreader.model.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node

@ExperimentalCoroutinesApi
object ArticleProducer {
    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn", "http://rss.cnn.com/rss/cnn_topstories.rss"),
        Feed("fox", "http://feeds.foxnews.com/foxnews/politics?format=xml"),
    )
    private val dispatcher = Dispatchers.IO
    private val builderFactory = DocumentBuilderFactory.newInstance()

    val producer: ReceiveChannel<List<Article>> = GS.produce(dispatcher) {
        feeds.forEach {
            // NOTE: we fetch and send articles in groups of feeds here
            // (we will likely make this more granular later?)
            send(fetchArticles(it))
        }
    }

    private fun fetchArticles(feed: Feed): List<Article> {
        val builder = builderFactory.newDocumentBuilder()
        val xml = builder.parse(feed.url)
        val news = xml.getElementsByTagName("channel").item(0)

        return news.getElements()
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