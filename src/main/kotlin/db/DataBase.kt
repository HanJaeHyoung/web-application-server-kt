package db

import model.User

object DataBase {
    private val users: MutableMap<String?, User> = mutableMapOf()
    fun addUser(user: User) {
        users[user.userId] = user
    }

    fun findUserById(userId: String?): User? {
        return users[userId]
    }

    fun findAll(): Collection<User> {
        return users.values
    }
}