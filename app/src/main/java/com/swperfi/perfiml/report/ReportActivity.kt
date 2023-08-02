package com.swperfi.perfiml.report

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.opencsv.CSVWriter
import com.swperfi.perfiml.R
import com.swperfi.perfiml.adapter.KernelAdapter
import java.io.*

class ReportActivity : AppCompatActivity() {

    private var chartView: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        val button: Button = findViewById(R.id.selecioneArquivo)

        File(Environment.getExternalStorageDirectory().path + "/Documents/SWPerfl/DataOwl/Report/").mkdirs()

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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){

            val selectedFile = data?.data
            if(selectedFile != null){
                val contentResolver = contentResolver
                val inputStream = contentResolver.openInputStream(selectedFile)

                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?

                try {
                    line = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line)
                        stringBuilder.append('\n')
                        line = reader.readLine()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    reader.close()
                }

                val fileContent = stringBuilder.toString()
                verifyBugReport(fileContent)
            }
        }
    }

    private fun verifyBugReport(textoArq: String) {
        val arqBlocks = textoArq.trimIndent().split("\n{2,}^ *".toRegex(RegexOption.MULTILINE))

        val kernelWakeLocks = arqBlocks[9]
        val partialWakeLocks = arqBlocks[10]
        val wakeUpReason = arqBlocks[11]

        setKernelWakeLock(kernelWakeLocks)
        setPartialWakeLock(partialWakeLocks)
        setWakeUpReason(wakeUpReason)
    }

    private fun setWakeUpReason(wakeUpReason: String) {
        val wakeupReasonListPart1 = "Wakeup reason [a-zA-Z_:\\-0-9,\\[\\]\\. ]+ realtime".toRegex()
        val wakeupReasonStrBloco1 = wakeupReasonListPart1.findAll(wakeUpReason)
            .map { it.value }
            .joinToString("\n")


        val nameWakeupReasonListPart1 = "Wakeup reason (.+): .+ \\(".toRegex()
            .findAll(wakeupReasonStrBloco1)
            .map { it.groupValues[1] }
            .toList()

        val typeWakeupReasonListPart1 = MutableList(nameWakeupReasonListPart1.size) { "Wakeup reason" }
        val indexWakeupReasonListPart1 = IntRange(0, nameWakeupReasonListPart1.size - 1).toList()

        val timeWakeupReasonListPart1 = "Wakeup reason .+: (.+) \\(".toRegex()
            .findAll(wakeupReasonStrBloco1)
            .map { it.groupValues[1] }
            .toList()

        val vezesWakeupReasonListPart1 = "Wakeup reason .+: .+ \\(([0-9]+) times\\)".toRegex()
            .findAll(wakeupReasonStrBloco1)
            .map { it.groupValues[1] }
            .toList()

        val dadosWakeupReasonListPart1 = mutableListOf<List<String>>()
        dadosWakeupReasonListPart1.add(listOf(
            "Tipo do Wake Lock",
            "Nome do Wake Lock",
            "Tempo total que os Wake Locks mantiveram o dispositivo acordado",
            "Quantas vezes os Wake Locks se mantiveram ativos"
        ))

        for (i in nameWakeupReasonListPart1.indices) {
            val type = typeWakeupReasonListPart1[i]
            val name = nameWakeupReasonListPart1[i]
            val tempo = timeWakeupReasonListPart1[i]
            val vezes = vezesWakeupReasonListPart1[i]

            val dados = listOf(type, name, tempo, vezes)
            dadosWakeupReasonListPart1.add(dados)
        }


        val wakeupReasonListPart2 = "Wakeup reason [a-zA-Z_:\\-0-9,\\[\\]\\. ]+ realtime".toRegex()
        val wakeupReasonStrBloco2 = wakeupReasonListPart2.findAll(wakeUpReason)
            .map { it.value }
            .joinToString("\n")

        val nameWakeupReasonListPart2 = "Wakeup reason (.+) realtime".toRegex()
            .findAll(wakeupReasonStrBloco2)
            .map { it.groupValues[1] }
            .toList()

        val typeWakeupReasonListPart2 = MutableList(nameWakeupReasonListPart2.size) { "Wakeup reason" }
        val timeWakeupReasonListPart2 = MutableList(nameWakeupReasonListPart2.size) { "-1" }
        val vezesWakeupReasonListPart2 = MutableList(nameWakeupReasonListPart2.size) { "-1" }
        val indexWakeupReasonListPart2 = IntRange(0, nameWakeupReasonListPart2.size - 1).toList()

        for (i in nameWakeupReasonListPart2.indices) {
            val type = typeWakeupReasonListPart2[i]
            val name = nameWakeupReasonListPart2[i]
            val tempo = timeWakeupReasonListPart2[i]
            val vezes = vezesWakeupReasonListPart2[i]

            val dados = listOf(type, name, tempo, vezes)
            dadosWakeupReasonListPart1.add(dados)
        }

        val csvFile = File(Environment.getExternalStorageDirectory().path + "/Documents/SWPerfl/DataOwl/Report/", "reason_wake_locks.csv")
        val fileWriter = FileWriter(csvFile)
        val csvWriter = CSVWriter(fileWriter)

        for (dados in dadosWakeupReasonListPart1) {
            csvWriter.writeNext(dados.toTypedArray())
        }
        csvWriter.close()
        fileWriter.close()

    }

    private fun setPartialWakeLock(partialWakeLocks: String) {
        val realTime = Regex("realtime").findAll(partialWakeLocks).map { it.value }.toList()

        val uidWake_IndexDf = Regex("Wake lock (.+?) ").findAll(partialWakeLocks).map { it.groupValues[1] }.toList()

        val typeWakeList = MutableList(uidWake_IndexDf.size) { "Partial wake locks" }

        val nameWakeList = Regex("lock .+? (.+):").findAll(partialWakeLocks).map { it.groupValues[1] }.toList()

        val tempoWakeList = Regex("Wake lock .+: (.+) \\([0-9]+ times\\)").findAll(partialWakeLocks).map { it.groupValues[1] }.toList()

        val vezesWakeList = Regex("([0-9]+) times").findAll(partialWakeLocks).map { it.groupValues[1] }.toList()

        val maxWakeList = Regex("max=([0-9]+)").findAll(partialWakeLocks).map { it.groupValues[1] }.toList()

        var actualWakeList = Regex("max=[0-9]+(.+)realtime").findAll(partialWakeLocks).map { it.groupValues[1] }.toList()

        actualWakeList = actualWakeList.map {
            if (it == " ") {
                "=None"
            } else {
                it.trim()
            }
        }

        var actualWake = ""
        for (item in actualWakeList) {
            actualWake += item
        }

        val actualWakeListUpdated = Regex("=([0-9]*[b-zA-Z]*)").findAll(actualWake).map { it.groupValues[1] }.toList()

        val dadosWakeList0 = mutableListOf<List<String>>()
        dadosWakeList0.add(listOf("Type of Wake Lock",
            "Name of the Wake Lock",
            "Total duration for which the Wake Locks kept the device awake",
            "Number of times the Wake Locks stayed active.",
            "Max",
            "Actual"
        ))

        for (i in nameWakeList.indices) {
            val type = typeWakeList[i]
            val name = nameWakeList[i]
            val tempo = tempoWakeList[i]
            val vezes = vezesWakeList[i]
            val max = maxWakeList[i]
            val actual = actualWakeListUpdated[i]

            val dados = listOf(type, name, tempo, vezes, max, actual)
            dadosWakeList0.add(dados)
        }

        val csvFile = File(Environment.getExternalStorageDirectory().path + "/Documents/SWPerfl/DataOwl/Report/", "partial_wake_locks.csv")
        val fileWriter = FileWriter(csvFile)
        val csvWriter = CSVWriter(fileWriter)

        for (dados in dadosWakeList0) {
            csvWriter.writeNext(dados.toTypedArray())
        }
        csvWriter.close()
        fileWriter.close()

    }

    private fun setKernelWakeLock(kernelWakeLocks: String) {
        val nameWakeList0 = mutableListOf<String>()

        val pattern = "lock( .*):".toRegex()
        val nameWakeMatches = pattern.findAll(kernelWakeLocks)
        for (match in nameWakeMatches) {
            var name = match.groupValues[1]
            if (name == " ") {
                name = "None"
            }
            nameWakeList0.add(name.trim())
        }

        val typeWakeList0 = MutableList(nameWakeList0.size) { "Kernel wake locks" }
        val uidWakeIndexDf0 = IntRange(0, nameWakeList0.size - 1).toList()
        val maxWakeList0 = MutableList(nameWakeList0.size) { -1 }
        val actualWakeList0 = MutableList(nameWakeList0.size) { -1 }

        val tempoWakeList0 = mutableListOf<String>()
        val tempoPattern = ": (.+) \\(".toRegex()
        val tempoMatches = tempoPattern.findAll(kernelWakeLocks)
        for (match in tempoMatches) {
            tempoWakeList0.add(match.groupValues[1])
        }

        val vezesWakeList0 = mutableListOf<String>()
        val vezesPattern = "\\((.+) .+\\)".toRegex()
        val vezesMatches = vezesPattern.findAll(kernelWakeLocks)
        for (match in vezesMatches) {
            vezesWakeList0.add(match.groupValues[1])
        }

        val dadosWakeList0 = mutableListOf<List<String>>()
        dadosWakeList0.add(listOf("Type of Wake Lock",
            "Name of the Wake Lock",
            "Total duration for which the Wake Locks kept the device awake",
            "Number of times the Wake Locks stayed active.")
        )

        for (i in 0 until nameWakeList0.size) {
            val type = typeWakeList0[i]
            val name = nameWakeList0[i]
            val tempo = tempoWakeList0[i]
            val vezes = vezesWakeList0[i]

            val dados = listOf(type, name, tempo, vezes)
            dadosWakeList0.add(dados)
        }

        val csvFile = File(Environment.getExternalStorageDirectory().path + "/Documents/SWPerfl/DataOwl/Report/", "kernel_wake_locks.csv")
        val fileWriter = FileWriter(csvFile)
        val csvWriter = CSVWriter(fileWriter)

        for (dados in dadosWakeList0) {
            csvWriter.writeNext(dados.toTypedArray())
        }
        csvWriter.close()
        fileWriter.close()

        Toast.makeText(this, "CSV files created!", Toast.LENGTH_SHORT).show()
        openFolder()
    }

    private fun openFolder() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val uri = Uri.parse(Environment.getExternalStorageDirectory().path + "/Documents/SWPerfl/DataOwl/Report/")
        intent.data = uri
        intent.type = "text/csv"

        val chooser = Intent.createChooser(intent, "Abrir pasta")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        } else {
            // Não há aplicativo de gerenciamento de arquivos instalado no dispositivo
        }
    }
}