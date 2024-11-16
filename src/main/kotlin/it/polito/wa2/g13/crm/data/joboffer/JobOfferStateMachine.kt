package it.polito.wa2.g13.crm.data.joboffer


class JobOfferStateMachine(
    private val currentStatus: JobOfferStatus,
) {
    fun isStatusFeasible(nextStatus: JobOfferStatus, nextProfessionalId: Long?): Boolean {
        return when (currentStatus) {
            JobOfferStatus.Created -> {
                when (nextStatus) {
                    JobOfferStatus.SelectionPhase -> true
                    JobOfferStatus.Aborted -> true
                    else -> false
                } && nextProfessionalId == null
            }

            JobOfferStatus.SelectionPhase -> {
                when (nextStatus) {
                    JobOfferStatus.CandidateProposal -> true
                    JobOfferStatus.Aborted -> true
                    else -> false
                } && nextProfessionalId == null
            }

            JobOfferStatus.CandidateProposal -> {
                when (nextStatus) {
                    JobOfferStatus.SelectionPhase -> nextProfessionalId == null
                    JobOfferStatus.Consolidated -> nextProfessionalId != null
                    JobOfferStatus.Aborted -> nextProfessionalId == null
                    else -> false
                }
            }

            JobOfferStatus.Consolidated -> {
                when (nextStatus) {
                    JobOfferStatus.SelectionPhase -> true
                    JobOfferStatus.Done -> true
                    JobOfferStatus.Aborted -> true
                    else -> false
                } && nextProfessionalId == null
            }

            JobOfferStatus.Done -> {
                when (nextStatus) {
                    JobOfferStatus.SelectionPhase -> nextProfessionalId == null
                    else -> false
                }
            }

            JobOfferStatus.Aborted -> {
                false
            }
        }
    }

}

