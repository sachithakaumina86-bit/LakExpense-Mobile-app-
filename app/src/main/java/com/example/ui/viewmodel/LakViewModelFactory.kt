package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.LakExpenseDatabase
import com.example.data.local.PreferencesManager
import com.example.data.repository.LakExpenseRepository
import com.example.security.VaultSecurityManager
import com.example.util.ExportManager

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: LakExpenseDatabase = LakExpenseDatabase.getInstance(appContext)
    val preferencesManager: PreferencesManager = PreferencesManager(appContext)
    val securityManager: VaultSecurityManager = VaultSecurityManager(appContext)
    val exportManager: ExportManager = ExportManager(appContext)
    val repository: LakExpenseRepository = LakExpenseRepository(
        context = appContext,
        db = database,
        prefs = preferencesManager,
        security = securityManager
    )
}

class LakViewModelFactory(
    private val appContext: Context,
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                appContext = appContext,
                repository = container.repository,
                exportManager = container.exportManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
