package com.swperfi.perfiml.select

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.swperfi.perfiml.R
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class SelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        val button: Button = findViewById(R.id.selecioneArquivo)

        button.setOnClickListener {
            escolherArquivo()
        }
    }

    private fun escolherArquivo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"

        }
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){

            val selectedFile = data?.data
            if(selectedFile != null){
                val contentResolver = contentResolver
                val inputStream = contentResolver.openInputStream(selectedFile)
                if(isCsvValid(inputStream, "battery_power", "foreground_app")){
                    val documentFile = DocumentFile.fromSingleUri(this, selectedFile)
                    val filePath = documentFile?.uri?.path
                    if (filePath != null) {
                        val csvFile = File(Environment.getExternalStorageDirectory().path + "/Documents/SWPerfl/Tucandeira/Logs/Dynamic/", getFileNameFromUri(selectedFile).toString())
                        verificarCsv(csvFile.absolutePath)
                    }
                }
                else{
                    Toast.makeText(this, "Arquivo Inválido!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun verificarCsv(file: String) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val python = Python.getInstance()
        val result = python.getModule("main").callAttr("mediaCpu",file).toString()
        Toast.makeText(this, "Resultado: $result", Toast.LENGTH_SHORT).show()
    }

    private fun isCsvValid(inputStream: InputStream?, vararg columns : String): Boolean {
        inputStream?.let {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val header = reader.readLine()
            if(header != null){
                val headers = header.split(",")
                if(headers.containsAll(columns.toList())){
                    return true
                }
            }
        }
        return false
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                val displayName = it.getString(displayNameIndex)
                it.close()
                return displayName
            }
        }
        return null
    }
}