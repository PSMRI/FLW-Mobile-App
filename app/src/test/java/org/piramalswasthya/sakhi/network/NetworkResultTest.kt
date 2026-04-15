package org.piramalswasthya.sakhi.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkResultTest {

    // =====================================================
    // Success Tests
    // =====================================================

    @Test fun `Success creation with string data`() {
        val result = NetworkResult.Success("data")
        assertEquals("data", result.data)
    }

    @Test fun `Success creation with integer data`() {
        val result = NetworkResult.Success(42)
        assertEquals(42, result.data)
    }

    @Test fun `Success is NetworkResult`() {
        val result: NetworkResult<String> = NetworkResult.Success("data")
        assertTrue(result is NetworkResult.Success)
    }

    @Test fun `Success equals for same data`() {
        val a = NetworkResult.Success("data")
        val b = NetworkResult.Success("data")
        assertEquals(a, b)
    }

    @Test fun `Success not equals for different data`() {
        val a = NetworkResult.Success("data1")
        val b = NetworkResult.Success("data2")
        assertFalse(a == b)
    }

    @Test fun `Success copy changes data`() {
        val result = NetworkResult.Success("old")
        val copy = result.copy(data = "new")
        assertEquals("new", copy.data)
    }

    // =====================================================
    // Error Tests
    // =====================================================

    @Test fun `Error creation`() {
        val result = NetworkResult.Error(404, "Not Found")
        assertEquals(404, result.code)
        assertEquals("Not Found", result.message)
    }

    @Test fun `Error is NetworkResult`() {
        val result: NetworkResult<String> = NetworkResult.Error(500, "Server Error")
        assertTrue(result is NetworkResult.Error)
    }

    @Test fun `Error equals for same data`() {
        val a = NetworkResult.Error(404, "Not Found")
        val b = NetworkResult.Error(404, "Not Found")
        assertEquals(a, b)
    }

    @Test fun `Error not equals for different code`() {
        val a = NetworkResult.Error(404, "Not Found")
        val b = NetworkResult.Error(500, "Server Error")
        assertFalse(a == b)
    }

    @Test fun `Error copy changes code`() {
        val result = NetworkResult.Error(404, "Not Found")
        val copy = result.copy(code = 500)
        assertEquals(500, copy.code)
    }

    @Test fun `Error with code 0`() {
        val result = NetworkResult.Error(0, "Custom error")
        assertEquals(0, result.code)
    }

    // =====================================================
    // NetworkError Tests
    // =====================================================

    @Test fun `NetworkError is singleton`() {
        val a = NetworkResult.NetworkError
        val b = NetworkResult.NetworkError
        assertTrue(a === b)
    }

    @Test fun `NetworkError is NetworkResult`() {
        val result: NetworkResult<String> = NetworkResult.NetworkError
        assertTrue(result is NetworkResult.NetworkError)
    }

    @Test fun `NetworkError is not Success`() {
        val result: NetworkResult<String> = NetworkResult.NetworkError
        assertFalse(result is NetworkResult.Success)
    }

    @Test fun `NetworkError is not Error`() {
        val result: NetworkResult<String> = NetworkResult.NetworkError
        assertFalse(result is NetworkResult.Error)
    }

    // =====================================================
    // When Pattern Tests
    // =====================================================

    @Test fun `when matches Success`() {
        val result: NetworkResult<String> = NetworkResult.Success("data")
        val matched = when (result) {
            is NetworkResult.Success -> "success"
            is NetworkResult.Error -> "error"
            is NetworkResult.NetworkError -> "network_error"
        }
        assertEquals("success", matched)
    }

    @Test fun `when matches Error`() {
        val result: NetworkResult<String> = NetworkResult.Error(500, "Error")
        val matched = when (result) {
            is NetworkResult.Success -> "success"
            is NetworkResult.Error -> "error"
            is NetworkResult.NetworkError -> "network_error"
        }
        assertEquals("error", matched)
    }

    @Test fun `when matches NetworkError`() {
        val result: NetworkResult<String> = NetworkResult.NetworkError
        val matched = when (result) {
            is NetworkResult.Success -> "success"
            is NetworkResult.Error -> "error"
            is NetworkResult.NetworkError -> "network_error"
        }
        assertEquals("network_error", matched)
    }
}
