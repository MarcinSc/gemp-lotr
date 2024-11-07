package org.ccgemp.common

fun String.splitText(joinCharacter: Char, limit: Int = 0, escapeChar: Char = '\\'): List<String> {
    if (joinCharacter == escapeChar) {
        throw IllegalArgumentException()
    }
    if (this == escapeChar.toString()) {
        return emptyList()
    }
    val result = mutableListOf<String>()
    val sb = StringBuilder()
    var escape = false
    this.forEach {
        if (escape) {
            sb.append(it)
            escape = false
        } else {
            if (limit != 0 && limit == result.size + 1) {
                when (it) {
                    escapeChar -> {
                        escape = true
                    }

                    else -> {
                        sb.append(it)
                    }
                }
            } else {
                when (it) {
                    escapeChar -> {
                        escape = true
                    }

                    joinCharacter -> {
                        result.add(sb.toString())
                        sb.clear()
                    }

                    else -> {
                        sb.append(it)
                    }
                }
            }
        }
    }
    result.add(sb.toString())
    return result
}

fun List<String>.mergeTexts(joinCharacter: Char, escapeChar: Char = '\\'): String {
    if (joinCharacter == escapeChar) {
        throw IllegalArgumentException()
    }
    if (this.isEmpty()) {
        return escapeChar.toString()
    }
    return this.joinToString(joinCharacter.toString()) {
        it.replace(escapeChar.toString(), escapeChar.toString() + escapeChar.toString())
            .replace(joinCharacter.toString(), escapeChar.toString() + joinCharacter.toString())
    }
}
