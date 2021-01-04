package co.starcarr.rssreader.producer

import co.starcarr.rssreader.types.GS
import co.starcarr.rssreader.config.Config
import co.starcarr.rssreader.logic.Dom
import co.starcarr.rssreader.logic.Dom.getLeafElements
import co.starcarr.rssreader.logic.Dom.getTextContentByTag
import co.starcarr.rssreader.logic.Dom.sanitize
import co.starcarr.rssreader.model.Article
import co.starcarr.rssreader.model.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import javax.xml.parsers.DocumentBuilderFactory

@ExperimentalCoroutinesApi
object ArticleProducer {

    private val dispatcher = Dispatchers.IO
    private val builderFactory = DocumentBuilderFactory.newInstance()

    val producer: ReceiveChannel<List<Article>> = GS.produce(dispatcher) {
        Config.FEEDS.forEach {
            // NOTE: we fetch and send articles in groups of feeds here
            // (we will likely make this more granular later?)
            send(fetchArticles(it))
        }
    }

    private fun fetchArticles(feed: Feed): List<Article> =
        Dom.parseElementTree(builderFactory,feed.url)
            .getLeafElements()
            .map {
                Article(
                    feed = feed.name,
                    title = it.getTextContentByTag("title"),
                    summary = it.getTextContentByTag("description").sanitize(),
                )
            }
}