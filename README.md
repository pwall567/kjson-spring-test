# kjson-spring-test

[![Build Status](https://travis-ci.com/pwall567/kjson-spring-test.svg?branch=main)](https://travis-ci.com/github/pwall567/kjson-spring-test)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/static/v1?label=Kotlin&message=v1.6.10&color=7f52ff&logo=kotlin&logoColor=7f52ff)](https://github.com/JetBrains/kotlin/releases/tag/v1.6.10)
[![Maven Central](https://img.shields.io/maven-central/v/io.kjson/kjson-spring-test?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.kjson%22%20AND%20a:%22kjson-spring-test%22)

Spring JSON testing functions for kjson

## Background

The [Spring Framework](https://spring.io/projects/spring-framework) provides a number of classes to assist with testing;
the `kjson-spring-test` library adds functionality to simplify the use of these tests in conjunction with the
[`kjson`](https://github.com/pwall567/kjson) library.

## Incoming Requests and `MockMvc`

The testing of incoming REST requests involves setting up incoming calls, and then testing the result to confirm that
it matches the expected status, data _etc._

The `kjson-spring-test` library can help with both of these aspects of incoming request testing, including the use of
the `kjson-test` library for testing / matching the response.

### `getForJSON`, `postForJSON`

The Kotlin extensions for Spring have added `get`, `post` functions _etc._, each of which takes a lambda using a DSL to
specify the operation.
The `kjson-spring-test` library adds `getForJSON` and `postForJSON`, which set the `Accept` header to `application/json`
to indicate that the expected response is JSON.

For example:
```kotlin
        mockMvc.getForJSON("/testendpoint") {
            header("X-Custom-Header", "value")
        }.andExpect {
            // check response
        }
```

The functions take the same DSL as the existing `get` and `post` functions, as shown in the example above, which uses
the `header` function from that DSL.

### `contentJSON`

To set the JSON content of a `MockMvc` POST using `kjson` serialization, the `contentJSON` function provides a simple
mechanism:
```kotlin
        mockMvc.postForJSON("/testendpoint") {
            contentJSON {
                RequestData(
                    id = customerId,
                    name = customerName,
                )
            }
        }.andExpect {
            // check response
        }
```
There are two forms of the function, one which takes an object to be serialised and another (shown above) that takes a
lambda which will be invoked to create the object.

The `kjson` serialization will use the `JSONConfig` configuration as described [below](#configuration).

### `matchesJSON`, `contentMatchesJSON`

The results of a `MockMvc` call may be tested using the [`kjson-test`](https://github.com/pwall567/kjson-test) library.

The Spring Kotlin extensions include the `content` function, which allows the specification of tests against the content
of the result.
The `kjson-spring-test` library adds the `matchesJSON` function within the content DSL;
this function parses the result as JSON, and then executes the `kjson-test` test specifications against the parsed
result.
For example:
```kotlin
        mockMvc.getForJSON("/testendpoint") {
            header("X-Custom-Header", "value")
        }.andExpect {
            status { isOk() }
            content {
                matchesJSON {
                    property("date", LocalDate.of(2022, 7, 6))
                    property("extra", "ResultValue")
                }
            }
        }
```

Alternatively, if the only function inside `content` is `matchesJSON`, the two may be combined:
```kotlin
        mockMvc.getForJSON("/testendpoint") {
            header("X-Custom-Header", "value")
        }.andExpect {
            status { isOk() }
            contentMatchesJSON {
                property("date", LocalDate.of(2022, 7, 6))
                property("extra", "ResultValue")
            }
        }
```

See the documentation for [`kjson-test`](https://github.com/pwall567/kjson-test) for more details on the matching
capabilities available using that library.

## Outgoing Requests and `MockRestServiceServer`

The testing of outgoing client REST requests is in some ways the reverse of incoming request testing.
The mock server is configured to match one or more possible requests, and to respond with the appropriate data.

Where the input request testing allows the use of the `kjson-test` library to test the result data, client request
testing uses the same library for matching the requests.

### `mock`, `mockGet`, `mockPost`

The Spring class `MockRestServiceServer` provides facilities for testing REST clients, but the conventional use of this
class involves a chain of "fluent" function calls., using `MockRestRequestMatchers` static functions.
The `kjson-spring-test` library provides a DSL-based approach to client testing.

The `mock` extension function for `MockRestServiceServer` allows mock requests to be declared in a Kotlin idiomatic
manner:
```kotlin
    mockRestServiceServer.mock {
        requestTo("/endpoint")
        method(HttpMethod.GET)
        header("X-Custom-Header", "value")
    }
```

The parameters for the `mock` function are:

| Name            | Type            | Default                | Description                                               |
|-----------------|-----------------|------------------------|-----------------------------------------------------------|
| `expectedCount` | `ExpectedCount` | `ExpectedCount.once()` | The number of times the request is expected to be invoked |
| `method`        | `HttpMethod`    | `HttpMethod.GET`       | The expected method                                       |
| `uri`           | `URI`           | none                   | The expected URI                                          |
| `block`         | lambda          | none                   | The DSL lambda (see below)                                |

There are also `mockGet` and `mockPost` functions; these are convenience functions that avoid the need to specify the
method separately (although as noted above, `GET` is the default).
They do not take a `method` parameter (obviously).

Many of the functions within the `mock` lambda are named identically to the `MockRestRequestMatchers` static functions,
but in this case they are functions in the DSL created by the `mock` function.
There are also additional functions related to the use of JSON.

The following functions are available:

| Name                    | Parameter(s)                       | Description                                                       |
|-------------------------|------------------------------------|-------------------------------------------------------------------|
| `requestTo`             | `String`                           | Matches the URI by string                                         |
| `requestTo`             | `URI`                              | Matches the URI                                                   |
| `requestTo`             | `Matcher<String>`                  | Matches the URI using a `Matcher`                                 |
| `method`                | `HttpMethod`                       | Matches the method                                                |
| `queryParam`            | `String`, `vararg String`          | Matches a named query parameter against a set of values           |
| `header`                | `String`, `vararg String`          | Matches a named header against a set of values                    |
| `header`                | `String`, `vararg Matcher<String>` | Matches a named header against a set of values using `Matcher`s   |
| `acceptApplicationJSON` |                                    | Matches the `Accept` header as compatible with `application/json` |
| `headerDoesNotExist`    | `String`                           | Expects the named header to not be present                        |
| `requestJSON`           | lambda - see below                 | Matches the request body using the `kjson-test` library           |

The `requestJSON` function allows the request body to be matched using the
[`kjson-test`](https://github.com/pwall567/kjson-test) library.
For example:
```kotlin
    mockRestServiceServer.mock {
        requestTo("/endpoint")
        method(HttpMethod.POST)
        requestJSON {
            property("id", isUUID)
            property("name", length(1..99))
        }
    }
```
See the documentation for [`kjson-test`](https://github.com/pwall567/kjson-test) for more details on the matching
capabilities available using that library.

### `respondJSON`

To configure the mock operation to respond with a JSON object serialized by the `kjson` library, just use the
`.respondJSON` function in place of the `.andRespond` function:
```kotlin
    mockRestServiceServer.mockGet {
        requestTo("/endpoint")
    }.respondJSON {
        ResponseData(date = LocalDate.now(), extra = "XYZ")
    }
```

The last parameter of the function is a lambda which will be evaluated to create the response object; this object will
then be serialized using the `kjson` library.

The `kjson` serialization will use the `JSONConfig` configuration as described [below](#configuration).

The full set of parameters for `respondJSON` is:

| Name      | Type          | Default          | Description                              |
|-----------|---------------|------------------|------------------------------------------|
| `status`  | `HttpStatus`  | `HttpStatus.GET` | The status to be returned                |
| `headers` | `HttpHeaders` | empty list       | The headers to be added to the response  |
| `block`   | lambda        | none             | The lambda to create the response object |

## Configuration

The `kjson` serialization and deserialization functions all take an optional
[`JSONConfig`](https://github.com/pwall567/kjson/blob/main/USERGUIDE.md#configuration) object.
The `JSONConfig` to be used by the functions invoked by the `kjson-spring-test` library may be provided in the usual
Spring manner:
```kotlin
@Configuration
open class SpringAppConfig {

    @Bean open fun config(): JSONConfig {
        return JSONConfig {
            // configuration options here
        }
    }

}
```

If the project is also using the [`kjson-spring`](https://github.com/pwall567/kjson-spring), the same configuration may
be shared by both libraries.

## Dependency Specification

The latest version of the library is 3.2.1 (the version number of this library matches the version of `kjson` with which
it was built), and it may be obtained from the Maven Central repository.
(The following dependency declarations assume that the library will be included for test purposes; this is
expected to be its principal use.)

### Maven
```xml
    <dependency>
      <groupId>io.kjson</groupId>
      <artifactId>kjson-spring</artifactId>
      <version>3.2.1</version>
      <scope>test</scope>
    </dependency>
```
### Gradle
```groovy
    testImplementation 'io.kjson:kjson-spring:3.2.1'
```
### Gradle (kts)
```kotlin
    testImplementation("io.kjson:kjson-spring:3.2.1")
```

Peter Wall

2022-07-12
