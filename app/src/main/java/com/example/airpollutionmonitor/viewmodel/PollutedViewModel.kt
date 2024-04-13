package com.example.airpollutionmonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airpollutionmonitor.App
import com.example.airpollutionmonitor.data.Record
import com.example.airpollutionmonitor.repo.DataRepository
import com.example.airpollutionmonitor.utils.NetworkUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

private const val TAG = "PollutedViewModel"

class PollutedViewModel(private val dataRepository: DataRepository) : ViewModel() {
    val highInfoFlow: SharedFlow<List<Record>>
        get() = _highInfoFlow
    val lowInfoFlow: SharedFlow<List<Record>>
        get() = _lowInfoFlow
    val listStateFlow: StateFlow<ListState>
        get() = _listStateFlow

    private val exceptionHandler = CoroutineExceptionHandler { _, error ->
        when (error) {
            is SocketTimeoutException -> _listStateFlow.value = ListState.Timeout
        }
    }
    private val _highInfoFlow = MutableSharedFlow<List<Record>>(0, 1, BufferOverflow.DROP_OLDEST)
    private var _lowInfoFlow = MutableSharedFlow<List<Record>>(0, 1, BufferOverflow.DROP_OLDEST)
    private var _listStateFlow = MutableStateFlow<ListState>(ListState.Refreshing)

    init {
        getPollutedInfo()
    }

    fun getPollutedInfo() {
        viewModelScope.launch(exceptionHandler) {
            if (NetworkUtil.isConnect(App.appContext)) {
                _listStateFlow.emit(ListState.Refreshing) // TODO: refactor as flow
                val info = dataRepository.getPollutedInfo()
                _highInfoFlow.emit(info.first)
                _lowInfoFlow.emit(info.second)
                _listStateFlow.emit(ListState.ShowAll)
            } else {
                _listStateFlow.emit(ListState.NoNetwork)
            }
        }
    }

    fun handleFilter(expanded: Boolean, itemCount: Int, keyword: String) {
        viewModelScope.launch(exceptionHandler) {
            val state =
                if (!expanded) {
                    ListState.ShowAll
                } else {
                    if (keyword.isNotEmpty()) {
                        if (itemCount == 0) {
                            ListState.NotFound(keyword)
                        } else {
                            ListState.Found
                        }
                    } else {
                        ListState.Hide
                    }
                }
            _listStateFlow.emit(state)
        }
    }
}
