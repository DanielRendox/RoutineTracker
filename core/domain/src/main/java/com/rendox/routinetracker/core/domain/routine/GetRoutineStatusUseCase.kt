package com.rendox.routinetracker.core.domain.routine

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.model.CompletableStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

//class GetRoutineStatusUseCase(
//    private val routineRepository: RoutineRepository,
//    private val completionHistoryRepository: CompletionHistoryRepository,
//) {
//    operator fun invoke(currentDate: LocalDate): Flow<List<CompletableStatus>> {
//
//    }
//}