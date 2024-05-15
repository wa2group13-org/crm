package it.polito.wa2.g13.crm.data.joboffer

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.data.professional.Professional
import jakarta.persistence.*
import org.hibernate.annotations.TimeZoneStorage
import org.hibernate.annotations.TimeZoneStorageType
import java.time.OffsetDateTime

@Entity
class JobOfferHistory(
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(referencedColumnName = "id", name = "job_offer_id", foreignKey = ForeignKey())
    var jobOffer: JobOffer,

    @OneToOne
    var assignedProfessional: Professional?,

    @Temporal(TemporalType.TIMESTAMP)
    @TimeZoneStorage(TimeZoneStorageType.COLUMN)
    var logTime: OffsetDateTime,
    @Enumerated(EnumType.STRING)
    var currentStatus: JobOfferStatus,
    @Column(columnDefinition = "text")
    var note: String?,
) : BaseEntity()