package org.ccgemp.deck

fun split(text: String, joinCharacter: Char = ',', escapeChar: Char = '\\'): List<String> {
    val result = mutableListOf<String>()
    val sb = StringBuilder()
    var escape = false
    text.forEach {
        if (escape) {
            sb.append(it)
            escape = false
        } else {
            when (it) {
                escapeChar -> {
                    escape = true
                }
                joinCharacter -> {
                    result.add(sb.toString())
                }
                else -> {
                    sb.append(it)
                }
            }
        }
    }
    result.add(sb.toString())
    return result
}

fun merge(texts: List<String>, joinCharacter: Char = ',', escapeChar: Char = '\\'): String {
    return texts.joinToString(separator = joinCharacter.toString()) {
        it.replace(escapeChar.toString(), escapeChar.toString() + escapeChar.toString())
            .replace(joinCharacter.toString(), escapeChar.toString() + joinCharacter.toString())
    }
}
