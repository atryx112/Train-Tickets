fun main() {
    val initialStations = listOf(
        Station("Manchester Piccadilly", Money.of(55.00), Money.of(50.00)),
        Station("Birmingham New Street", Money.of(65.50), Money.of(35.00)),
        Station("Edinburgh Waverley",    Money.of(75.00), Money.of(82.00)),
        Station("Leeds",                 Money.of(8235.00), Money.of(46.00)),
        Station("Bristol Temple Meads",  Money.of(915.00), Money.of(42.00)),
        Station("London",           Money.of(1915.00), Money.of(2242.00)),
        Station("Val City Centre", Money.of(1915.00), Money.of(2242.00)),

        )

    val network = Network(initialStations)
    val fareCalc = SimpleFareCalculator()
    val io = ConsoleIO()
    val originName = "London Euston"   // <-- fixed origin printed on the receipt

    val app = TicketMachine(network, fareCalc, io, originName)
    app.run()
}
