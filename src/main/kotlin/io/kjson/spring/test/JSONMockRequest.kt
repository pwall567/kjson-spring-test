/*
 * @(#) JSONMockRequest.kt
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

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockHttpServletRequestDsl

import io.kjson.stringifyJSON

/**
 * Add JSON POST content to a `MockMvc` request.
 */
fun MockHttpServletRequestDsl.contentJSON(data: Any?) {
    contentType = MediaType.APPLICATION_JSON
    content = data.stringifyJSON(JSONTestConfig.config)
}

/**
 * Add JSON POST content to a `MockMvc` request, invoking a lambda to create to POST data.
 */
fun MockHttpServletRequestDsl.contentJSON(lambda: () -> Any?) {
    contentJSON(lambda())
}
