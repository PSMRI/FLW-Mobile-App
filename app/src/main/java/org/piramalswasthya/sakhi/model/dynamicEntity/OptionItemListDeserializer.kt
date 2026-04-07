package org.piramalswasthya.sakhi.model.dynamicEntity

/**
 * Utility to convert raw options (from Gson) into List<OptionItem>.
 *
 * Gson deserializes "options" as Any?, which at runtime becomes:
 * - List<LinkedTreeMap> for [{"id":20,"value":"Home","label":"Home"},...]
 * - List<String> for ["Yes","No"]
 * - null if not present
 *
 * This function handles all cases without relying on Gson TypeAdapters.
 */
object OptionItemParser {

    fun parse(raw: Any?): List<OptionItem>? {
        if (raw == null) return null
        if (raw !is List<*>) return null

        val result = raw.mapNotNull { item ->
            when (item) {
                is OptionItem -> item
                is Map<*, *> -> {
                    val value = item["value"]?.toString() ?: return@mapNotNull null
                    val label = item["label"]?.toString() ?: value
                    OptionItem(label = label, value = value)
                }
                is String -> OptionItem(label = item, value = item)
                else -> null
            }
        }
        return result.ifEmpty { null }
    }
}

/** Extension on FormFieldDto to get parsed option items. */
fun FormFieldDto.optionItems(): List<OptionItem>? = OptionItemParser.parse(options)
