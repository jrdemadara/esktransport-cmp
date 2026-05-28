package org.noztek.esktransport.app.navigation

object RootRoute {
    const val STARTER = "root/starter"
    const val AUTH = "root/auth"
    const val PASSENGER = "root/passenger"
    const val DRIVER = "root/driver"
}

object StarterRoute {
    const val WELCOME = "starter/welcome"
}

object AuthRoute {
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
    const val OTP = "auth/otp"
    const val FORGOT_PASSWORD = "auth/forgot_password"
    const val RESET_PASSWORD = "auth/reset_password"
}

object PassengerRoute {
    const val HOME = "passenger/home"
}

object DriverRoute {
    const val HOME = "driver/home"
}

object DevRoute {
    const val MAP_PREVIEW = "dev/map_preview"
}

fun org.noztek.esktransport.core.session.domain.SessionUser.authenticatedRootRoute(): String {
    val normalizedRoles = roles.map { it.trim().lowercase() }.toSet()
    val normalizedRole = primaryRole?.trim()?.lowercase()
    return when {
        normalizedRole == "driver" || normalizedRoles.contains("driver") -> RootRoute.DRIVER
        normalizedRole == "passenger" || normalizedRole == "customer" ||
            normalizedRoles.contains("passenger") || normalizedRoles.contains("customer") -> RootRoute.PASSENGER
        else -> RootRoute.AUTH
    }
}
