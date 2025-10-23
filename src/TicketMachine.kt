import java.math.BigDecimal

class TicketMachine(
    private val network: Network,
    private val fareCalc: FareCalculator,
    private val io: IO,
    private val originName: String   // <-- fixed origin configured at startup
) {
    fun run() {
        loop@ while (true) {
            when (menu("Main Menu", listOf("Buy Ticket", "Admin", "Exit"))) {
                0 -> buyTicket()
                1 -> admin()
                else -> break@loop
            }
        }
        io.println("Goodbye.")
    }

    private fun buyTicket() {
        val views = network.all()
        if (views.isEmpty()) { io.println("No destinations configured."); return }

        // Only choose destination (no origin selection)
        val destIdx = io.chooseFrom("Choose destination:", views.map { it.name })
        val typeIdx = io.chooseFrom("Journey type:", listOf("SINGLE", "RETURN"))

        val dest = network.findByName(views[destIdx].name)!!
        val type = if (typeIdx == 0) JourneyType.SINGLE else JourneyType.RETURN

        val price = fareCalc.calculateFare(dest, type)
        val quote = TicketQuote(dest.name, type, price)
        dest.recordSale()

        // Receipt in the exact structure you asked
        io.println("***")
        io.println(originName)
        io.println("to")
        io.println(quote.destination)
        io.println("Price: ${quote.price} [${quote.type}]")
        io.println("***")
    }

    private fun admin() {
        when (menu("Admin", listOf("List Destinations", "Add Destination", "Edit Destination", "Change All Prices by Factor", "Back"))) {
            0 -> listDestinations()
            1 -> addDestination()
            2 -> editDestination()
            3 -> bulkChange()
        }
    }

    private fun listDestinations() {
        val all = network.all()
        if (all.isEmpty()) { io.println("No destinations configured."); return }
        io.println("Station | Single | Return | Sales")
        all.forEach { io.println("${it.name} | ${it.single} | ${it.ret} | ${it.sales}") }
    }

    private fun addDestination() {
        val name = io.readLine("New station name: ").ifBlank {
            io.println("Name cannot be empty."); return
        }
        if (network.findByName(name) != null) { io.println("Station already exists."); return }
        val single = io.readMoney("Single price")
        val ret = io.readMoney("Return price")
        network.add(Station(name, single, ret))
        io.println("Added $name.")
    }

    private fun editDestination() {
        val names = network.all().map { it.name }
        if (names.isEmpty()) { io.println("No stations."); return }
        val idx = io.chooseFrom("Choose station to edit:", names)
        val stationName = names[idx]
        val newSingle = io.readOptionalMoney("New single price")
        val newReturn = io.readOptionalMoney("New return price")
        if (!network.editPrices(stationName, newSingle, newReturn)) {
            io.println("Station not found.")
        } else {
            io.println("Updated $stationName.")
        }
    }

    private fun bulkChange() {
        val factor: BigDecimal = io.readFactor("Multiply all prices by factor")
        network.bulkAdjustPrices(factor)
        io.println("All prices updated by ×$factor.")
    }

    private fun menu(title: String, items: List<String>): Int {
        io.println("\n== $title ==")
        return io.chooseFrom("Select an option:", items)
    }
}
