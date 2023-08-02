package com.swperfi.perfiml.results

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.swperfi.perfiml.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val responseReturn: Button = findViewById(R.id.responseReturn)
        responseReturn.setOnClickListener {
            finish()
        }

        val result = intent.getStringExtra("result")
        val behavior = intent.getStringExtra("behavior")
        val function = intent.getStringExtra("function")

        checkResultBehavior(behavior.toString(), result.toString(), function.toString())
    }

    @SuppressLint("SetTextI18n")
    private fun checkResultBehavior(behavior: String, result: String, function: String) {
        val responseText: TextView = findViewById(R.id.responseText)
        val barChart = findViewById<HorizontalBarChart>(R.id.barChart)
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        saveReport(behavior, result, function)
        when (behavior) {
            "text" -> {
                Log.d("DATA OWL - LOG", result)
                responseText.visibility = View.VISIBLE
                responseText.text = result
            }
            "bar" -> {
                barChart.visibility = View.VISIBLE
                val featureImportances = mutableListOf<Pair<String, Float>>()
                val lines = result.trimIndent().lines()
                for (line in lines) {
                    val parts = line.trim().split("\\s+".toRegex())
                    if (parts.size == 2) {
                        val feature = parts[0]
                        val importance = parts[1].toFloatOrNull()
                        if (importance != null) {
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
                val xAxis: XAxis = barChart.xAxis
                xAxis.valueFormatter = IndexAxisValueFormatter(featureNames.toTypedArray())
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f

                barChart.data = barData

                barChart.setFitBars(true)
                barChart.animateY(1000)
            }
            "pizza" -> {
                pieChart.visibility = View.VISIBLE
                // Prepare the data for the chart
                val pieEntries = mutableListOf<PieEntry>()
                val labels = mutableListOf<String>()

                val lines = result.trimIndent().lines()
                for (line in lines.drop(1)) {
                    val parts = line.trim().split("\\s+".toRegex())
                    if (parts.size == 2) {
                        val deviceId = parts[0]
                        val percentage = parts[1].replace("%", "").toFloatOrNull()
                        if (percentage != null) {
                            pieEntries.add(PieEntry(percentage, deviceId))
                            labels.add(deviceId)
                        }
                    }
                }

                val pieDataSet = PieDataSet(pieEntries, "Device ID Collected")
                pieDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

                val pieData = PieData(pieDataSet)
                pieChart.data = pieData

                // Styling the chart
                pieChart.description.isEnabled = false
                pieChart.isRotationEnabled = true
                pieChart.setHoleColor(Color.TRANSPARENT)
                pieChart.setTransparentCircleAlpha(0)
                pieChart.setEntryLabelTextSize(12f)
                pieChart.setEntryLabelColor(Color.BLACK)

                // Refresh the chart
                pieChart.invalidate()
            }
            else -> {
                responseText.visibility = View.VISIBLE
                responseText.text = "Não foi possível apresentar o resultado da análise feita"
            }
        }
    }

    private fun saveReport(behavior: String, result: String, function: String) {
        File(Environment.getExternalStorageDirectory().path + "/Documents/SWPerfl/DataOwl/Logs/").mkdirs()
        val file = File(Environment.getExternalStorageDirectory().path + "/Documents/SWPerfl/DataOwl/Logs/",
            "log_$function.txt"
        )

        try {
            FileOutputStream(file).use { fos ->
                fos.write(result.toByteArray())
                Toast.makeText(this, "A log file was saved in \"Documents/SWPerfi/SWPerfl/DataOwl/Logs\"", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar o arquivo", Toast.LENGTH_SHORT).show()
        }
    }
}