package com.darley.unifound.printer.data

import com.darley.unifound.printer.data.dao.UserDao.getSavedUser
import com.darley.unifound.printer.data.model.LoggedInUser
import java.io.IOException
import kotlin.Result

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
object LoginDataSource {

    fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            // TODO: handle loggedInUser authentication

            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
            val user = getSavedUser()!!
            Result.success(user)
        } catch (e: Throwable) {
            Result.failure(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}