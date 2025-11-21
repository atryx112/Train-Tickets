import java.time.LocalDate

data class SpecialOffer(
    val id: Int,
    val stationName: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    fun isActiveOn(date: LocalDate): Boolean {
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }
}
