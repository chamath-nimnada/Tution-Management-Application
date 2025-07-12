plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.tutionmanagementapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tutionmanagementapplication"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packagingOptions {
        exclude("META-INF/NOTICE.md")
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")
        exclude("META-INF/ASL2.0")
        exclude("META-INF/LGPL2.1")
        exclude("META-INF/DEPENDENCIES")
        pickFirst("META-INF/mailcap")
        pickFirst("META-INF/mimetypes.default")
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // QR Code generation library
    implementation(libs.core.v351)
    implementation(libs.zxing.android.embedded)

    // Firebase Storage
    implementation(libs.firebase.storage)

    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    implementation(libs.cardview)

    //Email
    implementation(libs.android.mail)
    implementation(libs.android.activation)

    //Map
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
}