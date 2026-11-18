plugins {
  alias(libs.plugins.android.test)
  alias(libs.plugins.androidx.baselineprofile)
}

android {
  namespace = "com.example.baselineprofile"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  defaultConfig {
    minSdk = 28
    targetSdk = 36
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  targetProjectPath = ":app"

  testOptions.managedDevices.allDevices {
    create<com.android.build.api.dsl.ManagedVirtualDevice>("pixel6Api34") {
      device = "Pixel 6"
      apiLevel = 34
      systemImageSource = "aosp-atd"
    }
  }
}

baselineProfile {
  managedDevices += "pixel6Api34"
  useConnectedDevices = true
}

dependencies {
  implementation(libs.androidx.benchmark.macro.junit4)
  implementation(libs.androidx.espresso.core)
  implementation(libs.androidx.junit)
  implementation(libs.androidx.uiautomator)
}
