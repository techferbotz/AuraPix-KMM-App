package com.ferbotz.aurapix.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class AuraViewModel {
    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    open fun onCleared() {
        scope.cancel()
    }
}
