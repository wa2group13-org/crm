package it.polito.wa2.g13.crm.data.customer

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.data.contact.Contact
import it.polito.wa2.g13.crm.data.joboffer.JobOffer
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
class Customer(

    @OneToOne
    var contact : Contact,

    @OneToMany(mappedBy = "customer")
    var offers : MutableSet<JobOffer>,

    var note : String

) : BaseEntity()
