package it.polito.wa2.g13.crm.exceptions

import it.polito.wa2.g13.crm.data.BaseEntity

sealed class ContactException(override val message: String, override val cause: Throwable?) :
    RuntimeException(message, cause) {

    data class NotFound(override val message: String, override val cause: Throwable? = null) :
        ContactException(message, cause) {
        companion object {
            @JvmStatic
            fun from(id: Long): NotFound = NotFound(
                message = "Contact with id: $id was not found!"
            )

            @JvmStatic
            inline fun <reified T : BaseEntity> fromRelation(contactId: Long, relationId: Long): NotFound = NotFound(
                message = "${T::class.simpleName}@$relationId not found of Contact@$contactId"
            )
        }
    }

    data class Duplicate(override val message: String, override val cause: Throwable? = null) :
        ContactException(message, cause) {
        companion object {
            @JvmStatic
            inline fun <reified T : BaseEntity> fromRelation(contactId: Long): NotFound = NotFound(
                message = "Contact@$contactId already has an entity with the same value of ${T::class.simpleName}"
            )
        }
    }
}