package org.ccgemp.db

import org.sql2o.Sql2o

interface DbAccessInterface {
    fun openDB(): Sql2o
}