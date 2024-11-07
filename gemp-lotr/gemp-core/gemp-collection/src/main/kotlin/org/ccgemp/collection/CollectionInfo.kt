package org.ccgemp.collection

class CollectionInfo {
    var id: Int = 0
    var player: String? = null
    var type: String? = null

    constructor() {
    }

    constructor(id: Int, player: String, type: String) {
        this.id = id
        this.player = player
        this.type = type
    }
}
