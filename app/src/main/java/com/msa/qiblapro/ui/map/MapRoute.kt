package com.msa.qiblapro.ui.map

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.qiblapro.ui.compass.QiblaViewModel

@Composable
fun MapRoute(vm: QiblaViewModel) {
    val st = vm.state.collectAsStateWithLifecycle().value
    MapScreen(
        st = st,
        onSetMapType = vm::setMapType
    )
}
