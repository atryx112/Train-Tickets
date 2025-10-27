fun main() {
    val initialStations = listOf(
        Station("Manchester Piccadilly", Money.of(100.00), Money.of(100.00)),
        Station("Birmingham New Street", Money.of(100.50), Money.of(100.00)),
        Station("Edinburgh Waverley",    Money.of(100.00), Money.of(100.00)),
        Station("Leeds",                 Money.of(100.00), Money.of(100.00)),
        Station("Bristol Temple Meads",  Money.of(100.00), Money.of(100.00)),
    )

    val network = Network(initialStations)
    val fareCalc = SimpleFareCalculator()
    val io = ConsoleIO()
    val originName = "London Euston"   // <-- fixed origin printed on the receipt

    val app = TicketMachine(network, fareCalc, io, originName)
    app.run()
}
