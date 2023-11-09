package com.rendox.routinetracker.routine_details

//class RoutineDetailsScharedViewModel(
//    routineId: Long,
//    routineRepository: RoutineRepository,
//    getRoutineStatusList: GetRoutineStatusListUseCase,
//    getListOfStreaks: GetListOfStreaksUseCase,
//    insertRoutineStatusIntoHistory: InsertRoutineStatusIntoHistoryUseCase,
//) : ViewModel() {
//
//    private val _routineGeneralDetailsState: MutableStateFlow<UiState> =
//        MutableStateFlow(UiState.Loading)
//    val routineGeneralDetailsState: StateFlow<UiState> = _routineGeneralDetailsState.asStateFlow()
//
//    private val _routineCalendarState = MutableStateFlow(UiState.Loading)
//    val routineCalendarState: StateFlow<UiState> = _routineCalendarState.asStateFlow()
//
//    private val _routineStatisticsState = MutableStateFlow(UiState.Loading)
//    val routineStatisticsState: StateFlow<UiState> = _routineStatisticsState.asStateFlow()
//
//    init {
//        viewModelScope.launch {
//            val routine = routineRepository.getRoutineById(routineId)
//
//            _routineGeneralDetailsState.update {
//                UiState.Success(
//                    RoutineGeneralDetailsUiState(
//                        name = routine.name,
//                        description = routine.description,
//                        progress = routine.progress,
//
//                        )
//                )
//            }
//        }
//    }
//}

//data class RoutineDetailsUiState(
//    val routineName: String,
//    val routineDescription: String?,
//    val routineProgress: Float?,
//    val scheduleType: KClass<out Schedule>,
//    val numOfDueDaysPerPeriod: Int,
//    val correspondingPeriod: DatePeriod?,
//    val sessionDurationMinutes: Int?,
//    val completionTime: LocalTime?,
//    val reminderEnabled: Boolean,
//    val numericalValueRoutineUnit: Routine.NumericalValueRoutineUnit?,
//    val scheduleDeviation: Int,
//    val projectedCompletionDate: LocalDate?,
//    val routineEndDate: LocalDate?,
//    val numOfDaysInCurrentStreak: Int,
//    val numOfDaysInLongestStreak: Int,
//    val routineCompletionPerPeriodStats: RoutineCompletionPerPeriodStats?,
//)

//val numOfDueDaysPerPeriod: Int,
//val correspondingPeriod: DatePeriod?,
//val sessionDurationMinutes: Int?,
//val numOfUnitsPerSession: Int?,
//val unitsOfMeasure: String?,
//val sessionTimeHour: Int?,
//val sessionTimeMinute: Int?,
//val reminderEnabled: Boolean,
//val routineStatusToday: RoutineStatusLite,

//enum class RoutineStatusLite {
//    Pending,
//    Completed,
//    Failed,
//}
//
//data class RoutineCompletionPerPeriodStats(
//    val period: DatePeriod,
//    val timesCompleted: Int,
//    val timesNotCompleted: Int,
//)