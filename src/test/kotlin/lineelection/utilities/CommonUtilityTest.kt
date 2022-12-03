package lineelection.utilities

import lineelection.Try
import lineelection.constants.invalidNationalId
import org.junit.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommonUtilityTest {

    data class Person(
        val id: Long? = null,
        val name: String? = null,
        val salary: BigDecimal? = null,
        val dob: Date? = null
    )

    private val person = Person(
        id = 1L,
        name = "ABC",
        salary = BigDecimal.TEN,
        dob = Date()
    )

    @Test
    fun testValidate() {
        // Case not pass criteria
        var result = Try.on {
            "1 is not equal 2" validate (1 == 2)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("1 is not equal 2"))

        // Case pass criteria
        result = Try.on {
            "1 is equal 2" validate (1 != 2)
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun testIsValidNationId() {
        // Case Invalid national id due to input is null
        var result = Try.on {
            isValidNationalId(null)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(invalidNationalId))

        // Case Invalid national id due to input is blank
        result = Try.on {
            isValidNationalId("    ")
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(invalidNationalId))

        // Case Invalid national id due to check sum not matched
        result = Try.on {
            isValidNationalId("1234567890123")
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(invalidNationalId))

        // Case pass
        result = Try.on {
            isValidNationalId("1130100550069")
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun testGetFiledValue() {
        // Case "Field: abc not exist in class lineelection.utilities.Person"
        var result = Try.on {
            person.getFieldValue<Any>("abc")
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Field: abc not exist in class Person"))

        // Case get Long type
        result = Try.on {
            person.getFieldValue<Long>(Person::id.name)
        }
        assertTrue(result.isSuccess)
        assertEquals(person.id, result.getOrThrow())

        // Case get String type
        result = Try.on {
            person.getFieldValue<String>(Person::name.name)
        }
        assertTrue(result.isSuccess)
        assertEquals(person.name, result.getOrThrow())

        // Case get BigDecimal type
        result = Try.on {
            person.getFieldValue<BigDecimal>(Person::salary.name)
        }
        assertTrue(result.isSuccess)
        assertEquals(person.salary, result.getOrThrow())

        // Case get Date type
        result = Try.on {
            person.getFieldValue<Date>(Person::dob.name)
        }
        assertTrue(result.isSuccess)
        assertEquals(person.dob, result.getOrThrow())
    }
}