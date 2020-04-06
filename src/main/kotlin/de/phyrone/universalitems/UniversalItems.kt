package de.phyrone.universalitems

import com.google.gson.GsonBuilder
import de.phyrone.universalitems.utils.LegacyPrefixUtil
import de.phyrone.universalitems.utils.LegacyUtils
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.io.InputStreamReader
import java.util.function.Supplier


class UniversalItems(private val plugin: Plugin, val itemListOverride: Supplier<List<ItemConfigEntry>>?) {
    constructor(plugin: Plugin) : this(plugin, null)

    private val logger = plugin.logger

    /* a map with simpler names */
    private val aliases = HashMap<String, Supplier<ItemStack>>()

    /* keep support for legacy ids in newer versions */
    private val legacyIds = HashMap<Pair<Int, Short>, Supplier<ItemStack>>()

    private val isLegacy = LegacyUtils.isLegacy()
    private val supportLegacyPrefix = LegacyPrefixUtil.supported()

    private fun getItemsConfig(): List<ItemConfigEntry> {
        return if (itemListOverride != null) {
            itemListOverride.get()
        } else {
            logger.info("loading $ITEMLIST_FILE...")
            val fileStream = plugin.getResource(ITEMLIST_FILE)
            if (fileStream != null) {
                gson.fromJson(InputStreamReader(fileStream), ItemConfigEntryList::class.java)
            } else {
                logger.warning("$ITEMLIST_FILE not found inside plugin '${plugin.name}' legacy ids and aliases may not work")
                listOf()
            }
        }

    }

    private fun ItemConfigEntry.findMaterial(): Material? {
        for (mat in materials) {
            val material = Material.getMaterial(mat)
            if (material != null) return material
        }
        if (supportLegacyPrefix) {
            for (mat in materials) {
                val material = LegacyPrefixUtil.getMaterialWithLegacyName(mat)
                if (material != null) return material
            }
        }
        return null
    }

    private fun initMaps() {
        val config = getItemsConfig()
        config.forEach { entry ->
            val material = entry.findMaterial() ?: return@forEach
            val legacyId = entry.legacyId
            val legacyData = entry.legacyData ?: 0
            val supplier =
                if (legacyData != 0.toShort() && isLegacy) {
                    LegacyItemStackSupplier(material, legacyData)
                } else {
                    ItemStackSupplier(material)
                }
            entry.aliases?.forEach { alias -> aliases[alias.trim().toLowerCase()] = supplier }
            if (legacyId != null) {
                legacyIds[Pair(legacyId, legacyData)] = supplier
            }
        }
    }


    fun getItem(name: String) = getItemSupplier(name)?.get()

    fun getItemSupplier(name: String): Supplier<ItemStack>? {
        val matString = name.trim()
        return when {
            itemIdPattern.matches(matString) -> {
                getItemById(matString.toInt(), 0)
            }
            itemIdWithDataPattern.matches(matString) -> {
                val splited = matString.split(":")
                getItemById(splited.component1().toInt(), splited.component2().toShort())
            }
            else -> {
                var material = Material.matchMaterial(matString)
                if (material == null && supportLegacyPrefix) {
                    material = LegacyPrefixUtil.matchMaterialWithLegacyName(matString)
                }
                if (material == null) {
                    findByAlias(matString)
                } else {
                    ItemStackSupplier(material)
                }

            }
        }
    }

    private fun findByAlias(alias: String): Supplier<ItemStack>? = aliases[alias.toLowerCase()]

    private fun getItemById(id: Int, data: Short) = if (isLegacy) {
        LegacyIdItemStackSupplier(id, data)
    } else {
        legacyIds[Pair(id, data)]
    }

    init {
        initMaps()
    }

    companion object {
        private val gson = GsonBuilder().setPrettyPrinting().create()
        private val itemIdPattern = Regex("^[0-9]+")
        private val itemIdWithDataPattern = Regex("^[0-9]+:[0-9]+")
        private const val ITEMLIST_FILE = "itemlist.json"
    }
}

private class LegacyIdItemStackSupplier(val id: Int, val data: Short) : Supplier<ItemStack> {
    override fun get() = LegacyUtils.getLegacyItemStack(id, data)
}

private class LegacyItemStackSupplier(val material: Material, val data: Short) : Supplier<ItemStack> {
    override fun get() = LegacyUtils.getLegacyItemStack(material, data)
}

private class ItemStackSupplier(val material: Material) : Supplier<ItemStack> {
    override fun get() = ItemStack(material)

}