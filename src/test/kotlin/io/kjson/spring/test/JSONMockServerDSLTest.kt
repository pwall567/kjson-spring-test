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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.expect

import java.net.URI
import java.time.LocalDate
import java.util.UUID

import org.hamcrest.core.StringStartsWith
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.getForEntity
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject

import io.kjson.spring.test.JSONMockServerDSL.Companion.mock
import io.kjson.spring.test.JSONMockServerDSL.Companion.mockGet
import io.kjson.spring.test.JSONMockServerDSL.Companion.mockPost

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestConfiguration::class])
class JSONMockServerDSLTest {

    @Test fun `should match simple mock request`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
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

    @Test fun `should match simple mock request using mockGet`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mockGet(uri = URI("/testendpoint")).respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val response = restTemplate.getForObject<String>("/testendpoint")
        expect("""{"date":"2022-07-12","extra":"OK"}""") { response }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match simple mock request using mockGet with wrong method`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mockGet(uri = URI("/testendpoint")).respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        assertFailsWith<AssertionError> { restTemplate.postForObject<String>("/testendpoint") }.let {
            expect("Request method incorrect; expected GET, was POST") { it.message }
        }
    }

    @Test fun `should match simple mock request using mockPost`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mockPost(uri = URI("/testendpoint")).respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val response = restTemplate.postForObject<String>("/testendpoint")
        expect("""{"date":"2022-07-12","extra":"OK"}""") { response }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match simple mock request using mockPost with wrong method`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mockPost(uri = URI("/testendpoint")).respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        assertFailsWith<AssertionError> { restTemplate.getForObject<String>("/testendpoint") }.let {
            expect("Request method incorrect; expected POST, was GET") { it.message }
        }
    }

    @Test fun `should match simple mock request using matcher`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            @Suppress("deprecation")
            requestTo(StringStartsWith.startsWith("/testendpoint"))
            method(HttpMethod.GET)
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val response = restTemplate.getForObject<String>("/testendpoint?abc=123")
        expect("""{"date":"2022-07-12","extra":"OK"}""") { response }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match simple mock request using incorrect matcher`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            @Suppress("deprecation")
            requestTo(StringStartsWith.startsWith("/testpointend"))
            method(HttpMethod.GET)
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        assertFailsWith<AssertionError> { restTemplate.getForObject<String>("/testendpoint") }.let {
            it.message.let { m ->
                assertNotNull(m)
                assertTrue(m.contains("Request URI"))
                assertTrue(m.contains("/testpointend"))
                assertTrue(m.contains("/testendpoint"))
            }
        }
    }

    @Test fun `should match simple mock request using lambda`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo { it.startsWith("/testendpoint") }
            method(HttpMethod.GET)
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val response = restTemplate.getForObject<String>("/testendpoint?abc=123")
        expect("""{"date":"2022-07-12","extra":"OK"}""") { response }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match simple mock request using incorrect lambda`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo { it.startsWith("/testpointend") }
            method(HttpMethod.GET)
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        assertFailsWith<java.lang.AssertionError> { restTemplate.getForObject<String>("/testendpoint") }.let {
            expect("Request URI doesn't match; was /testendpoint") { it.message }
        }
    }

    @Test fun `should match mock request with query param`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
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
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            queryParam("param1", "abc")
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        assertFailsWith<AssertionError> { restTemplate.getForObject<String>("/testendpoint") }.let {
            expect("Request query param [param1] not found") { it.message }
        }
    }

    @Test fun `should fail to match mock request with incorrect query param`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            queryParam("param1", "abc")
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        assertFailsWith<AssertionError> { restTemplate.getForObject<String>("/testendpoint?param1=xyz") }.let {
            expect("Request query param [param1] incorrect; expected abc, was xyz") { it.message }
        }
    }

    @Test fun `should match mock request with header`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            header("X-Custom-1", "ABC")
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val requestEntity = RequestEntity.method(HttpMethod.GET, "/testendpoint").header("X-Custom-1", "ABC").build()
        val response = restTemplate.exchange<String>(requestEntity)
        expect("""{"date":"2022-07-12","extra":"OK"}""") { response.body }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match mock request with incorrect header`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            header("X-Custom-1", "ABC")
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val requestEntity = RequestEntity.method(HttpMethod.GET, "/testendpoint").header("X-Custom-1", "AAA").build()
        assertFailsWith<AssertionError> { restTemplate.exchange<String>(requestEntity) }.let {
            expect("Request header [X-Custom-1] incorrect; expected ABC, was AAA") { it.message }
        }
    }

    @Test fun `should match mock request with header using lambda`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            header("X-Custom-1") { it.startsWith("A") }
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val requestEntity = RequestEntity.method(HttpMethod.GET, "/testendpoint").header("X-Custom-1", "ABC").build()
        val response = restTemplate.exchange<String>(requestEntity)
        expect("""{"date":"2022-07-12","extra":"OK"}""") { response.body }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match mock request with incorrect header using lambda`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            header("X-Custom-1") { it.startsWith("A") }
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val requestEntity = RequestEntity.method(HttpMethod.GET, "/testendpoint").header("X-Custom-1", "BBB").build()
        assertFailsWith<AssertionError> { restTemplate.exchange<String>(requestEntity) }.let {
            expect("Request header [X-Custom-1] incorrect; was BBB") { it.message }
        }
    }

    @Test fun `should match mock request with Accept header`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            accept(MediaType.APPLICATION_JSON)
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val requestEntity =
                RequestEntity.method(HttpMethod.GET, "/testendpoint").accept(MediaType.APPLICATION_JSON).build()
        val response = restTemplate.exchange<String>(requestEntity)
        expect("""{"date":"2022-07-12","extra":"OK"}""") { response.body }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match mock request with incorrect Accept header`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            acceptApplicationJSON()
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val requestEntity = RequestEntity.method(HttpMethod.GET, "/testendpoint").accept(MediaType.TEXT_PLAIN).build()
        assertFailsWith<AssertionError> { restTemplate.exchange<String>(requestEntity) }.let {
            expect("Request [Accept] header incorrect; expected application/json, was text/plain") { it.message }
        }
    }

    @Test fun `should match mock request with Content-Type header`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.POST)
            contentType(MediaType.TEXT_PLAIN)
            accept(MediaType.APPLICATION_JSON)
            requestContent("DATA!")
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 21), extra = "OK")
        }
        val headers = HttpHeaders().apply {
            contentType = MediaType.TEXT_PLAIN
        }
        val requestEntity = RequestEntity("DATA!", headers, HttpMethod.POST, URI("/testendpoint"))
        val response = restTemplate.exchange<String>(requestEntity)
        expect("""{"date":"2022-07-21","extra":"OK"}""") { response.body }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match mock request with incorrect Content-Type header`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.POST)
            contentTypeApplicationJSON()
            accept(MediaType.APPLICATION_JSON)
            requestContent("DATA!")
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 21), extra = "OK")
        }
        val headers = HttpHeaders().apply {
            contentType = MediaType.TEXT_PLAIN
        }
        val requestEntity = RequestEntity("DATA!", headers, HttpMethod.POST, URI("/testendpoint"))
        assertFailsWith<java.lang.AssertionError> { restTemplate.exchange<String>(requestEntity) }.let {
            expect("Request [Content-Type] header incorrect; expected application/json, was text/plain") { it.message }
        }
    }

    @Test fun `should match mock request using lambda to match content`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.POST)
            contentType(MediaType.TEXT_PLAIN)
            accept(MediaType.APPLICATION_JSON)
            requestContent { it.startsWith('D') }
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 21), extra = "OK")
        }
        val headers = HttpHeaders().apply {
            contentType = MediaType.TEXT_PLAIN
        }
        val requestEntity = RequestEntity("DATA!", headers, HttpMethod.POST, URI("/testendpoint"))
        val response = restTemplate.exchange<String>(requestEntity)
        expect("""{"date":"2022-07-21","extra":"OK"}""") { response.body }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match mock request using lambda to match content`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.POST)
            contentType(MediaType.TEXT_PLAIN)
            accept(MediaType.APPLICATION_JSON)
            requestContent { it.startsWith('d') }
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 21), extra = "OK")
        }
        val headers = HttpHeaders().apply {
            contentType = MediaType.TEXT_PLAIN
        }
        val requestEntity = RequestEntity("DATA!", headers, HttpMethod.POST, URI("/testendpoint"))
        assertFailsWith<AssertionError> { restTemplate.exchange<String>(requestEntity) }.let {
            expect("Request body incorrect") { it.message }
        }
    }

    @Test fun `should match mock request with JSON content`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mockPost {
            requestTo("/testendpoint")
            contentType(MediaType.APPLICATION_JSON)
            accept(MediaType.APPLICATION_JSON)
            requestJSON {
                property("id", testUUID)
                property("name", "Mary")
            }
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 21), extra = "OK")
        }
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val requestData = RequestData(id =  testUUID, name = "Mary")
        val requestEntity = RequestEntity(requestData, headers, HttpMethod.POST, URI("/testendpoint"))
        val response = restTemplate.exchange<String>(requestEntity)
        expect("""{"date":"2022-07-21","extra":"OK"}""") { response.body }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match mock request with incorrect JSON content`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mockPost {
            requestTo("/testendpoint")
            contentType(MediaType.APPLICATION_JSON)
            accept(MediaType.APPLICATION_JSON)
            requestJSON {
                property("id", testUUID)
                property("name", "Mary")
            }
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 21), extra = "OK")
        }
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val requestData = RequestData(id =  testUUID, name = "Maria")
        val requestEntity = RequestEntity(requestData, headers, HttpMethod.POST, URI("/testendpoint"))
        assertFailsWith<AssertionError> { restTemplate.exchange<String>(requestEntity) }.let {
            expect("""/name: JSON value doesn't match - expected "Mary", was "Maria"""") { it.message }
        }
    }

    @Test fun `should match mock request with absent header`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mockGet {
            requestTo("/testendpoint")
            headerDoesNotExist("X-Test-1")
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val response = restTemplate.getForObject<String>("/testendpoint")
        expect("""{"date":"2022-07-12","extra":"OK"}""") { response }
        mockRestServiceServer.verify()
    }

    @Test fun `should fail to match mock request when unexpected header present`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mockGet {
            requestTo("/testendpoint")
            headerDoesNotExist("X-Test-1")
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "OK")
        }
        val headers = HttpHeaders().apply {
            set("X-Test-1", "Shouldn't be here")
        }
        val requestEntity = RequestEntity<Unit>(headers, HttpMethod.GET, URI("/testendpoint"))
        assertFailsWith<java.lang.AssertionError> { restTemplate.exchange<String>(requestEntity) }.let {
            expect("Request [X-Test-1] header expected not to be present") { it.message }
        }
    }

    @Test fun `should match simple mock request and respond using new syntax`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            respondJSON(result = ResponseData(date = LocalDate.of(2022, 7, 12), extra = "XXX"))
        }
        val response = restTemplate.getForObject<String>("/testendpoint")
        expect("""{"date":"2022-07-12","extra":"XXX"}""") { response }
        mockRestServiceServer.verify()
    }

    @Test fun `should match simple mock request and respond using new syntax with lambda`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo { it.startsWith("/testendpoint/") }
            method(HttpMethod.GET)
            respondJSON {
                ResponseData(date = LocalDate.of(2022, 7, 12), extra = uri.path.substringAfterLast('/'))
            }
        }
        val response = restTemplate.getForObject<String>("/testendpoint/works")
        expect("""{"date":"2022-07-12","extra":"works"}""") { response }
        mockRestServiceServer.verify()
    }

    @Test fun `should match simple mock request and respond using new syntax with string`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            respond(result = "OK!")
        }
        val response = restTemplate.getForObject<String>("/testendpoint")
        expect("OK!") { response }
        mockRestServiceServer.verify()
    }

    @Test fun `should match simple mock request and respond using new syntax with status only`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpoint")
            method(HttpMethod.GET)
            respond(HttpStatus.CREATED)
        }
        val response = restTemplate.getForEntity<Unit>("/testendpoint")
        expect(HttpStatus.CREATED) { response.statusCode }
        mockRestServiceServer.verify()
    }

    @Test fun `should match multiple requests in sequence`() {
        val restTemplate = RestTemplate()
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
        mockRestServiceServer.mock {
            requestTo("/testendpointA")
            method(HttpMethod.GET)
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "AAAA")
        }
        mockRestServiceServer.mock {
            requestTo("/testendpointB")
            method(HttpMethod.GET)
        }.respondJSON {
            ResponseData(date = LocalDate.of(2022, 7, 12), extra = "BBBB")
        }
        expect("""{"date":"2022-07-12","extra":"AAAA"}""") { restTemplate.getForObject("/testendpointA") }
        expect("""{"date":"2022-07-12","extra":"BBBB"}""") { restTemplate.getForObject("/testendpointB") }
        mockRestServiceServer.verify()
    }

    companion object {
        val testUUID: UUID = UUID.fromString("49c998d4-10da-11ed-886d-0fd84b46e61e")
    }

}
