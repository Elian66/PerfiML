package com.swperfi.perfiml.select

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.swperfi.perfiml.R

class SelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        val button: Button = findViewById(R.id.selecioneArquivo)

        button.setOnClickListener {
            Toast.makeText(this,"texto qualquer",Toast.LENGTH_SHORT).show()
        }
    }
}