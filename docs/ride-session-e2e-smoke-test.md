# Ride Session End-to-End Smoke Test

Use this test with one passenger device and one driver device.

## Test setup

1. Build and install the development app on both devices.
2. Use different passenger and driver accounts.
3. Confirm both devices use the same backend environment.
4. Confirm the driver build has mock location enabled in `TripNavigationViewModel`.
5. Confirm the backend is running with realtime broadcasting enabled.

The mock driver location publishes a point near the current target every second. It is for development testing only.

## Test steps

1. Passenger creates a ride.
2. Driver accepts the ride.
3. Driver opens trip navigation.
4. Wait until `Confirm Pickup` appears.
5. Confirm that the passenger map receives driver location updates.
6. Driver taps `Confirm Pickup`.
7. Wait until `Arrive` appears.
8. Driver taps `Arrive`.
9. Confirm arrival and payment.
10. Confirm that the driver request succeeds.
11. Confirm that the passenger feedback modal appears.
12. Submit passenger feedback.
13. Confirm that the passenger returns to the home screen.

## Failure checks

- Completion must fail when the driver is not within 15 meters of the destination.
- A repeated completion request must not reopen the passenger feedback modal.
- If the passenger app is closed before completion, reopen it and confirm that the pending feedback modal appears.
- If the driver location is missing, completion must fail.

## Before release

Set `ENABLE_MOCK_DRIVER_LOCATION_UPDATES` to `false` before creating a production build.
