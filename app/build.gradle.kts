plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services) // Firebase 플러그인
}

android {
    namespace = "com.example.volunteerkim"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.volunteerkim"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Firebase BoM 사용 (버전 관리 통합)
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))

    // Firebase 개별 라이브러리 추가
    implementation("com.google.firebase:firebase-auth") // Firebase Authentication
    implementation("com.google.firebase:firebase-database") // Realtime Database
    implementation("com.google.firebase:firebase-firestore") // Firestore (필요한 경우)

    // AndroidX 및 기타 라이브러리
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // 네이버 지도 SDK
    implementation("com.naver.maps:map-sdk:3.20.0")

    // 테스트 라이브러리
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
