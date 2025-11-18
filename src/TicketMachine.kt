import java.math.BigDecimal

class TicketMachine(
    private val network: Network,
    private val fareCalc: FareCalculator,
    private val io: IO,
    private val originName: String
) {
    fun run() {
        loop@ while (true) {
            when (menu("Main Menu", listOf("Search for a Ticket", "Admin", "Exit"))) {
                0 -> searchTicket()
                1 -> admin()
                else -> break@loop
            }
        }
        io.println("Goodbye....")
    }

    // === Search flow ===
    private fun searchTicket() {
        val views = network.all()
        if (views.isEmpty()) { io.println("No destinations configured."); return }

        while (true) {
            val choice = menu(
                "Search Tickets",
                listOf("Search by destination", "Search by ticket type", "Back")
            )
            when (choice) {
                0 -> searchByDestination()
                1 -> searchByType()
                else -> return
            }
        }
    }

    private fun searchByDestination() {
        val all = network.all()
        val q = io.readLine("Enter part of the destination name: ").trim()
        val matches = if (q.isBlank()) all else all.filter { it.name.contains(q, ignoreCase = true) }

        if (matches.isEmpty()) {
            io.println("No matching destinations...")
            return
        }

        val destIdx = io.chooseFrom("Choose an option:", matches.map { it.name })
        val dest = network.findByName(matches[destIdx].name)!!

        val typeIdx = io.chooseFrom("Choose an option:", listOf("SINGLE", "RETURN"))
        val type = if (typeIdx == 0) JourneyType.SINGLE else JourneyType.RETURN

        completePurchase(dest, type)
    }

    private fun searchByType() {
        val typeIdx = io.chooseFrom("Choose an option:", listOf("SINGLE", "RETURN"))
        val type = if (typeIdx == 0) JourneyType.SINGLE else JourneyType.RETURN

        // Build a view showing price for the chosen type to help selection
        val views = network.all()
        val listing = views.map { v ->
            val price = if (type == JourneyType.SINGLE) v.single else v.ret
            "${v.name} | $price"
        }

        val pick = io.chooseFrom("Choose an option:", listing)
        val destName = views[pick].name
        val dest = network.findByName(destName)!!

        completePurchase(dest, type)
    }

    // === Money insertion + receipt ===
    private fun completePurchase(dest: Station, type: JourneyType) {
        val price = fareCalc.calculateFare(dest, type)
        io.println("Amount due: $price [$type]")

        var inserted = Money.ZERO
        while (inserted < price) {
            val remaining = price - inserted
            val chunk = io.readMoneyOrCancel("Insert money. Remaining: $remaining")
            if (chunk == null) {
                io.println("Purchase cancelled.")
                return
            }
            inserted += chunk
            if (inserted < price) {
                io.println("Inserted: $inserted  |  Still due: ${price - inserted}")
            }
        }

        val change = inserted - price
        dest.recordSale()

        io.println("***")
        io.println(originName)
        io.println("to")
        io.println(dest.name)
        io.println("Price: $price [$type]")
        io.println("***")

        if (change.isPositive()) {
            io.println("Change: $change")
        }
    }

    // === Admin (unchanged) ===
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
        if (all.isEmpty()) {
            io.println("No destinations configured.")
            return
        }

        // Column widths
        val nameWidth = all.maxOf { it.name.length }.coerceAtLeast(10)
        val priceWidth = 10
        val salesWidth = 5

        // Header
        io.println(
            "%-${nameWidth}s | %-${priceWidth}s | %-${priceWidth}s | %${salesWidth}s"
                .format("Station", "Single", "Return", "Sales")
        )
        io.println("-".repeat(nameWidth + priceWidth * 2 + salesWidth + 9))

        // Rows
        all.forEach {
            io.println(
                "%-${nameWidth}s | %-${priceWidth}s | %-${priceWidth}s | %${salesWidth}d"
                    .format(it.name, it.single, it.ret, it.sales)
            )
        }
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
        val idx = io.chooseFrom("Choose an option:", names)
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
        return io.chooseFrom("Choose an option:", items)
    }
}

