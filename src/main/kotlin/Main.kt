// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import javax.swing.JFileChooser

private const val TEMP_FILE_NAME_TAIL = ".temp"
private const val TARGET_TO_REMOVE = "\"></string>"

@Composable
@Preview
fun App(windowScope: FrameWindowScope) {
    var buttonText by remember { mutableStateOf("Select File") }
    var message by remember { mutableStateOf("") }
    var selectedFile: File? = null

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Button(onClick = {
                selectFile(windowScope.window, selectedFile) {
                    message = ""
                    selectedFile = it
                    buttonText = it.name
                    message = try {
                        removeEmptyString(it)
                        "Success!!"
                    }catch (e:IOException){
                        e.toString()
                    }
                }
            }) {
                Text(text = buttonText)
            }
            Text(text = message)
        }
    }
}

private fun selectFile(window: ComposeWindow, selectedFile: File?, listener: (File) -> Unit) {
    val fileChooser = JFileChooser(selectedFile)
    fileChooser.currentDirectory = selectedFile
    if (fileChooser.showOpenDialog(window) != JFileChooser.APPROVE_OPTION) {
        return
    }
    listener(fileChooser.selectedFile)
}

@Throws(IOException::class)
private fun removeEmptyString(file: File) {
    val tempFile = File("${file.path}$TEMP_FILE_NAME_TAIL")
    BufferedReader(FileReader(file)).use { bufferReader ->
        tempFile.bufferedWriter().use { bufferWriter ->
            var line: String?
            var isWriteFirstLine = true
            while (bufferReader.readLine().also { line = it } != null) {
                if (line!!.contains(TARGET_TO_REMOVE)) {
                    continue
                }
                if (!isWriteFirstLine) {
                    bufferWriter.newLine()
                }
                isWriteFirstLine = false
                bufferWriter.write(line)
            }
        }
    }
    file.delete()
    tempFile.renameTo(file)
}

fun main() = application {
    Window(title = "Remove Empty String", onCloseRequest = ::exitApplication) {
        App(this)
    }
}
