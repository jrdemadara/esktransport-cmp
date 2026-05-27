package org.noztek.esktransport.feature.common.register.domain.model

enum class RegisterRole(val apiValue: String) {
    CUSTOMER("customer"),
    DRIVER("driver");

    companion object {
        fun from(value: String): org.noztek.esktransport.feature.common.register.domain.model.RegisterRole = when (value.lowercase()) {
            DRIVER.apiValue -> DRIVER
            else -> CUSTOMER
        }
    }
}
