package com.dicoding.kebali

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class TranslateFragment : Fragment() {

    companion object {
        var languages = hashMapOf<String, String>()
        var languagesArraySource = arrayOf<String>()
        var languagesArrayTarget = arrayOf<String>()
        var isLanguagesLoaded = false
    }

    private var deepLKey = "cb654b32-c4c6-4f37-bfa7-6225263d6217:fx"
    private var sourceLanguage = ""
    private var targetLanguage = "null"

    private lateinit var spinnerSource: Spinner
    private lateinit var spinnerTarget: Spinner
    private lateinit var sourceTextUI: EditText
    private lateinit var targetTextUI: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_translate, container, false)

        AndroidNetworking.initialize(requireContext())

        confTextZone(view)
        loadPreferences()

        if (!isLanguagesLoaded) {
            loadLanguages {
                confSpinners(view)
                isLanguagesLoaded = true
            }
        } else {
            confSpinners(view)
        }

        val translateButton: Button = view.findViewById(R.id.button)
        translateButton.setOnClickListener {
            onClickTranslate()
        }

        return view
    }

    private fun confTextZone(view: View) {
        sourceTextUI = view.findViewById(R.id.sourceText)
        targetTextUI = view.findViewById(R.id.translationTextView)

        sourceTextUI.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                targetTextUI.text = ""
                view.findViewById<TextView>(R.id.detectedLanguageText).text = ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // ...
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // ...
            }
        })

        sourceTextUI.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val inputMethodManager =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(sourceTextUI.windowToken, 0)
            }
        }
    }

    private fun loadPreferences() {
        deepLKey = deepLKey
    }

    private fun loadLanguages(callback: () -> Unit) {
        if (testKeyNull()) {
            callback.invoke()
            return
        }

        val that = this

        AndroidNetworking.get("https://api-free.deepl.com/v2/languages")
            .addHeaders("Authorization", "DeepL-Auth-Key $deepLKey")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    try {
                        for (i in 0 until response.length()) {
                            val obj = response.getJSONObject(i)
                            languages[obj.getString("name")] = obj.getString("language")
                        }

                        var languageArray = languages.keys.toList()
                        languageArray = languageArray.sorted()

                        languagesArraySource = arrayOf("Detect language") + languageArray
                        languagesArrayTarget = languageArray.toTypedArray()

                        callback.invoke()
                    } catch (e: JSONException) {
                        Toast.makeText(
                            that.requireContext(),
                            "An error occurred while retrieving languages\n${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    Toast.makeText(
                        that.requireContext(),
                        "A connection error occurred while retrieving languages\n${anError.toString()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun confSpinners(view: View) {
        spinnerSource = view.findViewById(R.id.sourceLanguageSpinner)
        spinnerTarget = view.findViewById(R.id.targetLanguageSpinner)

        if (spinnerSource.adapter != null && spinnerSource.adapter.count > 0) {
            return
        }

        val adapterSource =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, languagesArraySource)
        spinnerSource.adapter = adapterSource

        val adapterTarget =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, languagesArrayTarget)
        spinnerTarget.adapter = adapterTarget

        spinnerSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val lastSource = parent.getItemAtPosition(position).toString()
                sourceLanguage = languages[lastSource].toString()
                if (sourceLanguage == "null") sourceLanguage = ""
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        spinnerTarget.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val lastTarget = parent.getItemAtPosition(position).toString()
                targetLanguage = languages[lastTarget].toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun testKeyNull(): Boolean {
        if (deepLKey == "") {
            Toast.makeText(requireContext(), "Masukkan DeepL Key", Toast.LENGTH_SHORT).show()
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val usageFragment = UsageFragment()
            fragmentTransaction.replace(android.R.id.content, usageFragment)
            fragmentTransaction.commit()
            return true
        }
        return false
    }

    private fun onClickTranslate() {
        sourceTextUI.clearFocus()

        val sourceText = sourceTextUI.text.toString()

        if (testKeyNull() || sourceText == "") {
            return
        }

        val that = this

        AndroidNetworking.get("https://api-free.deepl.com/v2/translate")
            .addHeaders("Authorization", "DeepL-Auth-Key $deepLKey")
            .addQueryParameter("text", sourceText)
            .addQueryParameter("source_lang", sourceLanguage)
            .addQueryParameter("target_lang", targetLanguage)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val trad = response.getJSONArray("translations").getJSONObject(0)
                        val targetText = trad.getString("text")
                        val detectLanguage = trad.getString("detected_source_language")

                        displayTrad(
                            null,
                            targetText,
                            sourceLanguage,
                            detectLanguage,
                            null
                        )
                    } catch (e: JSONException) {
                        Toast.makeText(that.requireContext(), "A JSON error occurred\n${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    Toast.makeText(
                        that.requireContext(),
                        "A connection error occurred\n${anError.toString()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun displayTrad(
        sourceText: String?,
        targetText: String?,
        sourceLanguage: String?,
        detectLanguage: String?,
        targetLanguage: String?
    ) {
        val detectedLanguageUI = requireView().findViewById<TextView>(R.id.detectedLanguageText)

        if (sourceText != null) {
            sourceTextUI.setText(sourceText)
        }

        if (targetText != null) {
            targetTextUI.text = targetText
        }

        if (detectLanguage != null && sourceLanguage != null && detectLanguage != sourceLanguage) {
            val text = "Detected Language: ${findMatchingLang(detectLanguage)}"
            detectedLanguageUI.text = text
        } else {
            detectedLanguageUI.text = ""
        }

        setSpinners(findMatchingLang(sourceLanguage), findMatchingLang(targetLanguage))
    }

    private fun setSpinners(langSource: String?, langTarget: String?) {
        if (langSource != null) {
            spinnerSource.setSelection(languagesArraySource.indexOf(langSource))
        }
        if (langTarget != null) {
            spinnerTarget.setSelection(languagesArrayTarget.indexOf(langTarget))
        }
    }

    private fun findMatchingLang(code: String?): String? {
        if (code == null || languages.size == 0) {
            return null
        }

        if (code == "") {
            return "Detect language"
        }

        return languages.filter { it.value == code }.keys.first()
    }
}
