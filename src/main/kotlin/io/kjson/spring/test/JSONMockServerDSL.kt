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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.ResponseActions
import org.springframework.test.web.client.match.MockRestRequestMatchers

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
        MockRestRequestMatchers.requestTo(expectedURI).match(request)
    }

    fun requestTo(expectedURI: URI) {
        MockRestRequestMatchers.requestTo(expectedURI).match(request)
    }

    fun requestTo(matcher: Matcher<in String>) {
        MockRestRequestMatchers.requestTo(matcher).match(request)
    }

    fun method(method: HttpMethod) {
        MockRestRequestMatchers.method(method).match(request)
    }

    fun queryParam(name: String, vararg expectedValues: String) {
        MockRestRequestMatchers.queryParam(name, *expectedValues).match(request)
    }

    fun header(name: String, vararg expectedValues: String) {
        MockRestRequestMatchers.header(name, *expectedValues).match(request)
    }

    fun header(name: String, vararg matchers: Matcher<in String?>) {
        MockRestRequestMatchers.header(name, *matchers).match(request)
    }

    fun acceptApplicationJSON() {
        MockRestRequestMatchers.header(HttpHeaders.ACCEPT, mediaTypeMatcher).match(request)
    }

    fun headerDoesNotExist(name: String) {
        MockRestRequestMatchers.headerDoesNotExist(name).match(request)
    }

    fun requestJSON(tests: JSONExpect.() -> Unit) {
        MockRestRequestMatchers.header(HttpHeaders.CONTENT_TYPE, mediaTypeMatcher).match(request)
        expectJSON(request.bodyAsString, tests)
    }

    companion object {

        val mediaTypeMatcher = MediaTypeMatcher(MediaType.APPLICATION_JSON)

        fun MockRestServiceServer.mock(
            expectedCount: ExpectedCount = ExpectedCount.once(),
            method: HttpMethod? = null,
            uri: URI? = null,
            block: JSONMockServerDSL.() -> Unit
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
            block: JSONMockServerDSL.() -> Unit
        ) = mock(expectedCount, HttpMethod.GET, uri, block)

        fun MockRestServiceServer.mockPost(
            expectedCount: ExpectedCount = ExpectedCount.once(),
            uri: URI? = null,
            block: JSONMockServerDSL.() -> Unit
        ) = mock(expectedCount, HttpMethod.POST, uri, block)

    }

}
