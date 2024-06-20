package com.dicoding.kebali

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONException
import org.json.JSONObject

class UsageFragment : Fragment() {
    companion object {
        const val textNoKey = "Masukkan API Key untuk melihat usage"
    }

    private var deepLKey = "cb654b32-c4c6-4f37-bfa7-6225263d6217:fx"
    private lateinit var deepLKeyText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_usage, container, false)

        confText(view)
        loadPreferences()

        return view
    }

    private fun loadPreferences() {
        deepLKey = deepLKey

        deepLKeyText.text = deepLKey

        if (deepLKey != "") {
            getDeepLUsage()
        } else {
            showNoKey()
        }
    }

    private fun confText(view: View) {
        deepLKeyText = view.findViewById(R.id.DeepLKeyText)
    }

    private fun showNoKey() {
        view?.findViewById<TextView>(R.id.DeepLUsageText)?.text = textNoKey
    }

    private fun getDeepLUsage() {
        val that = this

        AndroidNetworking.get("https://api-free.deepl.com/v2/usage")
            .addHeaders("Authorization", "DeepL-Auth-Key $deepLKey")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val characterCount = response.getInt("character_count")
                        val characterLimit = response.getInt("character_limit")

                        updateUsage(characterCount, characterLimit)
                    } catch (e: JSONException) {
                        showNoKey()
                        Toast.makeText(that.requireContext(), "Terjadi kesalahan koneksi JSON\n${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    showNoKey()
                    Toast.makeText(that.requireContext(), "Terjadi kesalahan koneksi\n${anError?.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun updateUsage(charCount: Int, charLimit: Int) {
        val txt = "$charCount dari $charLimit karakter terpakai\n(${charLimit - charCount} tersisa)"
        view?.findViewById<TextView>(R.id.DeepLUsageText)?.text = txt
    }
}
