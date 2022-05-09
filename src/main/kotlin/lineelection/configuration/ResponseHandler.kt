package lineelection.configuration

import lineelection.annotations.ResponseModelAnnotation
import lineelection.constants.Status
import lineelection.models.ErrorInfoDto
import lineelection.models.ResponseModel
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import java.lang.reflect.AnnotatedElement
import kotlin.reflect.KClass


@ControllerAdvice
class ResponseHandler : ResponseBodyAdvice<Any> {
    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        return true
    }

    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handleException(ex: Exception): Any {
        ex.printStackTrace()
        return getErrorInfo(ex)
    }

    override fun beforeBodyWrite(
            body: Any?,
            returnType: MethodParameter,
            selectedContentType: MediaType,
            selectedConverterType: Class<out HttpMessageConverter<*>>,
            request: ServerHttpRequest,
            response: ServerHttpResponse
    ): Any? {
        val httpResult = responseModel(body, returnType)
        val httpStatus = getHttpStatus(httpResult)
        response.setStatusCode(httpStatus)
        return httpResult
    }

    /**
     * Method for define response data
     */
    private fun responseModel(body: Any?, returnType: MethodParameter): Any? {
        val methodReturnType = returnType.method?.returnType

        if (methodReturnType == Void.TYPE && isAnnotatedResponseModel(returnType.annotatedElement)) {
            return ResponseModel(status = Status.OK.message)
        }

        return when (body) {
            is ErrorInfoDto -> ResponseModel(
                    Status.ERROR.message,
                    body.errorMessage ?: body.httpResponseStatus.reasonPhrase
            )
            is ResponseModel -> {
                body.status = Status.OK.message
                body
            }
            else -> body
        }
    }

    /**
     * Method for check is annotation response model annotation
     */
    private fun isAnnotatedResponseModel(annotatedElement: AnnotatedElement): Boolean {
        return annotatedElement.getAnnotation(ResponseModelAnnotation::class.java) != null
    }

    /**
     * Method for get error info from Exception class
     */
    private fun getErrorInfo(exception: Exception): ErrorInfoDto {
        return when {
            exception.hasCause(IllegalArgumentException::class) || exception.hasCause(IllegalStateException::class) -> {
                ErrorInfoDto(
                        httpResponseStatus = HttpStatus.BAD_REQUEST,
                        errorMessage = exception.message
                )
            }
            exception.hasCause(AccessDeniedException::class) -> ErrorInfoDto(
                    httpResponseStatus = HttpStatus.UNAUTHORIZED,
                    errorMessage = exception.message
            )
            else -> ErrorInfoDto(
                    httpResponseStatus = HttpStatus.INTERNAL_SERVER_ERROR,
                    errorMessage = exception.message
            )
        }
    }

    private fun Throwable.hasCause(clazz: KClass<*>): Boolean {
        return listOf(this, this.cause).any { clazz.isInstance(it) }
    }

    /**
     * Method for get http status from response
     *
     * @param response
     */
    private fun getHttpStatus(response: Any?): HttpStatus {
        response ?: return HttpStatus.NO_CONTENT
        return when (response) {
            is ErrorInfoDto -> response.httpResponseStatus
            else -> HttpStatus.OK
        }
    }
}