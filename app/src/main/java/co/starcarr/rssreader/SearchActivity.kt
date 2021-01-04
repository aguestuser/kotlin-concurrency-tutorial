package co.starcarr.rssreader

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.starcarr.rssreader.adapter.ArticleAdapter
import co.starcarr.rssreader.search.Searcher
import co.starcarr.rssreader.types.GS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class SearchActivity : AppCompatActivity() {
    private val viewAdapter by lazy { ArticleAdapter() }
    private val viewManager by lazy  { LinearLayoutManager(this) }
    private val searcher = Searcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // list recycler view
        findViewById<RecyclerView>(R.id.articles).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        // search click handler
        findViewById<Button>(R.id.searchButton).setOnClickListener {
            viewAdapter.clear()
            GS.launch {
                search()
            }
        }
    }

    private suspend fun search() {
        val query = findViewById<EditText>(R.id.searchText).text.toString()
        val channel = searcher.search(query)

        while(!channel.isClosedForReceive) {
            // receive search hits one-by-one off the channel and add them to UI as they arrive
            val article = channel.receive()
            GS.launch(Dispatchers.Main) {
               viewAdapter.add(article)
            }
        }
    }
}