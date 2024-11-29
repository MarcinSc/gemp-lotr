package org.ccgemp.game

fun <GameEvent, ObserverSettings> createGameSystems(
    gameEventSinkProducer: GameEventSinkProducer<GameEvent>,
    gameObserveSettingsExtractor: GameObserveSettingsExtractor<ObserverSettings>,
    gameProducer: GameProducer<ObserverSettings>,
): List<Any> {
    return listOf(
        GameContainerSystem(),
        GameApiSystem<Any, Any>(),
        gameEventSinkProducer,
        gameObserveSettingsExtractor,
        gameProducer,
    )
}
