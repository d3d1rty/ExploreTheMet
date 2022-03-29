package com.example.explorethemet

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.explorethemet.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var searchJob : Job? = null
    var items : MutableList<JSONObject> = mutableListOf<JSONObject>()
    private lateinit var adapter : MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSearch.setOnClickListener(SearchListener())

        val layoutManager = LinearLayoutManager(this)
        binding.searchResults.layoutManager = layoutManager
        binding.searchResults.setHasFixedSize(true)

        adapter = MainAdapter()
        binding.searchResults.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_item_acknowledgement) {
            val builder = AlertDialog.Builder(binding.root.context)
            builder
                .setTitle(R.string.title_acknowledgement)
                .setMessage(R.string.message_acknowledgement)
                .setPositiveButton("Ok", null)
                .show()
        }
        return super.onOptionsItemSelected(item)
    }

    inner class SearchListener : View.OnClickListener {
        override fun onClick(p0: View?) {
            items.clear()

            if (isNetworkAvailable()) {
                binding.labelLoading.text = getString(R.string.searching_text)
                val searchTerms = binding.inputSearch.text.toString()
                enqueueSearchJob(searchTerms)
            } else {
                val builder = AlertDialog.Builder(binding.root.context)
                builder
                    .setTitle(R.string.search_no_wifi_title)
                    .setMessage(R.string.search_no_wifi_message)
                    .setPositiveButton("Ok", null)
                    .show()
            }
        }

        private fun isNetworkAvailable() : Boolean {
            var available = false

            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                    ) {
                        available = true
                    }
                }
            }

            return available
        }

        private fun enqueueSearchJob(searchTerms : String) {
            searchJob = CoroutineScope(Dispatchers.IO).launch {
                val searchUrl = URL(buildUrlForSearch(searchTerms))
                val itemIds = performSearch(searchUrl)
                for (i in 0 until itemIds.length()) {
                    if (i == 19) break

                    val objectLookupUrl = URL(buildUrlForObjectLookup(itemIds[i].toString()))
                    var item = performObjectLookup(objectLookupUrl)
                    items.add(item)
                }

                withContext(Dispatchers.Main) {
                    binding.labelLoading.text = ""
                    adapter.notifyDataSetChanged()
                }
            }
        }

        private fun performSearch(searchUrl : URL) : JSONArray {
            val connection: HttpURLConnection = searchUrl.openConnection() as HttpURLConnection
            var apiResp = ""

            try {
                apiResp = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            } finally {
                connection.disconnect()
            }

            return JSONObject(apiResp).getJSONArray("objectIDs")
        }

        private fun buildUrlForSearch(terms : String) : String {
            val builder = Uri.Builder()
                .scheme("https")
                .authority("collectionapi.metmuseum.org")
                .path("/public/collection/v1/search")
                .appendQueryParameter("q", terms)

            return builder.build().toString()
        }

        private fun performObjectLookup(objectLookupUrl: URL) : JSONObject {
            val connection : HttpURLConnection = objectLookupUrl.openConnection() as HttpURLConnection

            var apiResp = ""
            try {
                apiResp = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            } finally {
                connection.disconnect()
            }

            return JSONObject(apiResp)
        }

        private fun buildUrlForObjectLookup(objectId : String) : String {
            val builder = Uri.Builder()
                .scheme("https")
                .authority("collectionapi.metmuseum.org")
                .path("/public/collection/v1/objects/${objectId}")

            return builder.build().toString()
        }
    }

    inner class MainViewHolder(val view : View) :
            RecyclerView.ViewHolder(view),
            View.OnClickListener {

        init {
            view.findViewById<View>(R.id.results_item).setOnClickListener(this)
        }
        fun setText(text: String) {
            view.findViewById<TextView>(R.id.results_item).text = text
        }

        override fun onClick(view: View?) {
            if (view != null) {
                val intent = Intent(view.context, ArtObjectActivity::class.java)
                val item = items[adapterPosition]
                Log.i("TESTING", item.toString())
                val artObject = ArtObject(
                    item.getString("objectID"),
                    item.getString("objectURL"),
                    item.getString("accessionNumber"),
                    item.getString("department"),
                    item.getString("objectName"),
                    item.getString("title"),
                    item.getString("medium"),
                    item.getString("objectDate"),
                    item.getString("artistDisplayName"),
                    item.getString("artistDisplayBio"),
                    item.getString("creditLine"),
                    item.getString("primaryImageSmall")
                )
                intent.putExtra(getString(R.string.art_object_intent_key), artObject)
                startActivity(intent)
            }
        }
    }

    inner class MainAdapter() : RecyclerView.Adapter<MainViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
            return MainViewHolder(view)
        }

        override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
            var name = items[position].getString("objectName")
            if (name == "") {
                name = "UNKNOWN"
            }

            var title= items[position].getString("title")
            if (title == "") {
                title = "UNKNOWN"
            }

            holder.setText("${name} - ${title}")
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }
}