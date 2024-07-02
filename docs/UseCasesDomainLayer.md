# Use Cases (The domain layer of CLEAN architecture)

Routine Tracker uses CLEAN architecture with an optional domain layer. This layer was introduced because the app requires rather complicated logic for saving and processing activities. Thanks to the domain layer, the codebase is more granular.

The majority of user intents are processed through Use Cases, but some of them do not require any additional logic and can call the data layer directly. However, skipping use cases is not a good approach because it is inconsistent and not scalable.

> [!IMPORTANT]  
> That’s why everything is exposed to UI through the domain layer. The UI layer cannot access the data layer.

At the same time, we avoid boilerplate by making use of Kotlin Functional (SAM) interfaces for those UseCases that would otherwise only call the repository and not execute any additional logic like it is done in [`HabitDomainModule.kt`](https://github.com/DanielRendox/RoutineTracker/blob/2ff5195e96f7c710f62f8cf0ac5ca081c1854aa9/core/domain/src/main/java/com/rendox/routinetracker/core/domain/di/HabitDomainModule.kt) (Koin is used for dependency injection):

```kotlin
val habitDomainModule = module {
    single {
        InsertHabitUseCase(get<HabitRepository>()::insertHabit)
    }

    single {
        GetHabitUseCase(get<HabitRepository>()::getHabitById)
    }

    single {
        GetAllHabitsUseCase(get<HabitRepository>()::getAllHabits)
    }

    single {
        DeleteHabitUseCase(get<HabitRepository>()::deleteHabit)
    }
}

fun interface InsertHabitUseCase : suspend (Habit) -> Unit
fun interface GetHabitUseCase : suspend (Long) -> Habit
fun interface GetAllHabitsUseCase : suspend () -> List<Habit>
fun interface DeleteHabitUseCase : suspend (Long) -> Unit
```