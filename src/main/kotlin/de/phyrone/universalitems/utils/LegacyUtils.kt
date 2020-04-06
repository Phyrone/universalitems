package de.phyrone.universalitems.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object LegacyUtils {
    private val method = try {
        Material::class.java.getDeclaredMethod("getMaterial", Int::class.java).also {
            it.isAccessible = true
        }
    } catch (e: NoSuchMethodException) {
        null
    }
    private val idDataItemStackConstruktor =
        try {
            ItemStack::class.java.getDeclaredConstructor(Int::class.java, Int::class.java, Short::class.java).also {
                it.isAccessible = true
            }
        } catch (e: NoSuchMethodException) {
            null
        }
    private val materialDataItemStackConstruktor =
        try {
            ItemStack::class.java.getDeclaredConstructor(Material::class.java, Int::class.java, Short::class.java)
                .also {
                    it.isAccessible = true
                }
        } catch (e: NoSuchMethodException) {
            null
        }

    fun isLegacy() = method != null && idDataItemStackConstruktor != null
    fun materialByLegacyId(id: Int): Material? =
        (method ?: throw UnsupportedOperationException("not legacy")).invoke(null, id) as? Material

    fun getLegacyItemStack(id: Int, data: Short): ItemStack =
        (idDataItemStackConstruktor ?: throw UnsupportedOperationException("not legacy")).newInstance(
            id,
            1,
            data
        )

    fun getLegacyItemStack(material: Material, data: Short): ItemStack =
        (idDataItemStackConstruktor ?: throw UnsupportedOperationException("not legacy")).newInstance(
            material,
            1,
            data
        )

}