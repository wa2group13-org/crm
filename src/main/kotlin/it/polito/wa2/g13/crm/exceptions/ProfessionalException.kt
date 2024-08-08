package it.polito.wa2.g13.crm.exceptions

sealed class ProfessionalException(override val message: String, override val cause: Throwable?) :
    RuntimeException(message, cause) {

    data class NotFound(override val message: String, override val cause: Throwable? = null) :
        ProfessionalException(message, cause) {
        companion object {
            @JvmStatic
            fun from(id: Long): NotFound = NotFound(
                message = "Professional with id: $id was not found!"
            )
        }
    }

    data class InvalidContactState(override val message: String, override val cause: Throwable? = null) :
        ProfessionalException(message, cause) {
        companion object {
            @JvmStatic
            fun from(contactId: Long): InvalidContactState = InvalidContactState(
                message = "Tried to assign Contact@$contactId to Professional, but it is already taken!"
            )
        }
    }
}