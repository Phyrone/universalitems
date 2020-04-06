import com.google.gson.GsonBuilder
import de.phyrone.universalitems.ItemConfigEntry
import de.phyrone.universalitems.ItemConfigEntryList
import org.bukkit.Material
import org.junit.Test

class ItemfileBuilder {

    /* builds you a itemlist.json and prints it + a small read validation*/
    @Test
    fun buildLegacy() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val cfg = Material.values().map { material ->
            ItemConfigEntry(
                material.id,
                0,
                listOf(),
                listOf(material.name)
            )
        }
        val json = gson.toJson(cfg)
        println(json)
        /* test json read*/
        val readCfg = gson.fromJson(json, ItemConfigEntryList::class.java)
        println(readCfg.first()::class.java.name)
    }
}