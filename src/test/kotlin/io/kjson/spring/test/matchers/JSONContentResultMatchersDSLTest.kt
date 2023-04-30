/*
 * @(#) JSONContentResultMatchersDSLTest.kt
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
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner

import io.kjson.spring.JSONSpring
import io.kjson.spring.test.JSONMockMvc
import io.kjson.spring.test.TestConfiguration

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestConfiguration::class])
@ComponentScan(basePackageClasses = [JSONSpring::class])
class JSONContentResultMatchersDSLTest {

    @Autowired lateinit var jsonMockMvc: JSONMockMvc

    @Test fun `should test content type`() {
        jsonMockMvc.getForJSON("/testendpoint").andExpect {
            status { isOk() }
            content {
                contentType("application/json;charset=UTF-8")
            }
        }
    }

    @Test fun `should throw error on incorrect content type`() {
        assertFailsWith<AssertionError> {
            jsonMockMvc.getForJSON("/testendpoint").andExpect {
                status { isOk() }
                content {
                    contentType("text/plain")
                }
            }
        }.let {
            expect("Content type expected:<text/plain> but was:<application/json;charset=UTF-8>") { it.message }
        }
    }

    @Test fun `should test content type using MediaType`() {
        jsonMockMvc.getForJSON("/testendpoint").andExpect {
            status { isOk() }
            content {
                contentType(MediaType("application", "json", mapOf("charset" to "UTF-8")))
            }
        }
    }

    @Test fun `should test content type compatible with`() {
        jsonMockMvc.getForJSON("/testendpoint").andExpect {
            status { isOk() }
            content {
                contentTypeCompatibleWith("application/json")
            }
        }
    }

    @Test fun `should throw error on incorrect content type compatible with`() {
        assertFailsWith<AssertionError> {
            jsonMockMvc.getForJSON("/testendpoint").andExpect {
                status { isOk() }
                content {
                    contentTypeCompatibleWith("image/jpeg")
                }
            }
        }.let {
            expect("Content type [application/json;charset=UTF-8] is not compatible with [image/jpeg]") { it.message }
        }
    }

    @Test fun `should test content type compatible with using MediaType`() {
        jsonMockMvc.getForJSON("/testendpoint").andExpect {
            status { isOk() }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            }
        }
    }

    @Test fun `should test encoding`() {
        jsonMockMvc.getForJSON("/testendpoint").andExpect {
            status { isOk() }
            content {
                encoding("UTF-8")
            }
        }
    }

    @Test fun `should test content as string`() {
        jsonMockMvc.getForJSON("/testendpoint").andExpect {
            status { isOk() }
            content {
                string(right)
            }
        }
    }

    @Test fun `should throw error on incorrect content as string`() {
        assertFailsWith<AssertionError> {
            jsonMockMvc.getForJSON("/testendpoint").andExpect {
                status { isOk() }
                content {
                    string(wrong)
                }
            }
        }.let {
            expect("""Response content expected:<$wrong> but was:<$right>""") {
                it.message
            }
        }
    }

    @Test fun `should test content as byte array`() {
        jsonMockMvc.getForJSON("/testendpoint").andExpect {
            status { isOk() }
            content {
                bytes(right.toByteArray())
            }
        }
    }

    @Test fun `should test content using matchesJSON`() {
        jsonMockMvc.get("/testendpoint").andExpect {
            status { isOk() }
            content {
                matchesJSON {
                    property("date", LocalDate.of(2022, 7, 6))
                    property("extra", "Hello!")
                }
            }
        }
    }

    @Test fun `should throw error on incorrect content using matchesJSON`() {
        assertFailsWith<AssertionError> {
            jsonMockMvc.get("/testendpoint").andExpect {
                status { isOk() }
                content {
                    matchesJSON {
                        property("date", LocalDate.of(2022, 7, 6))
                        property("extra", "Goodbye!")
                    }
                }
            }
        }.let {
            expect("""/extra: JSON value doesn't match - expected "Goodbye!", was "Hello!"""") { it.message }
        }
    }

    companion object {
        const val right = """{"date":"2022-07-06","extra":"Hello!"}"""
        const val wrong = """{"date":"2022-07-06","extra":"Goodbye!"}"""
    }

}
