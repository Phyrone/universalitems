package de.phyrone.universalitems.utils

import org.bukkit.Material

object LegacyPrefixUtil {
    private val matchMaterialMethod = try {
        Material::class.java.getDeclaredMethod("matchMaterial", String::class.java, Boolean::class.java)
            .also { it.isAccessible = true }
    } catch (e: NoSuchMethodException) {
        null
    }
    private val getMaterialMethod = try {
        Material::class.java.getDeclaredMethod("getMaterial", String::class.java, Boolean::class.java)
            .also { it.isAccessible = true }
    } catch (e: NoSuchMethodException) {
        null
    }

    fun supported() = matchMaterialMethod != null && getMaterialMethod != null
    fun matchMaterialWithLegacyName(name: String) =
        (matchMaterialMethod ?: throw UnsupportedOperationException("too old server version?")).invoke(
            null,
            name,
            true
        ) as? Material

    fun getMaterialWithLegacyName(name: String) =
        (getMaterialMethod ?: throw UnsupportedOperationException("too old server version?")).invoke(
            null,
            name,
            true
        ) as? Material
}
