package com.gempukku.context.resource

import java.io.InputStream

interface FileResource {
    fun createInputStream(): InputStream?
    fun describe(): String
}