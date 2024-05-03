package it.polito.wa2.g13.crm.aspects

import it.polito.wa2.g13.crm.utils.Err
import it.polito.wa2.g13.crm.utils.Ok
import it.polito.wa2.g13.crm.utils.Result
import org.aspectj.lang.ProceedingJoinPoint
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import kotlin.reflect.KFunction

private val PROCEED_FUN: (ProceedingJoinPoint) -> Any? = ProceedingJoinPoint::proceed
private val PROCEED_NAME = (PROCEED_FUN as KFunction<*>).name

/**
 * [handleResult] allows use to use the return value of [org.aspectj.lang.ProceedingJoinPoint.proceed] in a safe way.
 * The return value it's supposed to be of type [Result] where the [Ok] is of type
 * [ResponseEntity] and the [Err] is a generic error chosen by the implementation, that can be handled
 * using the [handleErr] function.
 *
 * **Example**:
 * ```
 * @Around("@annotation(DocumentResult)")
 * fun handleDocumentErrors(joint: ProceedingJoinPoint): ResponseEntity<Any?> {
 *     val result = joint.proceed()
 *
 *     return handleResult<DocumentError>(result) { error ->
 *         when (error) {
 *             is DocumentError.NotFound -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, error.message)
 *             is DocumentError.Duplicate -> ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, error.message)
 *         }
 *     }
 * }
 * ```
 */
@Suppress("unchecked_cast")
internal inline fun <reified E : Any> handleResult(
    result: Any?,
    handleErr: (E) -> ProblemDetail
): ResponseEntity<Any?> {
    if (result == null) {
        throw IllegalArgumentException("Return type of '$PROCEED_NAME' should be: ${Result::class.qualifiedName}, but it was null")
    }

    if (result !is Result<*, *>) {
        throw IllegalArgumentException("Return type of '$PROCEED_NAME' should be: ${Result::class.qualifiedName}, but it was ${result::class.qualifiedName}")
    }

    val error = when (result) {
        is Ok<*, *> -> {
            // We need to use the @Suppress because when converting generics from java to kotlin
            // the compiler has problems with wild cards.
            if (result.t is ResponseEntity<*>) return result.t as ResponseEntity<Any?>
            else throw IllegalArgumentException(
                "Return Ok type of '$PROCEED_NAME' should be: ${ResponseEntity::class.qualifiedName}, but it was ${if (result.t != null) result.t::class.qualifiedName else null}"
            )
        }

        is Err<*, *> -> result.err
    }

    if (error == null) {
        throw IllegalArgumentException("Return Error type of '$PROCEED_NAME' should be: ${E::class.qualifiedName}, but it was null")
    }

    if (error !is E) {
        throw IllegalArgumentException("Return Error type of '$PROCEED_NAME' should be: ${E::class.qualifiedName}, but it was: ${error::class.qualifiedName}")
    }

    return handleErr(error).let {
        ResponseEntity.of(it).build()
    }
}
