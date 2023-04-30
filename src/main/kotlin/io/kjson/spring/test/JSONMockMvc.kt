/*
 * @(#) JSONMockMvc.kt
 *
 * kjson-spring-test  Spring JSON testing functions for kjson
 * Copyright (c) 2022, 2023 Peter Wall
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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultHandler
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import io.kjson.JSONConfig
import io.kjson.spring.test.matchers.JSONMockMvcResultMatchersDSL

/**
 * Replacement for [MockMvc] allowing the use of `json-spring-test` functions.
 *
 * @author  Peter Wall
 */
@Component
class JSONMockMvc(
    @Autowired(required = false) autowiredConfig: JSONConfig?,
    @Autowired val webApplicationContext: WebApplicationContext,
) {

    private val config: JSONConfig = autowiredConfig ?: JSONConfig.defaultConfig

    private val mockMvc: MockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

    /**
     * Perform a request on the underlying [MockMvc].  This allows the `JSONMockMvc` to be used on a similar manner to a
     * [MockMvc], for those users who are more familiar with that API.
     *
     * Note, however, that the [JSONResultActions] returned by this function is similar, but not identical, to the
     * [ResultActions] returned by the original function.
     */
    fun perform(requestBuilder: RequestBuilder): JSONResultActions {
        return JSONResultActions(mockMvc.perform(requestBuilder).andReturn())
    }

    /**
     * Perform a GET request, providing access to [JSONMockHttpServletRequestDSL] Kotlin DSL.
     */
    fun get(
        urlTemplate: String,
        vararg uriVars: Any?,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.get(urlTemplate, uriVars)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply(dsl).perform(mockMvc)
    }

    /**
     * Perform a GET request, providing access to [JSONMockHttpServletRequestDSL] Kotlin DSL.
     */
    fun get(
        uri: URI,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.get(uri)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply(dsl).perform(mockMvc)
    }

    /**
     * Make a GET call to a [MockMvc] with the nominated URL (created from a URL template string and an optional set of
     * variables), setting the `Accept` header to `application/json` to indicate that the expected response is JSON.
     * Further details may be added to the request with the optional configuration lambda.
     */
    fun getForJSON(
        urlTemplate: String,
        vararg uriVars: Any?,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.get(urlTemplate, uriVars)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply {
            accept(MediaType.APPLICATION_JSON)
        }.apply(dsl).perform(mockMvc)
    }

    /**
     * Make a GET call to a [MockMvc] with the nominated URI, setting the `Accept` header to `application/json` to
     * indicate that the expected response is JSON.  Further details may be added to the request with the optional
     * configuration lambda.
     */
    fun getForJSON(
        uri: URI,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.get(uri)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply {
            accept(MediaType.APPLICATION_JSON)
        }.apply(dsl).perform(mockMvc)
    }

    /**
     * Perform a POST request, providing access to [JSONMockHttpServletRequestDSL] Kotlin DSL.
     */
    fun post(
        urlTemplate: String,
        vararg uriVars: Any?,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.post(urlTemplate, uriVars)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply(dsl).perform(mockMvc)
    }

    /**
     * Perform a POST request, providing access to [JSONMockHttpServletRequestDSL] Kotlin DSL.
     */
    fun post(
        uri: URI,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.post(uri)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply(dsl).perform(mockMvc)
    }

    /**
     * Make a POST call to a [MockMvc] with the nominated URL (created from a URL template string and an optional set of
     * variables), setting the `Accept` header to `application/json` to indicate that the expected response is JSON.
     * Further details may be added to the request with the optional configuration lambda.
     */
    fun postForJSON(
        urlTemplate: String,
        vararg uriVars: Any?,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.post(urlTemplate, uriVars)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply {
            accept(MediaType.APPLICATION_JSON)
        }.apply(dsl).perform(mockMvc)
    }

    /**
     * Make a POST call to a [MockMvc] with the nominated URI, setting the `Accept` header to `application/json` to
     * indicate that the expected response is JSON.  Further details may be added to the request with the optional
     * configuration lambda.
     */
    fun postForJSON(
        uri: URI,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.post(uri)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply {
            accept(MediaType.APPLICATION_JSON)
        }.apply(dsl).perform(mockMvc)
    }

    /**
     * Perform a PUT request, providing access to [JSONMockHttpServletRequestDSL] Kotlin DSL.
     */
    fun put(
        urlTemplate: String,
        vararg uriVars: Any?,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.put(urlTemplate, uriVars)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply(dsl).perform(mockMvc)
    }

    /**
     * Perform a PUT request, providing access to [JSONMockHttpServletRequestDSL] Kotlin DSL.
     */
    fun put(
        uri: URI,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.put(uri)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply(dsl).perform(mockMvc)
    }

    /**
     * Make a PUT call to a [MockMvc] with the nominated URL (created from a URL template string and an optional set of
     * variables), setting the `Accept` header to `application/json` to indicate that the expected response is JSON.
     * Further details may be added to the request with the optional configuration lambda.
     */
    fun putForJSON(
        urlTemplate: String,
        vararg uriVars: Any?,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.put(urlTemplate, uriVars)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply {
            accept(MediaType.APPLICATION_JSON)
        }.apply(dsl).perform(mockMvc)
    }

    /**
     * Make a PUT call to a [MockMvc] with the nominated URI, setting the `Accept` header to `application/json` to
     * indicate that the expected response is JSON.  Further details may be added to the request with the optional
     * configuration lambda.
     */
    fun putForJSON(
        uri: URI,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.put(uri)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply {
            accept(MediaType.APPLICATION_JSON)
        }.apply(dsl).perform(mockMvc)
    }

    /**
     * Perform a PATCH request, providing access to [JSONMockHttpServletRequestDSL] Kotlin DSL.
     */
    fun patch(
        urlTemplate: String,
        vararg uriVars: Any?,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.patch(urlTemplate, uriVars)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply(dsl).perform(mockMvc)
    }

    /**
     * Perform a PATCH request, providing access to [JSONMockHttpServletRequestDSL] Kotlin DSL.
     */
    fun patch(
        uri: URI,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.patch(uri)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply(dsl).perform(mockMvc)
    }

    /**
     * Make a PATCH call to a [MockMvc] with the nominated URL (created from a URL template string and an optional set
     * of variables), setting the `Accept` header to `application/json` to indicate that the expected response is JSON.
     * Further details may be added to the request with the optional configuration lambda.
     */
    fun patchForJSON(
        urlTemplate: String,
        vararg uriVars: Any?,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.patch(urlTemplate, uriVars)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply {
            accept(MediaType.APPLICATION_JSON)
        }.apply(dsl).perform(mockMvc)
    }

    /**
     * Make a PATCH call to a [MockMvc] with the nominated URI, setting the `Accept` header to `application/json` to
     * indicate that the expected response is JSON.  Further details may be added to the request with the optional
     * configuration lambda.
     */
    fun patchForJSON(
        uri: URI,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.patch(uri)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply {
            accept(MediaType.APPLICATION_JSON)
        }.apply(dsl).perform(mockMvc)
    }

    /**
     * Perform a DELETE request, providing access to [JSONMockHttpServletRequestDSL] Kotlin DSL.
     */
    fun delete(
        urlTemplate: String,
        vararg uriVars: Any?,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.delete(urlTemplate, uriVars)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply(dsl).perform(mockMvc)
    }

    /**
     * Perform a DELETE request, providing access to [JSONMockHttpServletRequestDSL] Kotlin DSL.
     */
    fun delete(
        uri: URI,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.delete(uri)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply(dsl).perform(mockMvc)
    }

    /**
     * Make a DELETE call to a [MockMvc] with the nominated URL (created from a URL template string and an optional set
     * of variables), setting the `Accept` header to `application/json` to indicate that the expected response is JSON.
     * Further details may be added to the request with the optional configuration lambda.
     */
    fun deleteForJSON(
        urlTemplate: String,
        vararg uriVars: Any?,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.delete(urlTemplate, uriVars)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply {
            accept(MediaType.APPLICATION_JSON)
        }.apply(dsl).perform(mockMvc)
    }

    /**
     * Make a DELETE call to a [MockMvc] with the nominated URI, setting the `Accept` header to `application/json` to
     * indicate that the expected response is JSON.  Further details may be added to the request with the optional
     * configuration lambda.
     */
    fun deleteForJSON(
        uri: URI,
        dsl: JSONMockHttpServletRequestDSL.() -> Unit = {}
    ): JSONResultActionsDSL {
        val requestBuilder: MockHttpServletRequestBuilder = MockMvcRequestBuilders.delete(uri)
        return JSONMockHttpServletRequestDSL(requestBuilder, config).apply {
            accept(MediaType.APPLICATION_JSON)
        }.apply(dsl).perform(mockMvc)
    }

    class JSONResultActions(val mvcResult: MvcResult) : ResultActions {

        override fun andExpect(matcher: ResultMatcher): JSONResultActions {
            matcher.match(mvcResult)
            return this
        }

        override fun andDo(handler: ResultHandler): JSONResultActions {
            handler.handle(mvcResult)
            return this
        }

        override fun andReturn(): MvcResult = mvcResult

    }

    class JSONResultActionsDSL(val resultActions: ResultActions) {

        val jsonResultActions = JSONResultActions(resultActions.andReturn())

        fun andExpect(block: JSONMockMvcResultMatchersDSL.() -> Unit): JSONResultActionsDSL {
            JSONMockMvcResultMatchersDSL(jsonResultActions).block()
            return this
        }

        fun andReturn(): MvcResult = resultActions.andReturn()

    }

}
