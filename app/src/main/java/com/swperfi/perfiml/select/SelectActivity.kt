@file:Suppress("DEPRECATION")

package com.swperfi.perfiml.select

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.github.mikephil.charting.BuildConfig
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.swperfi.perfiml.R
import java.io.*

class SelectActivity : AppCompatActivity() {

    private var chart: HorizontalBarChart? = null
    private var chartView: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.swperfi.perfiml.R.layout.activity_select)

        setupPermissions()

        val reloadButton: Button = findViewById<Button>(com.swperfi.perfiml.R.id.reloadButton)
        val button: Button = findViewById(com.swperfi.perfiml.R.id.selecioneArquivo)
        chart = findViewById<HorizontalBarChart>(com.swperfi.perfiml.R.id.chart)

        reloadButton.setOnClickListener {
            chartView!!.visibility = View.GONE
        }

        button.setOnClickListener {
            escolherArquivo()
        }
    }

    private fun setupChart(fileName: String) {
        val file = File(Environment.getExternalStorageDirectory().path + "/Documents/SWPerfl/TucanPython/", fileName)

        // Lista para armazenar as features e importâncias
        val featureImportances = mutableListOf<Pair<String, Float>>()

        file.forEachLine { line ->
            // Remove espaços em branco extras e divide a linha em duas partes (feature e importância)
            val parts = line.trim().split("\\s+".toRegex())
            if (parts.size == 2) {
                val feature = parts[0]
                val importance = parts[1].toFloatOrNull()
                if (importance != null) {
                    // Adiciona a feature e importância à lista
                    featureImportances.add(Pair(feature, importance))
                }
            }
        }

        featureImportances.sortByDescending { it.second }

        // Seleciona apenas os 10 primeiros elementos
        val top10FeatureImportances = featureImportances.take(10)

        val barEntries = top10FeatureImportances.mapIndexed { index, pair ->
            BarEntry(index.toFloat(), pair.second)
        }

        // Extrai os nomes das features
        val featureNames = top10FeatureImportances.map { it.first }

        val barDataSet = BarDataSet(barEntries, "Feature Importance")
        barDataSet.color = Color.BLUE

        val barData = BarData(barDataSet)
        barData.barWidth = 0.05f

        // Configura o eixo X para exibir o nome das features
        val xAxis: XAxis = chart!!.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(featureNames.toTypedArray())
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        chart!!.data = barData

        chart!!.setFitBars(true)
        chart!!.animateY(1000)

        chartView = findViewById(R.id.layoutChart)
        chartView!!.visibility = View.VISIBLE

    }

    private fun setupPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1001
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
            }
        }
    }

    private class CustomValueFormatter(private val labels: List<String>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index >= 0 && index < labels.size) {
                labels[index]
            } else {
                ""
            }
        }
    }

    private fun escolherArquivo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"

        }
        startActivityForResult(intent, 1)
    }

    @Deprecated("Deprecated in Java")
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
                        verifyCsv(csvFile.absolutePath)
                    }
                }
                else{
                    Toast.makeText(this, "Arquivo Inválido!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun verifyCsv(file: String) {
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        val python = Python.getInstance()
        val result = python.getModule("main").callAttr("getInfluences", 0.2, file).toString()
        saveTextToFile(result, "result.txt")
    }

    private fun saveTextToFile(text: String, fileName: String) {
        File(Environment.getExternalStorageDirectory().path + "/Documents/SWPerfl/TucanPython/").mkdirs()
        val file = File(Environment.getExternalStorageDirectory().path + "/Documents/SWPerfl/TucanPython/", fileName)

        try {
            FileOutputStream(file).use { fos ->
                fos.write(text.toByteArray())
                Toast.makeText(this, "Texto salvo em $fileName", Toast.LENGTH_SHORT).show()

                setupChart(fileName)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar o arquivo", Toast.LENGTH_SHORT).show()
        }
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