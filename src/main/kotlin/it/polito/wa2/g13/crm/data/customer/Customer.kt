package it.polito.wa2.g13.crm.data.customer

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.data.contact.Contact
import it.polito.wa2.g13.crm.data.joboffer.JobOffer
import it.polito.wa2.g13.crm.dtos.CreateCustomerDTO
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
class Customer(

    @OneToOne(
        cascade = [CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH],
        fetch = FetchType.EAGER
    )
    var contact: Contact,

    @OneToMany(mappedBy = "customer")
    var jobOffers: MutableSet<JobOffer>,

    var note: String?,


    ) : BaseEntity() {
    companion object {
        fun from(customer: CreateCustomerDTO, contact: Contact): Customer = Customer(
            note = customer.note,
            jobOffers = mutableSetOf(),
            contact = contact,
        )
    }

}
