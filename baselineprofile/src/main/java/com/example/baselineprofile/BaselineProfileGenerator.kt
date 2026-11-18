package com.example.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates Startup and Baseline Profiles for LakExpense covering:
 * 1. Cold app startup through Splash -> Onboarding / Home Dashboard.
 * 2. Vertical LazyColumn scrolling across Home, Transactions, Budgets, Reports, and Settings.
 * 3. Quick Add Transaction open/entry interaction path.
 *
 * Run against a connected device or Managed Virtual Device with:
 * `gradle :app:generateBaselineProfile`
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() {
        rule.collect(
            packageName = TARGET_PACKAGE,
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()
            ensureHomeDashboardReady()
        }
    }

    @Test
    fun criticalUserJourneysAndScroll() {
        rule.collect(
            packageName = TARGET_PACKAGE,
            includeInStartupProfile = false
        ) {
            pressHome()
            startActivityAndWait()
            ensureHomeDashboardReady()

            // 1. Scroll Home Dashboard
            flingListVertically("home_dashboard_list")

            // 2. Transactions History scroll
            clickAndWaitFor("nav_transactions", "transactions_list")
            flingListVertically("transactions_list")

            // 3. Budgets scroll
            clickAndWaitFor("nav_budgets", "budgets_list")
            flingListVertically("budgets_list")

            // 4. Analytics & Reports scroll
            clickAndWaitFor("nav_reports", "reports_list")
            flingListVertically("reports_list")

            // 5. Profile & Settings scroll
            clickAndWaitFor("profile_settings_button", "settings_list")
            flingListVertically("settings_list")
            device.findObject(By.res("header_back_button"))?.click()
            device.wait(Until.hasObject(By.res("nav_quick_add_fab")), WAIT_TIMEOUT_MS)

            // 6. Quick Add Transaction path
            clickAndWaitFor("nav_quick_add_fab", "quick_add_chip_1000")
            device.findObject(By.res("quick_add_chip_1000"))?.click()
            device.waitForIdle()
            device.findObject(By.res("quick_add_close_button"))?.click()
            device.wait(Until.hasObject(By.res("nav_home")), WAIT_TIMEOUT_MS)
        }
    }
}

internal const val TARGET_PACKAGE = "com.aistudio.lakexpense.slvault"
internal const val WAIT_TIMEOUT_MS = 8_000L

internal fun MacrobenchmarkScope.ensureHomeDashboardReady() {
    if (device.wait(Until.hasObject(By.res("home_dashboard_list")), 4_000L)) {
        return
    }
    val nameField = device.wait(Until.findObject(By.res("onboarding_name_input")), WAIT_TIMEOUT_MS)
    if (nameField != null) {
        nameField.text = "Kasun"
        device.waitForIdle()
        // Scroll down onboarding if needed and enable sample dataset for scrollable CUJs
        device.findObject(By.res("onboarding_demo_switch"))?.click()
        var submitBtn = device.findObject(By.res("onboarding_submit_button"))
        if (submitBtn == null) {
            device.findObject(By.scrollable(true))?.scroll(Direction.DOWN, 1.0f)
            submitBtn = device.wait(Until.findObject(By.res("onboarding_submit_button")), 2_000L)
        }
        submitBtn?.click()
        device.wait(Until.hasObject(By.res("home_dashboard_list")), WAIT_TIMEOUT_MS)
    }
}

internal fun MacrobenchmarkScope.clickAndWaitFor(clickTag: String, targetTag: String) {
    device.wait(Until.findObject(By.res(clickTag)), WAIT_TIMEOUT_MS)?.click()
    device.wait(Until.hasObject(By.res(targetTag)), WAIT_TIMEOUT_MS)
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.flingListVertically(listTag: String) {
    val list = device.wait(Until.findObject(By.res(listTag)), WAIT_TIMEOUT_MS) ?: return
    list.setGestureMargin(device.displayWidth / 5)
    list.fling(Direction.DOWN)
    device.waitForIdle()
    list.fling(Direction.UP)
    device.waitForIdle()
}
