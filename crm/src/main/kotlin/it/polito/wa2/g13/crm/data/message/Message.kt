package it.polito.wa2.g13.crm.data.message

import it.polito.wa2.g13.crm.data.BaseEntity
import jakarta.persistence.*
import org.springframework.data.jpa.repository.Temporal
import java.time.OffsetDateTime

enum class Priority {
    Low,
    Medium,
    High,
}

enum class Status {
    Received,
    Read,
    Discarded,
    Processing,
    Done,
    Failed
}

@Entity
@Table(
    indexes = [
        Index(columnList = "mailId", unique = true),
    ]
)
class Message(
    var body: String?,
    var sender: String,
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(updatable = false)
    var date: OffsetDateTime,
    var subject: String?,

    var channel: String,

    @Enumerated(EnumType.ORDINAL)
    var priority: Priority,

    @Enumerated(EnumType.STRING)
    var status: Status,

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "message")
    var history: MutableSet<MessageActionsHistory>,

    @Column(unique = true, updatable = false)
    var mailId: String?,

) : BaseEntity()
