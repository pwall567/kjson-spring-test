/*
 * @(#) JSONMockServerDSL.kt
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

import java.net.URI

import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.ResponseActions

import io.kjson.test.JSONExpect
import io.kjson.test.JSONExpect.Companion.expectJSON

/**
 * A DSL class to assist with setting up [MockRestServiceServer] configurations.  Instances of this class are created by
 * the `mock`, `mockGet` or `mockPost` extension functions on [MockRestServiceServer].
 *
 * @author  Peter Wall
 */
class JSONMockServerDSL private constructor(private val request: MockClientHttpRequest) {

    fun requestTo(expectedURI: String) {
        requestTo(URI(expectedURI))
    }

    fun requestTo(expectedURI: URI) {
        val uri = request.uri
        if (!uri.equalIgnoringQuery(expectedURI))
            fail("Request URI doesn't match; expected $expectedURI, was $uri")
    }

    fun requestTo(test: (String) -> Boolean) {
        if (!test(request.uri.toString()))
            fail("Request URI doesn't match; was ${request.uri}")
    }

    @Deprecated("The use of Matcher will be removed in a future version", ReplaceWith("requestTo { test(it) }"))
    fun requestTo(matcher: Matcher<in String>) {
        MatcherAssert.assertThat("Request URI", request.uri.toString(), matcher)
    }

    fun method(method: HttpMethod) {
        if (request.method != method)
            fail("Request method incorrect; expected $method, was ${request.method}")
    }

    fun queryParam(name: String, vararg expectedValues: String) {
        val queryEntries = request.uri.query?.split('&')?.filter { it.startsWith("$name=") }
        if (queryEntries == null || queryEntries.isEmpty())
            throw AssertionError("Request query param [$name] not found")
        val n = expectedValues.size
        if (queryEntries.size != n)
            fail("Request query param [$name] number incorrect; expected $n, was ${queryEntries.size}")
        for (i in 0 until n) {
            val queryValue = queryEntries[i].substringAfter('=')
            if (expectedValues[i] != queryValue)
                fail("Request query param [$name] incorrect; expected ${expectedValues[i]}, was $queryValue")
        }
    }

    fun header(name: String, vararg expectedValues: String) {
        val n = expectedValues.size
        val headers = getHeaders(name, n)
        for (i in 0 until n)
            if (expectedValues[i] != headers[i])
                fail("Request header [$name] incorrect; expected ${expectedValues[i]}, was ${headers[i]}")
    }

    fun header(name: String, test: (String) -> Boolean) {
        val header = getHeaders(name, 1).first()
        if (!test(header))
            fail("Request header [$name] incorrect; was $header")
    }

    @Deprecated("The use of Matcher will be removed in a future version", ReplaceWith("header(name) { test(it) }"))
    fun header(name: String, vararg matchers: Matcher<in String?>) {
        val n = matchers.size
        val headers = getHeaders(name, n)
        for (i in 0 until n)
            MatcherAssert.assertThat("Request header [$name]", headers[i], matchers[i])
    }

    fun accept(expectedMediaType: MediaType) {
        val header = getHeaders(HttpHeaders.ACCEPT, 1).first()
        header.split(',').map { it.trim() }.forEach {
            if (checkMediaType(it, HttpHeaders.ACCEPT).isCompatibleWith(expectedMediaType))
                return
        }
        fail("Request [Accept] header incorrect; expected $expectedMediaType, was $header")
    }

    fun acceptApplicationJSON() {
        accept(MediaType.APPLICATION_JSON)
    }

    fun contentType(expectedMediaType: MediaType) {
        val header = getHeaders(HttpHeaders.CONTENT_TYPE, 1).first()
        if (!checkMediaType(header, HttpHeaders.CONTENT_TYPE).isCompatibleWith(expectedMediaType))
            fail("Request [Content-Type] header incorrect; expected $expectedMediaType, was $header")
    }

    fun contentTypeApplicationJSON() {
        contentType(MediaType.APPLICATION_JSON)
    }

    private fun getHeaders(name: String, expectedCount: Int): List<String> {
        val headers = request.headers[name] ?: throw AssertionError("Header [$name] not found")
        if (headers.size != expectedCount) {
            if (expectedCount == 1)
                fail("Request [$name] header; expected single header, was multiple (${headers.size})")
            else
                fail("Request [$name] header number incorrect; expected $expectedCount, was ${headers.size}")
        }
        return headers
    }

    fun headerDoesNotExist(name: String) {
        if (request.headers.containsKey(name))
            fail("Request [$name] header expected not to be present")
    }

    fun requestContent(body: String) {
        if (request.bodyAsString != body)
            fail("Request body incorrect")
    }

    fun requestContent(test: (String) -> Boolean) {
        if (!test(request.bodyAsString))
            fail("Request body incorrect")
    }

    fun requestJSON(tests: JSONExpect.() -> Unit) {
        contentTypeApplicationJSON()
        expectJSON(request.bodyAsString, tests)
    }

    companion object {

        fun fail(message: String): Nothing {
            throw AssertionError(message)
        }

        fun checkMediaType(header: String, name: String): MediaType = try {
            MediaType.parseMediaType(header)
        } catch (_: Exception) {
            fail("Request [$name] header media type invalid: $header")
        }

        fun URI.equalIgnoringQuery(other: URI): Boolean = if (isOpaque)
                other.isOpaque && schemeSpecificPart == other.schemeSpecificPart
            else
                !other.isOpaque && scheme == other.scheme && userInfo == other.userInfo && host == other.host &&
                        port == other.port && path == other.path

        fun MockRestServiceServer.mock(
            expectedCount: ExpectedCount = ExpectedCount.once(),
            method: HttpMethod? = null,
            uri: URI? = null,
            block: JSONMockServerDSL.() -> Unit = {}
        ): ResponseActions {
            return expect(expectedCount) { request ->
                JSONMockServerDSL(request as MockClientHttpRequest).apply {
                    method?.let { method(it) }
                    uri?.let { requestTo(it) }
                    block()
                }
            }
        }

        fun MockRestServiceServer.mockGet(
            expectedCount: ExpectedCount = ExpectedCount.once(),
            uri: URI? = null,
            block: JSONMockServerDSL.() -> Unit = {}
        ) = mock(expectedCount, HttpMethod.GET, uri, block)

        fun MockRestServiceServer.mockPost(
            expectedCount: ExpectedCount = ExpectedCount.once(),
            uri: URI? = null,
            block: JSONMockServerDSL.() -> Unit = {}
        ) = mock(expectedCount, HttpMethod.POST, uri, block)

    }

}
