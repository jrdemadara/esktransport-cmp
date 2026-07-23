package org.noztek.esktransport.feature.driver.settings.presentation

internal fun Long?.driverIdLabel(): String {
    return this?.let { "Driver ID: DRV-${it.toString().padStart(6, '0')}" } ?: "Driver ID: -"
}
