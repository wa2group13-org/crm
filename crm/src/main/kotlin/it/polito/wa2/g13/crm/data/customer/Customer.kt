package it.polito.wa2.g13.crm.data.customer

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.data.contact.Contact
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
class Customer(

    @OneToOne
    var contact : Contact,

    @OneToMany(mappedBy = "contact")
    var offers : MutableSet<JobOffer>,

    var note : String

) : BaseEntity() {

    fun addOffer(offer: JobOffer){
        offer.contact = this
        this.offers.add(offer)
    }
}


@Entity
class JobOffer(

    @ManyToOne
    var contact: Customer

) : BaseEntity()