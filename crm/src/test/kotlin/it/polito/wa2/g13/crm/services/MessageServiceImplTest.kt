package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.data.message.Priority
import it.polito.wa2.g13.crm.data.message.Status
import it.polito.wa2.g13.crm.dtos.MessageActionsHistoryDTO
import it.polito.wa2.g13.crm.exceptions.MessageException
import it.polito.wa2.g13.crm.repositories.SortBy
import it.polito.wa2.g13.crm.utils.randomMessage
import it.polito.wa2.g13.crm.utils.randomMessages
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import kotlin.math.min

@SpringBootTest
@Transactional
class MessageServiceImplTest : IntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(MessageServiceImplTest::class.java)

        private val messages = randomMessages(15)
    }

    @Autowired
    lateinit var messageService: MessageService

    @Test
    fun `it should retrieve all paged message inserted`() {
        val limit = 10
        var i = 0

        messages.take(6).forEach {
            messageService.createMessage(it)

            val result = messageService.listMessages(
                0, limit, null, null
            )
            i += 1
            assertEquals(min(i, limit), result.content.size)
        }

    }

    @Test
    fun `it should retrieve all paged message inserted filtered by status `() {
        messageService.createMessage(messages[0]) //received
        messageService.createMessage(messages[6]) //received
        val m1 = messageService.createMessage(messages[1]) //read
        val m2 = messageService.createMessage(messages[2]) //processing
        val m3 = messageService.createMessage(messages[3]) //done
        val m4 = messageService.createMessage(messages[4]) //failed
        val m5 = messageService.createMessage(messages[5]) //discarded

        // read all
        messageService.changeMessageState(m1.id, Status.Read, null)
        messageService.changeMessageState(m2.id, Status.Read, null)
        messageService.changeMessageState(m3.id, Status.Read, null)
        messageService.changeMessageState(m4.id, Status.Read, null)
        messageService.changeMessageState(m5.id, Status.Read, null)

        // READ->PROCESSING
        messageService.changeMessageState(m2.id, Status.Processing, null)

        //READ->DONE
        messageService.changeMessageState(m3.id, Status.Done, null)

        //READ->FAILED
        messageService.changeMessageState(m4.id, Status.Failed, null)

        //READ->DISCARDED
        messageService.changeMessageState(m5.id, Status.Discarded, null)


        assertEquals(messageService.listMessages(0, 10, null, Status.Received).content.size, 2)
        assertEquals(messageService.listMessages(0, 10, null, Status.Read).content.size, 1)
        assertEquals(messageService.listMessages(0, 10, null, Status.Processing).content.size, 1)
        assertEquals(messageService.listMessages(0, 10, null, Status.Done).content.size, 1)
        assertEquals(messageService.listMessages(0, 10, null, Status.Failed).content.size, 1)
        assertEquals(messageService.listMessages(0, 10, null, Status.Discarded).content.size, 1)
    }

    @Test
    fun `it should retrieve all paged message inserted sorted by status `() {
        val m0 = messageService.createMessage(messages[0]) //received
        var m1 = messageService.createMessage(messages[1]) //read
        var m2 = messageService.createMessage(messages[2]) //processing

        m1 = messageService.changeMessageState(m1.id, Status.Read, null)
        m2 = messageService.changeMessageState(m2.id, Status.Read, null)
        m2 = messageService.changeMessageState(m2.id, Status.Processing, null)

        val messages = messageService.listMessages(0, 10, SortBy.StatusDesc, null).content

        assertEquals(listOf(m0, m1, m2), messages)
    }

    @Test
    fun `it should retrieve a message`() {

        val message = messageService.createMessage(messages.first())

        val res = messageService.getMessage(message.id)

        assertThat(res).usingRecursiveComparison().ignoringFields("date").isEqualTo(message)

    }

    @Test
    fun `it should not find the message`() {
        assertThrowsExactly(MessageException.NotFound::class.java) {
            messageService.getMessage(10)
        }

        assertThrowsExactly(MessageException.NotFound::class.java) {
            messageService.changeMessagePriority(10, Priority.High)
        }

        assertThrowsExactly(MessageException.NotFound::class.java) {
            messageService.changeMessageState(10, Status.Processing, "It should fail before this gets even considered")
        }

        assertThrowsExactly(MessageException.NotFound::class.java) {
            messageService.getMessageHistory(10)
        }

    }

    @Test
    fun `it should successfully change the state of the message`() {
        val message = messageService.createMessage(messages.first())

        val res = messageService.changeMessageState(message.id, Status.Read, "message read by the operator")

        assertEquals(message.id, res.id)
        assertEquals(res.status, Status.Read)
    }

    @Test
    fun `it should fail to change the state of the message`() {
        val message = messageService.createMessage(messages.first())

        assertThrowsExactly(MessageException.ForbiddenTransition::class.java) {
            messageService.changeMessageState(message.id, Status.Done, "illegal state")
        }

    }

    @Test
    fun `it should fail to rollback the state of the message`() {
        val message = messageService.createMessage(messages.first())
        assertDoesNotThrow {
            messageService.changeMessageState(message.id, Status.Read, "Reading message")
            messageService.changeMessageState(message.id, Status.Processing, "Processing by HR")
            messageService.changeMessageState(message.id, Status.Done, "Message done")
        }

        assertThrowsExactly(MessageException.ForbiddenTransition::class.java) {
            messageService.changeMessageState(message.id, Status.Received, "ILLEGAL STATE")
        }
    }

    @Test
    fun `it should change the priority of a message`() {
        val message = messageService.createMessage(randomMessage(Priority.Low, null))

        val res = messageService.changeMessagePriority(message.id, Priority.Medium)

        assertEquals(res.priority, Priority.Medium)
    }

    @Test
    fun `it should retrieve the history of a message`() {
        val message =
            messageService.createMessage(messages.first()) //this will automatically add a log into the history

        messageService.changeMessageState(message.id, Status.Read, "Reading message")
        messageService.changeMessageState(message.id, Status.Processing, "Processing by HR")
        messageService.changeMessageState(message.id, Status.Done, null)


        val history = messageService.getMessageHistory(message.id)

        assertThat(history)
            .isNotNull()
            .hasSize(4) // Received, Read, Processing and Done
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("messageId", "timestamp")
            .containsExactlyInAnyOrder(
                MessageActionsHistoryDTO(0, Status.Received, OffsetDateTime.now(), null),
                MessageActionsHistoryDTO(0, Status.Read, OffsetDateTime.now(), "Reading message"),
                MessageActionsHistoryDTO(0, Status.Processing, OffsetDateTime.now(), "Processing by HR"),
                MessageActionsHistoryDTO(0, Status.Done, OffsetDateTime.now(), null),
            )
    }


}