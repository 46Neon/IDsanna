package com.idsanna.android

class InstructionParser {
    fun parse(input: String): ParsedInstruction {
        val normalized = input.trim().lowercase().replace(Regex("\\s+"), " ")
        val tokens = normalized.split(" ").filter { it.isNotBlank() }.map { Token(it, classify(it)) }
        val verb = tokens.firstOrNull()?.value ?: ""
        val target = when {
            normalized.contains("autocad") -> "autocad"
            normalized.contains("termux") || normalized.contains("terminal") -> "termux"
            normalized.contains("navegador") || normalized.contains("chrome") || normalized.contains("web") -> "browser"
            normalized.contains("dns") || normalized.contains("ip") || normalized.contains("puerto") || normalized.contains("red") -> "network"
            normalized.contains("android") || normalized.contains("teléfono") || normalized.contains("movil") -> "android"
            else -> "unknown"
        }
        val intent = when {
            verb in setOf("abre", "abrir", "inicia", "iniciar") -> "open"
            verb in setOf("crea", "crear", "genera", "generar") -> "create"
            verb in setOf("ejecuta", "ejecutar", "corre", "correr") -> "execute"
            verb in setOf("consulta", "consultar", "revisa", "revisar", "muestra", "mostrar") -> "inspect"
            verb in setOf("cambia", "cambiar", "modifica", "modificar") -> "change"
            else -> "unknown"
        }
        return ParsedInstruction(normalized, tokens, intent, target, input.trim().isNotEmpty())
    }

    private fun classify(value: String): String = when {
        value.matches(Regex("\\d+([.,]\\d+)?")) -> "number"
        value in setOf("abre", "abrir", "crea", "crear", "ejecuta", "ejecutar", "consulta", "revisa", "cambia", "modifica") -> "verb"
        value in setOf("autocad", "termux", "terminal", "chrome", "web", "dns", "ip", "puerto", "red", "android") -> "target_or_entity"
        else -> "text"
    }
}

data class Token(val value: String, val type: String)
data class ParsedInstruction(val normalized: String, val tokens: List<Token>, val intent: String, val target: String, val hasContent: Boolean)
