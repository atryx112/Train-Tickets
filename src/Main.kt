fun main() {
    val initialStations = listOf(
        Station("Manchester Piccadilly", Money.of(55.00), Money.of(50.00)),
        Station("Birmingham New Street", Money.of(65.50), Money.of(35.00)),
        Station("Edinburgh Waverley",    Money.of(75.00), Money.of(82.00)),
        Station("Reading",               Money.of(45.00), Money.of(46.00)),
        Station("Bristol Temple Meads",  Money.of(65.00), Money.of(42.00)),
        Station("London",                Money.of(78.00), Money.of(29.00)),
        Station("Coventry ",             Money.of(38.00), Money.of(56.00)),
        Station("Milton Keyness",        Money.of(49.00), Money.of(42.00)),
        Station("Nottingham",            Money.of(68.00), Money.of(78.00)),

        )

    val network = Network(initialStations)
    val fareCalc = SimpleFareCalculator()
    val io = ConsoleIO()
    val originName = "London Euston"   // <-- fixed origin printed on the receipt

    val app = TicketMachine(network, fareCalc, io, originName)
    app.run()
}

