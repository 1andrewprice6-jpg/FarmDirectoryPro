package com.example.farmdirectoryupgraded.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine as coroutinesCombine

/**
 * Combines three flows into a single flow using the given transform function.
 */
fun <T1, T2, T3, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    transform: suspend (T1, T2, T3) -> R
): Flow<R> = coroutinesCombine(flow1, flow2, flow3, transform)
