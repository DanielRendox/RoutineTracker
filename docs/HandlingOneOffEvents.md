# Handling one-off events

One-time events such as displaying a snackbar or navigating to a different destination that are typically passed from the ViewModel to the UI are implemented using Compose State. No SharedFlows or Channels involved.

For example, after deleting a habit, ViewModel decides to throw an event that tells the UI to navigate to a different screen. For that it updates the respective `StateFlow` variable. The UI observes this event in a `LaunchedEffect` block, which executes the action once the state gets changed. 

## Benefits

- state can't be lost in any scenario ([read more](https://medium.com/androiddevelopers/viewmodel-one-off-event-antipatterns-16a1da869b95));
- easy to use not only in `ViewModel`s, but also in plain state holder classes;
- ensures immediate response to new events (such as when a snackbar needs to be updated immediately upon a new event, instead of waiting for the previous message to disappear).

## Limitations

One-off events are handled with state but state is not really suitable for this purpose it keeps the value even after the event is finished. So this requires us to manually reset the state after it’s consumed. 

Othewise, some unexpected behavior may happen. Continuing the initial example, if the user tries to navigate back to the screen where the habit was deleted, it will not actually navigate because the state in that screen will immediately tell the program to navigate back.

That’s why  resetting the state is unified throughout the project with the help of extensions in [`UiEvent.kt`](https://github.com/DanielRendox/RoutineTracker/blob/main/core/ui/src/main/java/com/rendox/routinetracker/core/ui/helpers/UiEvent.kt)

## How to use

So all events should be objects of `UiEvent` interface and should typcially be nullable so that when the event is fired off, it could be reset to null. This interface has a function called `onConsumed` which should be overriden when the event is fired off. You can also pass data using `val data`. 

All events should be observed in the UI using `ObserveUiEvent` composable. This composable automatically calls `event.onConsumed()` once the action is finished.

Back to our example with deleting a habit. We would implement navigation as follows:

```kotlin
class RoutineDetailsScreenViewModel: ViewModel() {
	private val _navigateBackEvent: MutableStateFlow<UiEvent<Any>?> = MutableStateFlow(null)
	val navigateBackEvent = _navigateBackEvent.asStateFlow()
	
	fun onDeleteHabit() = viewModelScope.launch {
	    /* the logic of deleting a habit */
	    
	    _navigateBackEvent.update {
	        object : UiEvent<Any> {
	            override val data: Any = Unit
	            override fun onConsumed() {
	                _navigateBackEvent.update { null }
	            }
	        }
	    }
	}
}

@Composable
fun RoutineDetailsScreen(
	viewModel: RoutineDetailsScreenViewModel = koinViewModel(),
	navigateBack: () -> Unit,
) {
	val navigateBackEvent by viewModel.navigateBackEvent.collectAsStateWithLifecycle()
	ObserveUiEvent(navigateBackEvent) {
      navigateBack()
  }
}
```

So here we have a `StateFlow` that is initially set to `null`. When the user wants to delete the habit, `navigateBackEvent` gets a value of the `UiEvent` object. There is an implementation of `onConsumed` defined that resets our `StateFlow` back to null. 

Then we observe this event using `ObserveUiEvent` in the compose code. After the we’ve navigated back, the `navigateBackEvent` will be reset to null automatically.

> Now, in practice, it is rarely necessary to reset the state back and we could get away without doing it. However, we must follow this pattern for all one-off events to keep consistency inside the codebase and avoid unexpected behavior.