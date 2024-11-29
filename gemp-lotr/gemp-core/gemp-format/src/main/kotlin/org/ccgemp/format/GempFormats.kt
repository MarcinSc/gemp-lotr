package org.ccgemp.format

import org.ccgemp.common.DeckValidator

interface GempFormats<Format> {
    fun findFormat(format: String): Format?
    fun getAllFormats(): List<Format>
    fun getValidator(format: String): DeckValidator
}