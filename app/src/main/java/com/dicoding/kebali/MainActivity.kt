package com.dicoding.kebali

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.dicoding.kebali.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onTextChanged()
        switchLan()
    }

    private fun onTextChanged(){
        binding.apply {
            inputText.doAfterTextChanged {
                outputText.text = inputText.text
            }
        }
    }

    private fun switchLan(){
        binding.apply {
            btnSwitch.setOnClickListener{
                val temp = tvEn.text
                tvEn.text = tvBali.text
                tvBali.text = temp.toString()
            }
        }
    }
}