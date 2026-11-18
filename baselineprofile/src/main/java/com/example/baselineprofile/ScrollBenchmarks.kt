package com.example.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Frame-timing Macrobenchmark verifying vertical scroll smoothness across
 * Home Dashboard, Transactions History, Budgets, and Reports with vs. without
 * Baseline Profile pre-compilation.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ScrollBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollHomeAndTransactionsCompilationNone() =
        measureScroll(CompilationMode.None())

    @Test
    fun scrollHomeAndTransactionsBaselineProfiles() =
        measureScroll(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun measureScroll(compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.WARM,
            iterations = 5,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                ensureHomeDashboardReady()
            }
        ) {
            flingListVertically("home_dashboard_list")

            clickAndWaitFor("nav_transactions", "transactions_list")
            flingListVertically("transactions_list")

            clickAndWaitFor("nav_budgets", "budgets_list")
            flingListVertically("budgets_list")

            clickAndWaitFor("nav_reports", "reports_list")
            flingListVertically("reports_list")
        }
    }
}
