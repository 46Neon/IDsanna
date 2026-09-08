package com.idsanna.android

class ToolRegistry {
    private val tools = linkedMapOf<String, ToolDefinition>()

    fun register(tool: ToolDefinition) { tools[tool.name] = tool }
    fun get(name: String): ToolDefinition? = tools[name]
    fun all(): List<ToolDefinition> = tools.values.toList()
    fun isRegistered(name: String): Boolean = tools.containsKey(name)

    fun canPropose(name: String, capabilityRegistry: CapabilityRegistry): Boolean {
        val tool = tools[name] ?: return false
        return capabilityRegistry.isRegistered(tool.capability)
    }

    companion object {
        fun defaults(): ToolRegistry = ToolRegistry().apply {
            register(ToolDefinition("android.open_app", "android", Risk.LOW, requiresConfirmation = false, inputFields = setOf("package")))
            register(ToolDefinition("android.capture_screen", "android", Risk.MEDIUM, requiresConfirmation = false))
            register(ToolDefinition("browser.navigate", "browser", Risk.MEDIUM, requiresConfirmation = true, inputFields = setOf("url")))
            register(ToolDefinition("termux.run_registered_task", "termux", Risk.HIGH, requiresConfirmation = true, inputFields = setOf("task")))
            register(ToolDefinition("network.get_local_ip", "network", Risk.LOW, requiresConfirmation = false))
            register(ToolDefinition("cad.create_geometry", "autocad", Risk.HIGH, requiresConfirmation = true, inputFields = setOf("geometry")))
        }
    }
}

data class ToolDefinition(
    val name: String,
    val capability: String,
    val risk: Risk,
    val requiresConfirmation: Boolean,
    val inputFields: Set<String> = emptySet(),
    val verification: String = "result_present"
)

enum class Risk { LOW, MEDIUM, HIGH }
