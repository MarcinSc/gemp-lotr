package org.ccgemp.server.player

fun String.convertToRoleSet(): Set<String> {
    val result = mutableSetOf<String>()
    toCharArray().forEach {
        result.add(it.toString())
    }
    return result
}
