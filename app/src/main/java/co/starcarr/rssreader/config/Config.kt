package co.starcarr.rssreader.config

import co.starcarr.rssreader.model.Feed

object Config {
    val FEEDS =  listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn", "http://rss.cnn.com/rss/cnn_topstories.rss"),
        Feed("fox", "http://feeds.foxnews.com/foxnews/politics?format=xml"),
    )
}
