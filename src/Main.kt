import java.util.Locale
import kotlin.math.abs


enum class JourneyType { SINGLE, RETURN }

data class Station(
    val code: String,
    var name: String,
    var zone: Int,
    var singlePrice: Int = 0,
    var returnPrice: Int = 0,
    var takingsPence: Int = 0
)

data class TicketQuote(
    val origin: Station,
    val destination: Station,
    val journeyType: JourneyType,
    val pricePence: Int
)


interface FareCalculator {
    fun price(origin: Station, destination: Station, journeyType: JourneyType): Int
}

class SimpleFareCalculator : FareCalculator {
    override fun price(origin: Station, destination: Station, journeyType: JourneyType): Int {
        return when (journeyType) {
            JourneyType.SINGLE -> destination.singlePrice
            JourneyType.RETURN -> destination.returnPrice
        }
    }
}


object Money {
    fun formatPence(pence: Int): String {
        val pounds = pence / 100
        val pennies = pence % 100
        return "£$pounds.${pennies.toString().padStart(2, '0')}"
    }

    fun parseToPence(text: String): Int? {
        val cleaned = text.replace("\u00a3", "").trim()
        return try {
            val amount = cleaned.toDouble()
            (amount * 100).toInt()
        } catch (_: Exception) { null }
    }
}



interface IO {
    fun println(msg: String)
    fun prompt(msg: String): String
}

class ConsoleIO : IO {
    override fun println(msg: String) = kotlin.io.println(msg)
    override fun prompt(msg: String): String {
        kotlin.io.print(msg)
        return readLine() ?: ""
    }
}


class TicketMachine(
    private val stations: MutableList<Station>,
    private val origin: Station,
    private val fareCalc: FareCalculator,
    private val io: IO
) {
    fun run() {
        io.println("=== Train Ticket Machine (Console) ===")
        mainMenu()
    }

    private fun mainMenu() {
        loop@ while (true) {
            when (menu("Main Menu", listOf("Search & Buy Ticket", "View Takings by Destination", "Admin", "Exit"))) {
                1 -> searchAndBuyFlow()
                2 -> showTakings()
                3 -> adminMenu()
                4 -> { io.println("Goodbye."); break@loop }
            }
        }
    }

    private fun adminMenu() {
        loop@ while (true) {
            when (menu("Admin Menu", listOf("View All Destinations", "Add/Edit Destination", "Adjust Destination Prices", "Exit Admin Mode"))) {
                1 -> viewAllDestinations()
                2 -> addOrEditMenu()
                3 -> adjustDestinationPrices()
                4 -> break@loop
            }
        }
    }

    private fun addOrEditMenu() {
        loop@ while (true) {
            when (menu("Add/Edit Menu", listOf("Add New Destination", "Edit Existing Destination", "Back"))) {
                1 -> addDestination()
                2 -> editDestination()
                3 -> break@loop
            }
        }
    }

    private fun addDestination() {
        val code = io.prompt("Enter new station code: ").uppercase(Locale.getDefault()).take(3)
        if (stations.any { it.code == code }) {
            io.println("Station code already exists.")
            return
        }
        val name = io.prompt("Enter station name: ")
        val zone = io.prompt("Enter zone number: ").toIntOrNull()?.coerceAtLeast(1) ?: 1
        val single = Money.parseToPence(io.prompt("Enter single ticket price in pounds (e.g., 3.20): ")) ?: 300
        val rtn = Money.parseToPence(io.prompt("Enter return ticket price in pounds (e.g., 5.80): ")) ?: 550
        stations.add(Station(code, name, zone, single, rtn))
        io.println("New station added.")
    }

    private fun editDestination() {
        val code = io.prompt("Enter station code to edit: ").uppercase(Locale.getDefault()).take(3)
        val existing = stations.find { it.code == code }
        if (existing == null) {
            io.println("Station not found.")
            return
        }
        val name = io.prompt("Enter new station name (was ${existing.name}): ")
        val zone = io.prompt("Enter new zone number (was ${existing.zone}): ").toIntOrNull()?.coerceAtLeast(1) ?: existing.zone
        val single = Money.parseToPence(io.prompt("Enter new single ticket price in pounds (was ${Money.formatPence(existing.singlePrice)}): ")) ?: existing.singlePrice
        val rtn = Money.parseToPence(io.prompt("Enter new return ticket price in pounds (was ${Money.formatPence(existing.returnPrice)}): ")) ?: existing.returnPrice

        existing.name = name
        existing.zone = zone
        existing.singlePrice = single
        existing.returnPrice = rtn

        io.println("Station updated.")
    }

    private fun adjustDestinationPrices() {
        val code = io.prompt("Enter station code to adjust: ").uppercase(Locale.getDefault())
        val station = stations.find { it.code == code }
        if (station == null) {
            io.println("Station not found.")
            return
        }
        val single = Money.parseToPence(io.prompt("Enter new single price in pounds (was ${Money.formatPence(station.singlePrice)}): "))
        val rtn = Money.parseToPence(io.prompt("Enter new return price in pounds (was ${Money.formatPence(station.returnPrice)}): "))
        if (single != null) station.singlePrice = single
        if (rtn != null) station.returnPrice = rtn
        io.println("Prices updated for ${station.name}.")
    }

    private fun viewAllDestinations() {
        io.println("\n--- All Destinations ---")
        val dests = stations.sortedBy { it.name }
        for (s in dests) {
            io.println("${s.name} (Code: ${s.code}, Zone: ${s.zone})")
            io.println("  Single: ${Money.formatPence(s.singlePrice)}, Return: ${Money.formatPence(s.returnPrice)}, Sales: ${Money.formatPence(s.takingsPence)}")
        }
        io.println("")
    }

    private fun searchAndBuyFlow() {
        io.println("\n--- Search Ticket ---")
        val destination = chooseDestination()
        val journeyType = chooseJourneyType()
        val price = fareCalc.price(origin, destination, journeyType)
        val quote = TicketQuote(origin, destination, journeyType, price)

        io.println("\nQuote:")
        io.println("  ${origin.name} -> ${destination.name}")
        io.println("  Type: $journeyType")
        io.println("  Price: ${Money.formatPence(price)}")

        val proceed = yesNo("Proceed to buy this ticket now?")
        if (!proceed) {
            io.println("Cancelled.\n")
            return
        }
        handlePurchase(quote)
    }

    private fun handlePurchase(quote: TicketQuote) {
        io.println("\n--- Payment ---")
        io.println("Price due: ${Money.formatPence(quote.pricePence)}")

        var insertedPence = 0
        while (true) {
            val entry = io.prompt("Enter money amount (e.g., 3.50) or 'X' to cancel: ").trim().lowercase(Locale.getDefault())
            if (entry == "x") {
                io.println("Transaction cancelled.\n")
                return
            }
            val pence = Money.parseToPence(entry)
            if (pence == null || pence <= 0) {
                io.println("Invalid amount. Try again.")
                continue
            }
            insertedPence += pence
            io.println("Inserted total: ${Money.formatPence(insertedPence)}")

            if (insertedPence >= quote.pricePence) break
            val still = quote.pricePence - insertedPence
            io.println("Not enough. Need ${Money.formatPence(still)} more.")
        }

        val change = insertedPence - quote.pricePence
        printTicketBlock(quote)

        quote.destination.takingsPence += quote.pricePence

        if (change > 0) io.println("Change: ${Money.formatPence(change)}")
        io.println("Thank you!\n")
    }

    private fun printTicketBlock(quote: TicketQuote) {
        val typeText = if (quote.journeyType == JourneyType.SINGLE) "Single" else "Return"
        val priceText = Money.formatPence(quote.pricePence).replace("\u00a3", "")
        io.println("\n***")
        io.println(quote.origin.name.uppercase())
        io.println("to")
        io.println(quote.destination.name.uppercase())
        io.println("Price: $priceText [$typeText]")
        io.println("***\n")
    }

    private fun chooseDestination(): Station {
        val dests = stations.filter { it.code != origin.code }
        val idx = menu("Choose destination", dests.map { "${it.name} (Zone ${it.zone})" })
        return dests[idx - 1]
    }

    private fun chooseJourneyType(): JourneyType {
        val idx = menu("Choose ticket type", listOf("Single", "Return"))
        return if (idx == 1) JourneyType.SINGLE else JourneyType.RETURN
    }

    private fun showTakings() {
        io.println("\n--- Takings by Destination Station ---")
        val sorted = stations.sortedBy { it.name }
        if (sorted.all { it.takingsPence == 0 }) {
            io.println("(No sales yet.)\n")
            return
        }
        sorted.forEach {
            io.println("${it.name.padEnd(16)} : ${Money.formatPence(it.takingsPence)}")
        }
        io.println("")
    }

    private fun menu(title: String, options: List<String>): Int {
        io.println("\n$title")
        for (i in options.indices) io.println("  ${i + 1}) ${options[i]}")
        while (true) {
            val input = io.prompt("Choose [1-${options.size}]: ").trim()
            val n = input.toIntOrNull()
            if (n != null && n in 1..options.size) return n
            io.println("Invalid choice. Try again.")
        }
    }

    private fun yesNo(q: String): Boolean {
        while (true) {
            when (io.prompt("$q (y/n): ").trim().lowercase(Locale.getDefault())) {
                "y", "yes" -> return true
                "n", "no" -> return false
                else -> io.println("Please answer y/n.")
            }
        }
    }
}


object Network {
    var stations = mutableListOf(
        Station("PAD", "Paddington", 1, 320, 580),
        Station("KGX", "King's Cross", 1, 300, 550),
        Station("MAN", "Manchester Piccadilly", 3, 1200, 2200),
        Station("EDB", "Edinburgh Waverley", 5, 2000, 3800),
        Station("BHM", "Birmingham New Street", 4, 1500, 2700)
    )
    val origin: Station get() = stations.first { it.code == "PAD" }
}

fun main() {
    val io = ConsoleIO()
    val machine = TicketMachine(
        stations = Network.stations,
        origin = Network.origin,
        fareCalc = SimpleFareCalculator(),
        io = io
    )
    machine.run()
}
