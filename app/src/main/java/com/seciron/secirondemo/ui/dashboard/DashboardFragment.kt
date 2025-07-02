package com.seciron.secirondemo.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.seciron.secirondemo.databinding.FragmentDashboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonGetQuote.setOnClickListener {
            fetchRandomQuote()
        }

        return root
    }

    private fun fetchRandomQuote() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url("https://dummyjson.com/quotes/random")
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.string()?.let {
                            val json = JSONObject(it)
                            val quote = json.getString("quote")
                            val author = json.getString("author")
                            "\"$quote\"\n\n— $author"
                        } ?: "Empty response body"
                    } else {
                        "HTTP error: ${response.code}"
                    }
                } catch (e: Exception) {
                    Log.e("QuoteFetch", "Error fetching quote", e)
                    "Failed to fetch quote: ${e.message}"
                }
            }

            binding.textDashboard.text = result
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
