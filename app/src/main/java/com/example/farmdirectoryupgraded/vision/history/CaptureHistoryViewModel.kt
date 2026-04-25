package com.example.farmdirectoryupgraded.vision.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.vision.capture.CaptureMode
import com.example.farmdirectoryupgraded.vision.ledger.CaptureDao
import com.example.farmdirectoryupgraded.vision.ledger.CaptureEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryFilters(
    val mode: CaptureMode? = null,
    val status: String? = null,    // null = ALL; otherwise COMPLETE / INCOMPLETE / INCONSISTENT / REJECTED
    val farmId: String? = null,
)

/**
 * Construct via [Factory] from a Composable:
 *
 *   val vm: CaptureHistoryViewModel = viewModel(
 *       factory = CaptureHistoryViewModel.Factory(app, AppDatabase.getInstance(app).captureDao())
 *   )
 */
class CaptureHistoryViewModel(
    app: Application,
    private val dao: CaptureDao,
) : AndroidViewModel(app) {

    class Factory(
        private val app: Application,
        private val dao: CaptureDao,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CaptureHistoryViewModel(app, dao) as T
    }

    private val _filters = MutableStateFlow(HistoryFilters())
    val filters: StateFlow<HistoryFilters> = _filters

    val captures: StateFlow<List<CaptureEntity>> =
        dao.observeRecent(500).combine(_filters) { rows, f ->
            rows.filter { e ->
                (f.mode == null   || e.mode == f.mode.name) &&
                (f.status == null || e.status == f.status) &&
                (f.farmId == null || e.farmId == f.farmId)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMode(m: CaptureMode?)  { _filters.value = _filters.value.copy(mode = m) }
    fun setStatus(s: String?)     { _filters.value = _filters.value.copy(status = s) }
    fun setFarmId(f: String?)     { _filters.value = _filters.value.copy(farmId = f) }

    private val _detail = MutableStateFlow<CaptureEntity?>(null)
    val detail: StateFlow<CaptureEntity?> = _detail

    fun loadDetail(id: Long) {
        viewModelScope.launch {
            _detail.value = dao.get(id)
        }
    }

    fun clearDetail() {
        _detail.value = null
    }

    fun deleteCapture(id: Long) {
        viewModelScope.launch {
            dao.get(id)?.let { existing ->
                runCatching {
                    java.io.File(existing.rawImagePath).delete()
                }
                dao.delete(id)
            }
            if (_detail.value?.id == id) _detail.value = null
        }
    }
}
