package it.polito.wa2.g13.crm.data.message

enum class Event {
    ReadMessage,
    DiscardMessage,
    ProcessMessage,
    CompleteMessage,
    FailMessage,
    None
}

class StateMachine(
    private val currentStatus: Status
) {
    fun goTo(newStatus: Status) : Event {
        return when (newStatus) {
            Status.Read -> when (currentStatus) {
                Status.Received -> Event.ReadMessage
                else -> Event.None
            }
            Status.Discarded -> when (currentStatus) {
                Status.Read -> Event.DiscardMessage
                else -> Event.None
            }
            Status.Processing -> when (currentStatus) {
                Status.Read -> Event.ProcessMessage
                else -> Event.None
            }
            Status.Done -> when (currentStatus) {
                Status.Read, Status.Processing -> Event.CompleteMessage
                else -> Event.None
            }
            Status.Failed -> when (currentStatus) {
                Status.Processing, Status.Read -> Event.FailMessage
                else -> Event.None
            }
            else -> Event.None
        }
    }
}
