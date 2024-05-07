package it.polito.wa2.g13.crm.data.joboffer


class JobOfferStateMachine(
    private val currentStatus: JobOfferStatus
) {
    fun isStatusFeasible(nextStatus: JobOfferStatus): Boolean {
        return when (currentStatus) {
            JobOfferStatus.Created -> {
                when (nextStatus) {
                    JobOfferStatus.SelectionPhase -> true
                    JobOfferStatus.Aborted -> true
                    else -> false
                }
            }

            JobOfferStatus.SelectionPhase -> {
                when (nextStatus) {
                    JobOfferStatus.CandidateProposal -> true
                    JobOfferStatus.Aborted -> true
                    else -> false
                }
            }

            JobOfferStatus.CandidateProposal -> {
                when (nextStatus) {
                    JobOfferStatus.SelectionPhase -> true
                    JobOfferStatus.Consolidated -> true
                    JobOfferStatus.Aborted -> true
                    else -> false
                }
            }

            JobOfferStatus.Consolidated -> {
                when (nextStatus) {
                    JobOfferStatus.SelectionPhase -> true
                    JobOfferStatus.Done -> true
                    JobOfferStatus.Aborted -> true
                    else -> false
                }
            }

            JobOfferStatus.Done -> {
                when (nextStatus) {
                    JobOfferStatus.SelectionPhase -> true
                    else -> false
                }
            }

            JobOfferStatus.Aborted -> {
                false
            }
        }
    }

}

