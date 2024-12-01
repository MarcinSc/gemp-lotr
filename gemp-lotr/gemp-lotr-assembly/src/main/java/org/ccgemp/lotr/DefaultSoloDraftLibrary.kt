package org.ccgemp.lotr

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class DefaultSoloDraftLibrary(draftFolder: File) : SoloDraftLibrary {
    override val allSoloDraftFormats = mutableMapOf<String, String>()

    init {
        loadDrafts(draftFolder)
    }

    private fun loadDrafts(path: File) {
        if (path.isFile) {
            loadDraft(path)
        } else if (path.isDirectory) {
            for (file in path.listFiles()!!) {
                loadDrafts(file)
            }
        }
    }

    private fun loadDraft(file: File) {
        try {
            val reader = InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8)
            try {
                val parser = JSONParser()
                val draft = parser.parse(reader) as JSONObject
                val format = draft["format"] as String
                val code = draft["code"] as String

                if (allSoloDraftFormats.containsKey(code)) println("Duplicate draft loaded: $code")

                allSoloDraftFormats[code] = format
            } catch (exp: org.json.simple.parser.ParseException) {
                throw RuntimeException("Problem loading solo draft $file", exp)
            }
        } catch (exp: IOException) {
            throw RuntimeException("Problem loading solo draft $file", exp)
        }
    }
}
