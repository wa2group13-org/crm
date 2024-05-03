package it.polito.wa2.g13.crm.data.message

import it.polito.wa2.g13.crm.data.BaseEntity
import jakarta.persistence.*
import org.springframework.data.jpa.repository.Temporal
import java.time.OffsetDateTime

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["message_id", "status"])])
class MessageActionsHistory(
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(referencedColumnName = "id", name = "message_id", foreignKey = ForeignKey())
    var message: Message,
    @Enumerated(EnumType.STRING)
    var status: Status,
    @Temporal(value = TemporalType.TIMESTAMP)
    var timestamp: OffsetDateTime,
    var comment: String?,
) : BaseEntity()