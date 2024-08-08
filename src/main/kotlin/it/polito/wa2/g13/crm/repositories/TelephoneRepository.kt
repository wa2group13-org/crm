package it.polito.wa2.g13.crm.repositories

import it.polito.wa2.g13.crm.data.contact.Telephone
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TelephoneRepository : JpaRepository<Telephone, Long> {
    fun findByNumber(number: String): Telephone?

    @Query("select t from Telephone t where t.number in :numbers")
    fun findAllByNumbers(numbers: List<String>): List<Telephone>

    @Query("select t from Telephone t join t.contacts c where t.id=:id and c.id=:contactId")
    fun findByIdAndContactId(id: Long, contactId: Long): Telephone?
}