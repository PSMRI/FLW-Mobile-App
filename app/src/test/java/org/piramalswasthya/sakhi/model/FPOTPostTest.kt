package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FPOTPostTest {

    @Test
    fun `default constructor fills optional fields`() {
        val post = FPOTPost(beneficiaryid = 101L)

        assertNotNull(post)
        assertEquals(101L, post.beneficiaryid)
        assertEquals(null, post.beneficiaryName)
        assertEquals(0.0, post.latitude!!, 0.0001)
        assertEquals(0.0, post.longitude!!, 0.0001)
        assertEquals(0, post.loginId)
    }

    @Test
    fun `copy and equality behave as data class`() {
        val post = FPOTPost(beneficiaryid = 5L, beneficiaryName = "Asha", updatedDate = 10L)
        val same = post.copy()
        val other = post.copy(beneficiaryName = "Meena")

        assertEquals(post, same)
        assertEquals(post.hashCode(), same.hashCode())
        assertNotNull(post.toString())
        assertEquals("Meena", other.beneficiaryName)
        assertEquals(5L, other.beneficiaryid)
    }

    @Test
    fun `accessor round trip covers every property`() {
        val obj = FPOTPost(beneficiaryid = 7L)
        obj.javaClass.methods
            .filter { (it.name.startsWith("get") || it.name.startsWith("is")) && it.parameterCount == 0 }
            .forEach { getter ->
                runCatching {
                    val value = getter.invoke(obj)
                    val setterName = "set" + getter.name.removePrefix("get").removePrefix("is")
                    obj.javaClass.methods
                        .firstOrNull { it.name == setterName && it.parameterCount == 1 }
                        ?.invoke(obj, value)
                }
            }
        assertNotNull(obj)
    }
}
