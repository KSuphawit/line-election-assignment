package lineelection.configuration

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import lineelection.annotations.ResponseModelAnnotation
import lineelection.constants.Status
import lineelection.entities.Candidate
import lineelection.models.ErrorInfoDto
import lineelection.models.ResponseModel
import org.junit.Before
import org.junit.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.test.util.ReflectionTestUtils
import java.io.File
import java.lang.reflect.AnnotatedElement
import kotlin.test.*

class ResponseHandlerTest {

    @InjectMockKs
    lateinit var responseHandler: ResponseHandler

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testHandleException() {
        val responseHandler = spyk<ResponseHandler>(recordPrivateCalls = true)
        val exception = Exception()
        val errorInfoDto = ErrorInfoDto(HttpStatus.OK)

        every { responseHandler["getErrorInfo"](exception) } returns errorInfoDto

        val result = responseHandler.handleException(exception)
        assertEquals(errorInfoDto, result)
        verify(exactly = 1) { responseHandler["getErrorInfo"](exception) }
    }

    @Test
    fun testBeforeBodyWrite() {
        val responseHandler = spyk<ResponseHandler>(recordPrivateCalls = true)
        val body = Candidate()
        val returnType = mockk<MethodParameter>()
        val mediaType = mockk<MediaType>()
        val selectedConverterType = HttpMessageConverter::class.java
        val request = mockk<ServerHttpRequest>()
        val response = mockk<ServerHttpResponse>()

        every { responseHandler["responseModel"](body, returnType) } returns body
        every { responseHandler["getHttpStatus"](body) } returns HttpStatus.OK
        every { response.setStatusCode(HttpStatus.OK) } just Runs

        val result = responseHandler.beforeBodyWrite(
                body,
                returnType,
                mediaType,
                selectedConverterType,
                request,
                response
        )
        assertEquals(body, result)
        verify(exactly = 1) { responseHandler["responseModel"](body, returnType) }
        verify(exactly = 1) { responseHandler["getHttpStatus"](body) }
        verify(exactly = 1) { response.setStatusCode(HttpStatus.OK) }
    }

    @Test
    fun testResponseModel() {
        val responseHandler = spyk<ResponseHandler>(recordPrivateCalls = true)
        val returnType = mockk<MethodParameter>()

        // Case return void and have annotation ResponseModelAnnotation
        every { returnType.method?.returnType } returns Void.TYPE
        every { responseHandler["isAnnotatedResponseModel"](returnType.annotatedElement) } returns true

        var result = ReflectionTestUtils.invokeMethod<Any>(responseHandler, "responseModel", Void.TYPE, returnType)
        assertEquals(Status.OK.message, (result as ResponseModel).status)
        assertNull(result.message)
        verify(exactly = 1) { responseHandler["isAnnotatedResponseModel"](returnType.annotatedElement) }
        clearAllMocks(answers = false)

        // Case ErrorInfoDto with errorMessage
        var body: Any = ErrorInfoDto(httpResponseStatus = HttpStatus.OK, "errorMessage")
        every { returnType.method?.returnType } returns ErrorInfoDto::class.java

        result = ReflectionTestUtils.invokeMethod<Any>(responseHandler, "responseModel", body, returnType)
        assertEquals(Status.ERROR.message, (result as ResponseModel).status)
        assertEquals("errorMessage", result.message)
        verify(exactly = 0) { responseHandler["isAnnotatedResponseModel"](any<AnnotatedElement>()) }

        // Case ErrorInfoDto without errorMessage
        body = ErrorInfoDto(httpResponseStatus = HttpStatus.BAD_REQUEST)

        result = ReflectionTestUtils.invokeMethod<Any>(responseHandler, "responseModel", body, returnType)
        assertEquals(Status.ERROR.message, (result as ResponseModel).status)
        assertEquals(HttpStatus.BAD_REQUEST.reasonPhrase, result.message)
        verify(exactly = 0) { responseHandler["isAnnotatedResponseModel"](any<AnnotatedElement>()) }

        // Case ResponseModel
        body = ResponseModel(message = "Hello")
        every { returnType.method?.returnType } returns ResponseModel::class.java

        result = ReflectionTestUtils.invokeMethod<Any>(responseHandler, "responseModel", body, returnType)
        assertEquals(Status.OK.message, (result as ResponseModel).status)
        assertEquals("Hello", result.message)
        verify(exactly = 0) { responseHandler["isAnnotatedResponseModel"](any<AnnotatedElement>()) }

        // Case Other
        body = Candidate(id = 1L)
        every { returnType.method?.returnType } returns Candidate::class.java

        result = ReflectionTestUtils.invokeMethod<Any>(responseHandler, "responseModel", body, returnType)
        assertEquals(Candidate(id = 1L), result)
        verify(exactly = 0) { responseHandler["isAnnotatedResponseModel"](any<AnnotatedElement>()) }
    }

    @Test
    fun testIsAnnotatedResponseModel() {
        val annotatedElement = mockk<AnnotatedElement>()
        val annotation = mockk<ResponseModelAnnotation>()

        every { annotatedElement.getAnnotation(ResponseModelAnnotation::class.java) } returns annotation andThen null

        // Case annotation not null
        var result = ReflectionTestUtils.invokeMethod<Boolean>(responseHandler, "isAnnotatedResponseModel", annotatedElement)
        assertNotNull(result)
        assertTrue(result)

        // Case annotation is null
        result = ReflectionTestUtils.invokeMethod<Boolean>(responseHandler, "isAnnotatedResponseModel", annotatedElement)
        assertNotNull(result)
        assertFalse(result)
    }

    @Test
    fun testGetErrorInfo() {
        val errorMessage = "errorMessage"

        // Case IllegalArgumentException
        var exception: Exception = IllegalArgumentException(errorMessage)
        var result = ReflectionTestUtils.invokeMethod<ErrorInfoDto>(responseHandler, "getErrorInfo", exception)!!
        assertEquals(HttpStatus.BAD_REQUEST, result.httpResponseStatus)
        assertEquals(errorMessage, result.errorMessage)

        // Case IllegalStateException
        exception = IllegalStateException(errorMessage)
        result = ReflectionTestUtils.invokeMethod<ErrorInfoDto>(responseHandler, "getErrorInfo", exception)!!
        assertEquals(HttpStatus.BAD_REQUEST, result.httpResponseStatus)
        assertEquals(errorMessage, result.errorMessage)

        // Case AccessDeniedException
        val tempFile = File.createTempFile("temp_prefix", null)
        exception = AccessDeniedException(tempFile)
        result = ReflectionTestUtils.invokeMethod<ErrorInfoDto>(responseHandler, "getErrorInfo", exception)!!
        assertEquals(HttpStatus.UNAUTHORIZED, result.httpResponseStatus)
        assertNotNull(result.errorMessage)
        tempFile.delete()

        // Case Other
        exception = Exception(errorMessage)
        result = ReflectionTestUtils.invokeMethod<ErrorInfoDto>(responseHandler, "getErrorInfo", exception)!!
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.httpResponseStatus)
        assertEquals(errorMessage, result.errorMessage)
    }

    @Test
    fun testHasCause() {
        // Case matched with expected
        var result = ReflectionTestUtils.invokeMethod<Boolean>(responseHandler, "hasCause", IllegalArgumentException(), IllegalArgumentException::class)
        assertNotNull(result)
        assertTrue(result)

        // Case mismatched with expected
        result = ReflectionTestUtils.invokeMethod<Boolean>(responseHandler, "hasCause", IllegalArgumentException(), IllegalStateException::class)
        assertNotNull(result)
        assertFalse(result)
    }

    @Test
    fun testGetHttpStatus() {
        // Case NO_CONTENT
        var result = ReflectionTestUtils.invokeMethod<HttpStatus>(responseHandler, "getHttpStatus", null)
        assertEquals(HttpStatus.NO_CONTENT, result)

        // Case Get from ErrorInfoDto
        result = ReflectionTestUtils.invokeMethod<HttpStatus>(responseHandler, "getHttpStatus", ErrorInfoDto(HttpStatus.ACCEPTED))
        assertEquals(HttpStatus.ACCEPTED, result)

        // Case Other
        result = ReflectionTestUtils.invokeMethod<HttpStatus>(responseHandler, "getHttpStatus", Candidate())
        assertEquals(HttpStatus.OK, result)
    }

}