package com.minek.kotlin.everywhere.keuse.testCommon

import com.minek.kotlin.everywehre.keuson.convert.Converters
import com.minek.kotlin.everywehre.keuson.decode.Decoders
import com.minek.kotlin.everywehre.keuson.decode.map
import com.minek.kotlin.everywehre.keuson.encode.Encoders
import com.minek.kotlin.everywhere.keuse.Crate

class TestCrate : Crate() {
    val add by e(AddReq.converter, Converters.int)
    val inner by c(::TestInnerCrate)

    data class AddReq(val first: Int, val second: Int) {
        companion object {
            private val decoder = map(Decoders.field("first", Decoders.int), Decoders.field("second", Decoders.int), ::AddReq)
            private val encoder = { (first, second): AddReq ->
                Encoders.object_("first" to Encoders.int(first), "second" to Encoders.int(second))
            }
            val converter = encoder to decoder
        }
    }
}

class TestInnerCrate : Crate() {
    val flip by e(Converters.boolean, Converters.boolean)
}
