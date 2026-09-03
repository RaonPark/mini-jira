package org.raonpark.backend.common.exceptions

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

private val log = KotlinLogging.logger { }

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(exception = [ConstraintViolationException::class])
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<APIError> {
        return ResponseEntity.badRequest().body(
            APIError(
                code = "VALIDATION_FAILED",
                message = "Validation failed",
                fieldErrors = ex.constraintViolations.map {
                    APIError.FieldError(
                        field = it.propertyPath.last().name,
                        message = it.message,
                    )
                }
            )
        )
    }

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<APIError> {
        log.warn { "${ex.code} (${ex.message})" }
        return ResponseEntity.status(ex.status).body(APIError(ex.code, ex.message))
    }

    @ExceptionHandler(exception = [MethodArgumentNotValidException::class])
    fun handleBodyValidation(ex: MethodArgumentNotValidException) =
        ResponseEntity.badRequest().body(
            APIError(
                code = "VALIDATION_FAILED",
                message = "입력값을 확인해주세요.",
                fieldErrors = ex.bindingResult.fieldErrors.map{
                    APIError.FieldError(field = it.field, message = it.defaultMessage ?: "잘못된 값입니다.")
                }
            )
        )

    @ExceptionHandler(exception = [Exception::class])
    fun handleUnexpected(ex: Exception): ResponseEntity<APIError> {
        log.error(ex) { "Unexpected error" }
        return ResponseEntity.internalServerError().body(
            APIError(code = "INTERNAL_ERROR", message = "일시적인 오류가 발생했습니다.")
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException) =
        ResponseEntity.badRequest().body(
            APIError(
                code = "VALIDATION_FAILED",
                message = "입력값을 확인해주세요.",
                fieldErrors = listOf(
                    APIError.FieldError(field = ex.name, message = "허용되지 않는 값입니다: ${ex.value}")
                ),
            )
        )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<APIError> {
        log.warn { "Malformed request body: ${ex.mostSpecificCause.message}" }
        return ResponseEntity.badRequest().body(
            APIError(
                code = "MALFORMED_REQUEST",
                message = "요청 형식이 올바르지 않습니다.",
            )
        )
    }
}
