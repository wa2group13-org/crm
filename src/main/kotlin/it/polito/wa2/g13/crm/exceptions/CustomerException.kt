package it.polito.wa2.g13.crm.exceptions


sealed class CustomerException(override val message: String, override val cause: Throwable?) :
    RuntimeException(message, cause) {
    data class NotFound(override val message: String, override val cause: Throwable? = null) :
        CustomerException(message, cause) {
        companion object {
            @JvmStatic
            fun from(id: Long): NotFound = NotFound(
                message = "Customer with id: $id was not found!"
            )
        }
    }

    data class ContactAlreadyTaken(override val message: String, override val cause: Throwable? = null) :
        CustomerException(message, cause) {
        companion object {
            @JvmStatic
            fun from(contactId: Long): ContactAlreadyTaken = ContactAlreadyTaken(
                message = "Contact with id: $contactId was not available!"
            )
        }
    }
}