/*
 * @(#) JSONMockMvc.kt
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

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

/**
 * Make a GET call to a [MockMvc] with the nominated URI, setting the `Accept` header to `application/json` to indicate
 * that the expected response is JSON.  Further details may be added to the request with the optional configuration
 * lambda.
 */
fun MockMvc.getForJSON(uri: URI, block: MockHttpServletRequestDsl.() -> Unit = {}) =
    get(uri) {
        accept(MediaType.APPLICATION_JSON)
        block()
    }

/**
 * Make a GET call to a [MockMvc] with the nominated URL (created from a URL template string and an optional set of
 * variables), setting the `Accept` header to `application/json` to indicate that the expected response is JSON.
 * Further details may be added to the request with the optional configuration lambda.
 */
fun MockMvc.getForJSON(urlTemplate: String, vararg vars: Any?, block: MockHttpServletRequestDsl.() -> Unit = {}) =
    get(urlTemplate, *vars) {
        accept(MediaType.APPLICATION_JSON)
        block()
    }

/**
 * Make a POST call to a [MockMvc] with the nominated URI, setting the `Accept` header to `application/json` to indicate
 * that the expected response is JSON.  Further details may be added to the request with the optional configuration
 * lambda.
 */
fun MockMvc.postForJSON(uri: URI, block: MockHttpServletRequestDsl.() -> Unit = {}) =
    post(uri) {
        accept(MediaType.APPLICATION_JSON)
        block()
    }

/**
 * Make a POST call to a [MockMvc] with the nominated URL (created from a URL template string and an optional set of
 * variables), setting the `Accept` header to `application/json` to indicate that the expected response is JSON.
 * Further details may be added to the request with the optional configuration lambda.
 */
fun MockMvc.postForJSON(urlTemplate: String, vararg vars: Any?, block: MockHttpServletRequestDsl.() -> Unit = {}) =
    post(urlTemplate, *vars) {
        accept(MediaType.APPLICATION_JSON)
        block()
    }
