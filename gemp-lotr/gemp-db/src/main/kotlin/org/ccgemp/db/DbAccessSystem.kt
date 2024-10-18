package org.ccgemp.db

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.InjectProperty
import com.gempukku.context.resolver.expose.Exposes
import org.apache.commons.dbcp2.ConnectionFactory
import org.apache.commons.dbcp2.DriverManagerConnectionFactory
import org.apache.commons.dbcp2.PoolableConnection
import org.apache.commons.dbcp2.PoolableConnectionFactory
import org.apache.commons.dbcp2.PoolingDataSource
import org.apache.commons.pool2.impl.GenericObjectPool
import org.sql2o.Sql2o
import org.sql2o.converters.Converter
import org.sql2o.quirks.NoQuirks
import java.time.LocalDate
import java.util.*

@Exposes(DbAccessInterface::class, LifecycleObserver::class)
class DbAccessSystem : DbAccessInterface, LifecycleObserver {
    @InjectProperty("db.connection.class")
    private lateinit var connectionClass: String

    @InjectProperty("db.connection.url")
    private lateinit var connectionUrl: String

    @InjectProperty("db.connection.username")
    private lateinit var username: String

    @InjectProperty("db.connection.password")
    private lateinit var password: String

    @InjectProperty("db.connection.validateQuery")
    private lateinit var validateQuery: String

    @InjectProperty("db.connection.batch")
    private var batch: Boolean = false

    private var dataSource: PoolingDataSource<PoolableConnection>? = null

    private val customMappers: Map<Class<*>, Converter<*>> = mapOf(LocalDate::class.java to LocalDateConverter())

    override fun afterContextStartup() {
        try {
            Class.forName(connectionClass)
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("Couldn't find the DB driver", e)
        }

        dataSource = setupDataSource(connectionUrl, username, password, batch)
    }

    override fun beforeContextStopped() {
        dataSource?.close()
        dataSource = null
    }

    override fun openDB(): Sql2o {
        return Sql2o(dataSource, NoQuirks(customMappers))
    }

    private fun setupDataSource(
        connectURI: String,
        user: String,
        pass: String,
        batch: Boolean
    ): PoolingDataSource<PoolableConnection> {
        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        val props: Properties = object : Properties() {
            init {
                setProperty("user", user)
                setProperty("password", pass)
                setProperty("rewriteBatchedStatements", batch.toString().lowercase(Locale.getDefault()))
                setProperty("innodb_autoinc_lock_mode", "2")
            }
        }
        val connectionFactory: ConnectionFactory =
            DriverManagerConnectionFactory(connectURI, props)

        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        val poolableConnectionFactory: PoolableConnectionFactory = PoolableConnectionFactory(connectionFactory, null)
        poolableConnectionFactory.defaultAutoCommit = true
        poolableConnectionFactory.defaultReadOnly = false
        poolableConnectionFactory.validationQuery = validateQuery

        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        val connectionPool: GenericObjectPool<PoolableConnection> =
            GenericObjectPool(poolableConnectionFactory)
        connectionPool.testOnBorrow = true

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.pool = connectionPool

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        return PoolingDataSource<PoolableConnection>(connectionPool)
    }

}