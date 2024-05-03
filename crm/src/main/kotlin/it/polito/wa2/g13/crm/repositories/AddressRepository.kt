package it.polito.wa2.g13.crm.repositories

import it.polito.wa2.g13.crm.data.contact.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface AddressRepository : JpaRepository<Address, Long> {
    @Query("select a from Address a where a.city=:city and a.street=:street and a.civic=:civic and a.postalCode=:postalCode")
    fun findByAddress(city: String, street: String, civic: String, postalCode: String): Address?

    @Query("select a from Address a join a.contacts c where a.id=:id and c.id=:contactId")
    fun findByIdAndContactId(id: Long, contactId: Long): Address?
}