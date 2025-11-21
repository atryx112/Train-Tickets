import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

class SpecialOfferManager {

    private val idCounter = AtomicInteger(1)
    private val offers = mutableListOf<SpecialOffer>()

    fun addOffer(
        stationName: String,
        description: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): SpecialOffer {

        val offer = SpecialOffer(
            id = idCounter.getAndIncrement(),
            stationName = stationName,
            description = description,
            startDate = startDate,
            endDate = endDate
        )

        offers += offer
        return offer
    }

    fun getAllOffers(): List<SpecialOffer> {
        return offers.toList()
    }

    fun deleteOffer(id: Int): Boolean {
        return offers.removeIf { it.id == id }
    }

    fun searchOffers(stationName: String?): List<SpecialOffer> {
        return if (stationName == null || stationName.isBlank()) {
            offers.toList()
        } else {
            offers.filter { it.stationName.equals(stationName, ignoreCase = true) }
        }
    }
}
