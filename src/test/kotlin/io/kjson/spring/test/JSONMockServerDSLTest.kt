/*
 * @(#) JSONMockServerDSLTest.kt
 *
 * kjson-spring-test  Spring JSON testing functions for kjson
 * Copyright (c) 2022 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.kjson.spring.test

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

import java.time.LocalDate

import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

import io.kjson.spring.test.JSONMockServerDSL.Companion.mock

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestConfiguration::class])
class JSONMockServerDSLTest {

    @Test fun `should match simple mock request`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.reset()
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val response = restTemplate.getForObject<String>("/testendpoint")
        expect("""{"date":"2022-07-12","extra":"OK"}""") { response }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match simple mock request with incorrect method`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.reset()
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.POST)
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        assertFailsWith<AssertionError> { restTemplate.getForObject<String>("/testendpoint") }.let {
            expect("Request method incorrect; expected POST, was GET") { it.message }
        }
    }

    @Test fun `should match mock request with query param`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.reset()
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            queryParam("param1", "abc")
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val response = restTemplate.getForObject<String>("/testendpoint?param1=abc")
        expect("""{"date":"2022-07-12","extra":"OK"}""") { response }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match mock request with missing query param`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.reset()
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            queryParam("param1", "abc")
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        assertFailsWith<AssertionError> { restTemplate.getForObject<String>("/testendpoint") }.let {
            expect("Query param [param1] not found") { it.message }
        }
    }

    @Test fun `should fail to match mock request with incorrect query param`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.reset()
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            queryParam("param1", "abc")
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        assertFailsWith<AssertionError> { restTemplate.getForObject<String>("/testendpoint?param1=xyz") }.let {
            expect("Query param [param1] incorrect; expected abc, was xyz") { it.message }
        }
    }

}
