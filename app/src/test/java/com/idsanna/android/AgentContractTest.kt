package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentContractTest {
    private val agent = AgentContract()

    @Test
    fun incompleteAutocadInstructionNeedsClarification() {
        val result = agent.analyze("crea un edificio en AutoCAD")

        assertEquals(AgentStatus.NEEDS_CLARIFICATION, result.status)
        assertEquals(listOf("dimensions"), result.missing)
        assertTrue(result.plan.isEmpty())
    }

    @Test
    fun completeAutocadInstructionProducesPlan() {
        val result = agent.analyze("crea una pared de 4 metros en AutoCAD")

        assertEquals(AgentStatus.READY_FOR_APPROVAL, result.status)
        assertEquals("create", result.intent)
        assertEquals("autocad", result.target)
        assertEquals("cad.create_geometry", result.plan.single().tool)
        assertTrue(result.requiresConfirmation)
    }

    @Test
    fun unknownInstructionIsInvalid() {
        val result = agent.analyze("haz algo")

        assertEquals(AgentStatus.INVALID, result.status)
        assertTrue(result.errors.contains("unknown_intent"))
    }
}
