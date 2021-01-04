package co.starcarr.rssreader.search

import co.starcarr.rssreader.config.Config
import co.starcarr.rssreader.types.GS
import co.starcarr.rssreader.logic.Dom
import co.starcarr.rssreader.logic.Dom.getLeafElements
import co.starcarr.rssreader.logic.Dom.getTextContentByTag
import co.starcarr.rssreader.logic.Dom.sanitize
import co.starcarr.rssreader.model.Article
import co.starcarr.rssreader.model.Feed
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.xml.parsers.DocumentBuilderFactory

class Searcher {
    private val feeds = Config.FEEDS
    private val dispatcher = Executors.newFixedThreadPool(feeds.size).asCoroutineDispatcher()
    private val factory = DocumentBuilderFactory.newInstance()


    fun search(query: String): ReceiveChannel<Article>  =
        Channel<Article>(150).also { channel ->
            feeds.forEach { feed ->
                GS.launch(dispatcher) {
                    searchOne(feed, channel, query)
                }
            }

        }



    private suspend fun searchOne(feed: Feed, channel: SendChannel<Article>, query: String) =
        Dom.parseElementTree(factory, feed.url)
            .getLeafElements()
            .forEach {
                val title = it.getTextContentByTag("title")
                val summary = it.getTextContentByTag("description").sanitize()
                if(title.contains(query) || summary.contains(query)) {
                    channel.send(Article(feed.name, title, summary))
                }
            }
}