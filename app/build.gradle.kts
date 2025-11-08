import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // KSP for annotation processing
    alias(libs.plugins.hilt) // Hilt DI
    id("kotlin-parcelize")
    kotlin("plugin.serialization") version "1.9.22"
    id("jacoco")
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
    id("org.owasp.dependencycheck") version "8.4.2"
    id("com.google.gms.google-services") version "4.4.0"
    id("com.google.firebase.crashlytics") version "2.9.9"
    id("com.google.firebase.firebase-perf") version "1.4.2"
}

android {
    namespace = "com.gf.mail"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gf.mail"
        minSdk = 24
        targetSdk = 35
        versionCode = getVersionCode()
        versionName = getVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Enable multidex for large app
        multiDexEnabled = true

        // Build config fields
        buildConfigField("String", "VERSION_NAME", "\"${getVersionName()}\"")
        buildConfigField("int", "VERSION_CODE", "${getVersionCode()}")
        buildConfigField("boolean", "IS_DEBUG_BUILD", "Boolean.parseBoolean(\"${isDebugBuild()}\")")

        // Proguard configuration files
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("boolean", "IS_DEBUG_BUILD", "true")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("boolean", "IS_DEBUG_BUILD", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

	lint {
	        abortOnError = false
			baseline = file("lint-baseline.xml")
	}

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

// Jacoco configuration
jacoco {
    toolVersion = "0.8.8"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/databinding/**",
        "**/android/databinding/**",
        "**/androidx/databinding/**",
        "**/*_Hilt*.*",
        "**/hilt_aggregated_deps/**",
        "**/*_Factory*.*",
        "**/*_MembersInjector*.*",
        "**/*Module*.*",
        "**/*Component*.*"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) {
            include("jacoco/testDebugUnitTest.exec")
        }
    )
}

// ktlint configuration
ktlint {
    version.set("0.48.2")
    debug.set(false)
}

// Detekt configuration
detekt {
    toolVersion = "1.23.4"
    config.setFrom(files("${project.rootDir}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    autoCorrect = true
    baseline = file("${project.rootDir}/config/detekt/detekt-baseline.xml")
    source.setFrom(files("src/main/java", "src/main/kotlin"))
}

// Dependency Check configuration
dependencyCheck {
    failBuildOnCVSS = 7f
    suppressionFile = "${project.rootDir}/config/owasp/suppressions.xml"
    format = "HTML"
    outputDirectory = "${layout.buildDirectory.get()}/reports/dependency-check"
}

dependencies {
    // Import Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.animation)

    // Core KTX
    implementation(libs.androidx.core.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Email
    implementation(libs.javamail)
    implementation(libs.javamail.activation)

    // Dependency Injection - Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // UI/UX
    implementation(libs.coil.compose)
    implementation(libs.accompanist.permissions)

    // Security
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric)

    // Utilities
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.browser)

    // QR Code
    implementation(libs.zxing.android.embedded)
    
    // HTML Parser
    implementation("org.jsoup:jsoup:1.17.2")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

    // Multidex
    implementation("androidx.multidex:multidex:2.0.1")

    // ML Kit
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")
    implementation("com.google.mlkit:text-recognition-devanagari:16.0.0")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
}

tasks.withType<Test> {
    ignoreFailures = true
}

fun getVersionCode(): Int {
    return System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: 1
}

fun getVersionName(): String {
    return System.getenv("VERSION_NAME") ?: "1.0.0"
}

fun isDebugBuild(): Boolean {
    return System.getenv("DEBUG_BUILD")?.toBooleanStrictOrNull() ?: false
}