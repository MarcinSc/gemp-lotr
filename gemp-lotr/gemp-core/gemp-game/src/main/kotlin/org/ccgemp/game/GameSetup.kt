package org.ccgemp.game

fun createGameSystems(): List<Any> {
    return listOf(
        GameContainerSystem(),
        GameApiSystem<Any, Any>(),
    )
}
