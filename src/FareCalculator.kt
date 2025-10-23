interface FareCalculator {
    fun calculateFare(destination: Station, type: JourneyType): Money
}

class SimpleFareCalculator : FareCalculator {
    override fun calculateFare(destination: Station, type: JourneyType): Money =
        destination.quote(type)
}