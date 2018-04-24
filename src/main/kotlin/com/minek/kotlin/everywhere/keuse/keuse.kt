package com.minek.kotlin.everywhere.keuse

import com.goebl.david.Webb
import com.minek.kotlin.everywehre.keuson.convert.Converter
import com.minek.kotlin.everywehre.keuson.convert.decoder
import com.minek.kotlin.everywehre.keuson.convert.encoder
import com.minek.kotlin.everywehre.keuson.decode.Decoder
import com.minek.kotlin.everywehre.keuson.decode.decodeString
import com.minek.kotlin.everywehre.keuson.encode.Encoder
import com.minek.kotlin.everywehre.keuson.encode.encode
import com.minek.kotlin.everywhere.kelibs.result.Err
import com.minek.kotlin.everywhere.kelibs.result.Result
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


abstract class Crate {
    private var remote: String = "http://localhost:5000"
    internal val url: String
        get() {
            val parent = parent
            return if (parent == null) remote else parent.first.url + "/" + parent.second
        }
    private var parent: Pair<Crate, String>? = null

    fun <P, R> e(parameterConvert: Converter<P>, resultConverter: Converter<R>): EndPoint.Delegate<P, R> {
        return EndPoint.Delegate(this, parameterConvert, resultConverter)
    }

    fun <C : Crate> c(constructor: () -> C): Delegate<C> {
        return Delegate(constructor)
    }

    fun i(remote: String = "http://localhost:5000") {
        if (remote.endsWith("/")) {
            throw IllegalArgumentException("remote should not ends with /")
        }
        this.remote = remote
    }

    class Delegate<out C : Crate>(private val constructor: () -> C) : ReadOnlyProperty<Crate, C> {
        private var crate: C? = null

        override fun getValue(thisRef: Crate, property: KProperty<*>): C {
            return crate ?: constructor().apply {
                crate = this
                parent = thisRef to property.name
            }
        }
    }
}

class EndPoint<in P, out R>(private val crate: Crate, private val name: String, parameterConvert: Converter<P>, resultConverter: Converter<R>) {
    private val parameterEncoder: Encoder<P> = parameterConvert.encoder
    private val resultDecoder: Decoder<R> = resultConverter.decoder
    private val url: String
        get() = crate.url + '/' + name

    operator fun invoke(parameter: P): Result<String, R> {
        return try {
            Webb.create()
                    .post(url)
                    .body(encode(parameterEncoder(parameter)))
                    .ensureSuccess()
                    .asString()
                    .body.let { decodeString(resultDecoder, it) }
        } catch (e: Exception) {
            Err(e.message ?: e.toString())
        }
    }

    class Delegate<in P, out R>(private val crate: Crate, private val parameterConvert: Converter<P>, private val resultConverter: Converter<R>) : ReadOnlyProperty<Crate, EndPoint<P, R>> {
        private var endpoint: EndPoint<P, R>? = null

        override fun getValue(thisRef: Crate, property: KProperty<*>): EndPoint<P, R> {
            return endpoint ?: EndPoint(crate, property.name, parameterConvert, resultConverter)
        }
    }
}