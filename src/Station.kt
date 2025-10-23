data class Station(
    val name: String,
    private var singlePrice: Money,
    private var returnPrice: Money,
    private var salesCount: Int = 0
) {
    fun quote(type: JourneyType): Money =
        if (type == JourneyType.SINGLE) singlePrice else returnPrice

    fun setSinglePrice(newPrice: Money) {
        require(newPrice.amount >= java.math.BigDecimal.ZERO) { "Single price cannot be negative." }
        singlePrice = newPrice
    }

    fun setReturnPrice(newPrice: Money) {
        require(newPrice.amount >= java.math.BigDecimal.ZERO) { "Return price cannot be negative." }
        returnPrice = newPrice
    }

    fun adjustPricesByFactor(factor: java.math.BigDecimal) {
        require(factor > java.math.BigDecimal.ZERO) { "Factor must be > 0." }
        singlePrice = singlePrice.times(factor)
        returnPrice = returnPrice.times(factor)
    }

    fun recordSale() { salesCount++ }

    fun snapshot(): StationView = StationView(name, singlePrice, returnPrice, salesCount)
}

data class StationView(val name: String, val single: Money, val ret: Money, val sales: Int)
