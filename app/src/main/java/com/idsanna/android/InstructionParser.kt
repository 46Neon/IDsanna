package com.idsanna.android

class InstructionParser {
    fun parse(input: String): ParsedInstruction {
        val original = input.trim()
        val normalized = original.lowercase().replace(Regex("\\s+"), " ")
        val tokens = normalized.split(" ").filter { it.isNotBlank() }.map { Token(it, classify(it)) }
        val verb = tokens.firstOrNull()?.value.orEmpty()
        val target = detectTarget(normalized)
        val intent = detectIntent(verb)
        val parameters = mutableMapOf<String, String>()
        Regex("(\\d+(?:[.,]\\d+)?)\\s*(m|metros?|cm|centímetros?)").findAll(normalized).forEachIndexed { index, match ->
            parameters["measurement_${index + 1}"] = "${match.groupValues[1].replace(',', '.')} ${match.groupValues[2]}"
        }
        val errors = mutableListOf<String>()
        if (original.isEmpty()) errors.add("empty_instruction")
        if (intent == "unknown") errors.add("unknown_intent")
        if (target == "unknown") errors.add("unknown_target")
        if (intent == "create" && target == "autocad" && parameters.isEmpty()) errors.add("missing_dimensions")
        return ParsedInstruction(normalized, tokens, intent, target, parameters, errors)
    }

    private fun detectTarget(text: String): String = when {
        text.contains("autocad") -> "autocad"
        text.contains("termux") || text.contains("terminal") -> "termux"
        text.contains("navegador") || text.contains("chrome") || text.contains("web") -> "browser"
        text.contains("dns") || text.contains("ip") || text.contains("puerto") || text.contains("red") -> "network"
        text.contains("android") || text.contains("teléfono") || text.contains("movil") -> "android"
        else -> "unknown"
    }

    private fun detectIntent(verb: String): String = when (verb) {
        "abre", "abrir", "inicia", "iniciar" -> "open"
        "crea", "crear", "genera", "generar" -> "create"
        "ejecuta", "ejecutar", "corre", "correr" -> "execute"
        "consulta", "consultar", "revisa", "revisar", "muestra", "mostrar" -> "inspect"
        "cambia", "cambiar", "modifica", "modificar" -> "change"
        else -> "unknown"
    }

    private fun classify(value: String): String = when {
        value.matches(Regex("\\d+([.,]\\d+)?")) -> "number"
        value in setOf("abre", "abrir", "crea", "crear", "ejecuta", "ejecutar", "consulta", "revisa", "cambia", "modifica") -> "verb"
        value in setOf("autocad", "termux", "terminal", "chrome", "web", "dns", "ip", "puerto", "red", "android") -> "target_or_entity"
        else -> "text"
    }
}

data class Token(val value: String, val type: String)
data class ParsedInstruction(
    val normalized: String,
    val tokens: List<Token>,
    val intent: String,
    val target: String,
    val parameters: Map<String, String>,
    val errors: List<String>
) {
    val valid: Boolean get() = errors.isEmpty()
}
