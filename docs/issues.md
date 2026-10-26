# Issues

## Navigation chrome is not synchronized with screen transitions

- Area: Passenger navigation
- Status: Planned
- Symptom: When navigating back, the top bar changes before the previous screen finishes leaving. The destination screen appears later.
- Cause: The top bar observes the navigation route directly. The `NavHost` content uses a separate fade transition.
- Expected result: The top bar and screen content change at the same time.
- Possible fix: Use one transition state for the top bar and screen content, or move each screen's top bar inside its destination.
- Scope: Passenger app navigation

## Future issues

Add new issues below this section.
