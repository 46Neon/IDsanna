package com.idsanna.android

class CapabilityRegistry {
    private val capabilities = linkedMapOf<String, Capability>()

    fun register(capability: Capability) { capabilities[capability.name] = capability }
    fun get(name: String): Capability? = capabilities[name]
    fun all(): List<Capability> = capabilities.values.toList()
    fun isRegistered(name: String): Boolean = capabilities.containsKey(name)

    fun canUse(name: String, grantedPermissions: Set<String> = emptySet()): Boolean {
        val capability = capabilities[name] ?: return false
        return capability.enabled && capability.requiredPermissions.all(grantedPermissions::contains)
    }

    companion object {
        fun defaults(): CapabilityRegistry = CapabilityRegistry().apply {
            register(Capability("android", "Android", enabled = true))
            register(Capability("browser", "Navegador", enabled = false, requiredPermissions = setOf("browser_access")))
            register(Capability("termux", "Termux", enabled = false, requiredPermissions = setOf("termux_access")))
            register(Capability("network", "Red autorizada", enabled = true))
            register(Capability("autocad", "AutoCAD", enabled = false, requiredPermissions = setOf("app_access")))
        }
    }
}

data class Capability(
    val name: String,
    val displayName: String,
    val enabled: Boolean,
    val requiredPermissions: Set<String> = emptySet(),
    val tools: Set<String> = emptySet()
)
