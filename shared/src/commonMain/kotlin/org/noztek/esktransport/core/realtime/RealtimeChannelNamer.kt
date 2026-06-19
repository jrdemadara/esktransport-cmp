package org.noztek.esktransport.core.realtime

class RealtimeChannelNamer {
    fun driverPrivateChannel(userId: Long): String = "private-rider.$userId"

    fun driverOnboardingPrivateChannel(driverId: Long): String = "private-driver.$driverId"

    fun passengerPrivateChannel(userId: Long): String = "private-passenger.$userId"
}
