plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "id.cctv.rebag"
    compileSdk = 36

    defaultConfig {
        applicationId = "id.cctv.rebag"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    applicationVariants.all {
        val versi = versionName ?: "1.0"
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl)
                .outputFileName = "RebagHarian-v$versi.apk"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
}
