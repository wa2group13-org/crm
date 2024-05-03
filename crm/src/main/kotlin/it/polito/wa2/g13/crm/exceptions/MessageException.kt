package it.polito.wa2.g13.crm.exceptions

sealed class MessageException(override val message: String, override val cause: Throwable?) :
    RuntimeException(message, cause) {

    data class NotFound(override val message: String, override val cause: Throwable? = null) :
        MessageException(message, cause)

    data class ForbiddenTransition(override val message: String, override val cause: Throwable? = null) :
        MessageException(message, cause)
}