## 2024-01-07

- Updated the core functionality of the app to compute habit statuses on the go instead of caching them in the database. This change improves the robustness and clarity of the code.
- Refactored 'Routine' to 'Habit' across the application for better semantics.
- Improved performance by computing each date in a separate coroutine and introduced pagination for the RoutineCalendarScreen.
- Fixed various minor bugs and performance issues.