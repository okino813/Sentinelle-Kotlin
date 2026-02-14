package com.okino813.sentinelle

data class NavItem(
    val label: String,
    val icon: Int,
    val badgeCount: Int,
)

sealed class AppState {
    object Tutorial : AppState()
    object Auth : AppState()
    object Main : AppState()
}