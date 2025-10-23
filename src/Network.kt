class Network(initial: List<Station> = emptyList()) {
    private val stations: MutableList<Station> = initial.toMutableList()

    fun all(): List<StationView> = stations.map { it.snapshot() }.sortedBy { it.name.lowercase() }

    fun findByName(name: String): Station? =
        stations.firstOrNull { it.name.equals(name, ignoreCase = true) }

    fun add(station: Station) {
        require(findByName(station.name) == null) { "Station already exists: ${station.name}" }
        stations += station
    }

    fun editPrices(name: String, newSingle: Money?, newReturn: Money?): Boolean {
        val s = findByName(name) ?: return false
        newSingle?.let { s.setSinglePrice(it) }
        newReturn?.let { s.setReturnPrice(it) }
        return true
    }

    fun bulkAdjustPrices(factor: java.math.BigDecimal) {
        stations.forEach { it.adjustPricesByFactor(factor) }
    }
}

