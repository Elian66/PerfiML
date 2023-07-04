package com.swperfi.perfiml.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.swperfi.perfiml.R
import com.swperfi.perfiml.report.ReportActivity
import com.swperfi.perfiml.select.SelectActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnCsv = findViewById<Button>(R.id.btnCsv)
        val btnBug = findViewById<Button>(R.id.btnBug)

        btnCsv.setOnClickListener {
            startActivity(Intent(this, SelectActivity::class.java))
        }

        btnBug.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }
    }
}