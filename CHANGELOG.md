## 2024-01-11 New way of habit status computation [#6](https://github.com/DanielRendox/RoutineTracker/pull/6)

- Updated the core functionality of the app to compute habit statuses on the go instead of caching them in the database. This change improves the robustness and clarity of the code.
- Refactored 'Routine' to 'Habit' across the application for better semantics.
- Improved performance by computing each date in a separate coroutine and introduced pagination for the RoutineCalendarScreen.
- Fixed various minor bugs and performance issues.
- Drop support for period start reset after vacation end in AlternateDaysSchedule. That's because the use cases for that are not clear and requirements may change in the future, while it's tricky to provide solid support for such functionality.

## 2024-01-15 Add streak functionality and refactor

- Added streak functionality to the app. Now the user can see their current and longest streaks for each habit.
- Ensure consistency in following SOLID principles. Split huge classes into smaller parts, change classes to depend on abstractions instead of concrete implementations, use parameterized tests when it's possible.

