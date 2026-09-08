package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstructionParserTest {
    private val parser = InstructionParser()

    @Test
    fun parsesValidAutoCadInstructionWithMeasurement() {
        val result = parser.parse("Crea un plano en AutoCAD de 4 metros")
        assertEquals("create", result.intent)
        assertEquals("autocad", result.target)
        assertTrue(result.valid)
        assertEquals("4 metros", result.parameters["measurement_1"])
    }

    @Test
    fun rejectsIncompleteAutoCadInstruction() {
        val result = parser.parse("Crea un plano en AutoCAD")
        assertFalse(result.valid)
        assertTrue(result.errors.contains("missing_dimensions"))
    }

    @Test
    fun rejectsUnknownInstruction() {
        val result = parser.parse("haz algo")
        assertFalse(result.valid)
        assertTrue(result.errors.contains("unknown_intent"))
    }
}
