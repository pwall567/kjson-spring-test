/*
 * @(#) JSONMatcherTest.kt
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

package io.kjson.spring.test.matchers

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

import java.time.LocalDate

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.junit4.SpringRunner

import io.kjson.spring.JSONSpring
import io.kjson.spring.test.JSONMockMvc
import io.kjson.spring.test.TestConfiguration

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestConfiguration::class])
@ComponentScan(basePackageClasses = [JSONSpring::class])
class JSONMatcherTest {

    @Autowired lateinit var jsonMockMvc: JSONMockMvc

    @Test fun `should validate JSON output`() {
        jsonMockMvc.getForJSON("/testendpoint").andExpect {
            status { isOk() }
            contentMatchesJSON {
                property("date", LocalDate.of(2022, 7, 6))
                property("extra", "Hello!")
            }
        }
    }

    @Test fun `should fail on incorrect JSON output`() {
        assertFailsWith<AssertionError> {
            jsonMockMvc.getForJSON("/testendpoint").andExpect {
                status { isOk() }
                content {
                    matchesJSON {
                        property("date", LocalDate.of(2022, 7, 6))
                        property("extra", "Goodbye!")
                    }
                }
            }
        }.let {
            expect("/extra: JSON value doesn't match - expected \"Goodbye!\", was \"Hello!\"") { it.message }
        }
    }

}
