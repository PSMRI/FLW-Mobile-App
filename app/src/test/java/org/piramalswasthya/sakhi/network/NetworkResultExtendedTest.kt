package org.piramalswasthya.sakhi.network

import org.junit.Assert.*
import org.junit.Test

class NetworkResultExtendedTest {

    // =====================================================
    // NetworkResult.Success Tests
    // =====================================================

    @Test fun `Success with string data`() {
        val result = NetworkResult.Success("hello")
        assertEquals("hello", result.data)
    }

    @Test fun `Success with int data`() {
        val result = NetworkResult.Success(42)
        assertEquals(42, result.data)
    }

    @Test fun `Success with list data`() {
        val result = NetworkResult.Success(listOf(1, 2, 3))
        assertEquals(3, result.data.size)
    }

    @Test fun `Success with empty list data`() {
        val result = NetworkResult.Success(emptyList<Int>())
        assertTrue(result.data.isEmpty())
    }

    @Test fun `Success with boolean true`() {
        val result = NetworkResult.Success(true)
        assertTrue(result.data)
    }

    @Test fun `Success with boolean false`() {
        val result = NetworkResult.Success(false)
        assertFalse(result.data)
    }

    @Test fun `Success with map data`() {
        val result = NetworkResult.Success(mapOf("key" to "value"))
        assertEquals("value", result.data["key"])
    }

    @Test fun `Success with Long data`() {
        val result = NetworkResult.Success(Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE, result.data)
    }

    @Test fun `Success equality`() {
        val a = NetworkResult.Success("hello")
        val b = NetworkResult.Success("hello")
        assertEquals(a, b)
    }

    @Test fun `Success inequality`() {
        val a = NetworkResult.Success("hello")
        val b = NetworkResult.Success("world")
        assertNotEquals(a, b)
    }

    // =====================================================
    // NetworkResult.Error Tests
    // =====================================================

    @Test fun `Error with code and message`() {
        val result = NetworkResult.Error(500, "Internal Server Error")
        assertEquals(500, result.code)
        assertEquals("Internal Server Error", result.message)
    }

    @Test fun `Error with 404`() {
        val result = NetworkResult.Error(404, "Not Found")
        assertEquals(404, result.code)
    }

    @Test fun `Error with 401`() {
        val result = NetworkResult.Error(401, "Unauthorized")
        assertEquals(401, result.code)
    }

    @Test fun `Error with empty message`() {
        val result = NetworkResult.Error(0, "")
        assertEquals("", result.message)
    }

    @Test fun `Error equality`() {
        val a = NetworkResult.Error(500, "error")
        val b = NetworkResult.Error(500, "error")
        assertEquals(a, b)
    }

    @Test fun `Error inequality by code`() {
        val a = NetworkResult.Error(500, "error")
        val b = NetworkResult.Error(404, "error")
        assertNotEquals(a, b)
    }

    @Test fun `Error inequality by message`() {
        val a = NetworkResult.Error(500, "error1")
        val b = NetworkResult.Error(500, "error2")
        assertNotEquals(a, b)
    }

    // =====================================================
    // NetworkResult.NetworkError Tests
    // =====================================================

    @Test fun `NetworkError is singleton`() {
        val a = NetworkResult.NetworkError
        val b = NetworkResult.NetworkError
        assertSame(a, b)
    }

    @Test fun `NetworkError is not null`() {
        assertNotNull(NetworkResult.NetworkError)
    }

    // =====================================================
    // Type Check Tests
    // =====================================================

    @Test fun `Success is instance of NetworkResult`() {
        val result: NetworkResult<String> = NetworkResult.Success("data")
        assertTrue(result is NetworkResult.Success)
    }

    @Test fun `Error is instance of NetworkResult`() {
        val result: NetworkResult<String> = NetworkResult.Error(500, "err")
        assertTrue(result is NetworkResult.Error)
    }

    @Test fun `NetworkError is instance of NetworkResult`() {
        val result: NetworkResult<String> = NetworkResult.NetworkError
        assertTrue(result is NetworkResult.NetworkError)
    }

    @Test fun `Success is not Error`() {
        val result: NetworkResult<String> = NetworkResult.Success("data")
        assertFalse(result is NetworkResult.Error)
    }

    @Test fun `Error is not Success`() {
        val result: NetworkResult<String> = NetworkResult.Error(500, "err")
        assertFalse(result is NetworkResult.Success)
    }

    @Test fun `NetworkError is not Success`() {
        val result: NetworkResult<String> = NetworkResult.NetworkError
        assertFalse(result is NetworkResult.Success)
    }

    @Test fun `when expression covers all cases`() {
        val results = listOf<NetworkResult<String>>(
            NetworkResult.Success("ok"),
            NetworkResult.Error(500, "err"),
            NetworkResult.NetworkError
        )
        results.forEach { result ->
            val msg = when (result) {
                is NetworkResult.Success -> "success"
                is NetworkResult.Error -> "error"
                is NetworkResult.NetworkError -> "network"
            }
            assertTrue(msg.isNotEmpty())
        }
    }
}
