package com.gempukku.ostream

interface ObjectStream<ObjectType> {
    fun objectCreated(id: String, value: ObjectType)

    fun objectUpdated(id: String, value: ObjectType)

    fun objectRemoved(id: String)
}
