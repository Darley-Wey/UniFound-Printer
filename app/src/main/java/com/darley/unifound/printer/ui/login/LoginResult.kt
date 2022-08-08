package com.darley.unifound.printer.ui.login

import com.darley.unifound.printer.data.model.LoggedInUser

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
     val success: LoggedInUser? = null,
     val error: String? = null,
)