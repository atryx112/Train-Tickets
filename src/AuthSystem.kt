class AuthSystem {

    // Hard-coded admin login
    private val users = listOf(
        User("admin", "password123", "admin")
    )

    fun authenticate(username: String, password: String): User? {
        return users.find { it.username == username && it.password == password }
    }
}
