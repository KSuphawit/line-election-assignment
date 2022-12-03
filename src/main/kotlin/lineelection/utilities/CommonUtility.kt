package lineelection.utilities

import lineelection.constants.invalidNationalId


/**
 * Method for validate if criteria not matched this function will throw an error
 */
infix fun String.validate(criteria: Boolean) {
    if (!criteria) throw IllegalArgumentException(this)
}

/**
 * Method for validate thai nation id
 */
fun isValidNationalId(nationID : String?) {

    if (nationID.isNullOrBlank() || nationID.length != 13) throw IllegalArgumentException(invalidNationalId)

    try {
        var sum = 0

        for (i in 0..11) {
            sum += nationID[i].digitToInt() * (13 - i);
        }

        val mod = sum % 11;

        val checkSum = (11 - mod) % 10;

        if (checkSum != nationID[12].digitToInt()) throw IllegalArgumentException(invalidNationalId)

    } catch (e: Exception) {
        throw IllegalArgumentException(invalidNationalId)
    }
}

/**
 * Use for get field value via dynamic field name.
 * @param targetFieldName: field that we need to get value
 * @param ignoreCase: field to support getting field name with case insensitive, default value is false (case sensitive)
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.getFieldValue(targetFieldName: String, ignoreCase: Boolean = false): T? {
    val clazz = this::class.java
    val targetField = clazz.declaredFields.singleOrNull { it.name.equals(targetFieldName, ignoreCase = ignoreCase) }
    return if (targetField != null) {
        targetField.isAccessible = true
        targetField.get(this) as T
    } else {
        throw IllegalArgumentException("Field: $targetFieldName not exist in class ${clazz.simpleName}")
    }
}

