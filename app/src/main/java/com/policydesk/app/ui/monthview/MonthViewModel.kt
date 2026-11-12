package com.policydesk.app.ui.monthview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.domain.model.MonthViewData
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class YearMonthSelection(
    val year: Int = LocalDate.now().year,
    val month: Int = LocalDate.now().monthValue,
    val selectedDay: Int = LocalDate.now().dayOfMonth
)

internal fun clampDayForMonth(year: Int, month: Int, day: Int): Int {
    val maxDay = LocalDate.of(year, month, 1).lengthOfMonth()
    return day.coerceIn(1, maxDay)
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class MonthViewModel @Inject constructor(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _selection = MutableStateFlow(YearMonthSelection())
    val selection: StateFlow<YearMonthSelection> = _selection

    val monthData: StateFlow<MonthViewData> = _selection
        .flatMapLatest { sel ->
            repository.getMonthViewData(sel.year, sel.month)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MonthViewData(
                year = LocalDate.now().year,
                month = LocalDate.now().monthValue
            )
        )

    fun onPreviousMonth() {
        val current = _selection.value
        val date = LocalDate.of(current.year, current.month, 1).minusMonths(1)
        _selection.value = YearMonthSelection(
            year = date.year,
            month = date.monthValue,
            selectedDay = clampDayForMonth(date.year, date.monthValue, current.selectedDay)
        )
    }

    fun onNextMonth() {
        val current = _selection.value
        val date = LocalDate.of(current.year, current.month, 1).plusMonths(1)
        _selection.value = YearMonthSelection(
            year = date.year,
            month = date.monthValue,
            selectedDay = clampDayForMonth(date.year, date.monthValue, current.selectedDay)
        )
    }

    fun onToday() {
        val today = LocalDate.now()
        _selection.value = YearMonthSelection(today.year, today.monthValue, today.dayOfMonth)
    }

    fun onDaySelected(day: Int) {
        val current = _selection.value
        val clamped = clampDayForMonth(current.year, current.month, day)
        if (current.selectedDay != clamped) {
            _selection.value = current.copy(selectedDay = clamped)
        }
    }
}
