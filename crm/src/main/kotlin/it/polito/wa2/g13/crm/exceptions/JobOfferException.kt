package it.polito.wa2.g13.crm.exceptions

import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus


sealed class JobOfferException(override val message: String, override val cause: Throwable?) :
    RuntimeException(message, cause) {

    data class NotFound(override val message: String, override val cause: Throwable? = null) :
        JobOfferException(message, cause) {
        companion object {
            @JvmStatic
            fun from(id: Long): NotFound =
                NotFound(
                    message = "JobOffer with id: $id was not found!"
                )
        }
    }

    data class ForbiddenTargetStatus(override val message: String, override val cause: Throwable? = null) :
        JobOfferException(message, cause) {
        companion object {
            @JvmStatic
            fun from(curr: JobOfferStatus, target: JobOfferStatus): ForbiddenTargetStatus =
                ForbiddenTargetStatus(
                    message = "JobOffer cannot change from $curr to $target"
                )
        }

    }

    data class MissingProfessional(override val message: String, override val cause: Throwable? = null) :
        JobOfferException(message, cause) {
        companion object {
            @JvmStatic
            fun from(id: Long): MissingProfessional =
                MissingProfessional(
                    message = "JobOffer $id is not linked to Professional"
                )
        }
    }

    data class NoteNotFound(override val message: String, override val cause: Throwable? = null) :
        JobOfferException(message, cause) {
        companion object {
            @JvmStatic
            fun from(id: Long): MissingProfessional =
                MissingProfessional(
                    message = "Note with $id not found"
                )
        }
    }
}