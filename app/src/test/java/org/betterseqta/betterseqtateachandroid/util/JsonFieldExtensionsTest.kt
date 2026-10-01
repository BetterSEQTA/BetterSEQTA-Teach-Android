package org.betterseqta.betterseqtateachandroid.util

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonFieldExtensionsTest {

    @Test
    fun seqtaTruthyField_acceptsJsonBooleanTrue() {
        val obj = buildJsonObject { put("read", JsonPrimitive(true)) }
        assertTrue(obj.seqtaTruthyField("read"))
    }

    @Test
    fun seqtaTruthyField_acceptsIntOne() {
        val obj = buildJsonObject { put("read", JsonPrimitive(1)) }
        assertTrue(obj.seqtaTruthyField("read"))
    }

    @Test
    fun seqtaTruthyField_acceptsStringTrue() {
        val obj = buildJsonObject { put("read", JsonPrimitive("true")) }
        assertTrue(obj.seqtaTruthyField("read"))
    }

    @Test
    fun seqtaTruthyField_falseWhenMissing() {
        val obj = buildJsonObject { }
        assertFalse(obj.seqtaTruthyField("read"))
    }
}
