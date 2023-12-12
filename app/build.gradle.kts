plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.gms.google-services")
}

android {
  namespace = "com.example.facescore"
  compileSdk = 33

  defaultConfig {
    applicationId = "com.example.facescore"
    minSdk = 24
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

dependencies {

  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.9.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("com.google.android.gms:play-services-auth:20.7.0")
  implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
  implementation("androidx.preference:preference:1.2.0")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
  implementation("com.google.firebase:firebase-analytics-ktx")
  implementation("com.google.firebase:firebase-auth-ktx")
  implementation ("com.google.firebase:firebase-firestore-ktx");
  implementation ("com.squareup.picasso:picasso:2.71828");
  implementation ("com.github.bumptech.glide:glide:4.12.0");
  annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
  implementation ("com.google.firebase:firebase-ml-vision:24.0.3")
  implementation ("com.google.firebase:firebase-ml-vision-face-model:20.0.1")
  implementation ("androidx.core:core-ktx:1.7.0")
  implementation ("androidx.activity:activity-ktx:1.4.0")
  implementation ("androidx.fragment:fragment-ktx:1.3.6")
  implementation ("androidx.core:core-ktx:1.7.0")
  implementation ("androidx.camera:camera-camera2:1.1.0")
  implementation ("androidx.camera:camera-lifecycle:1.1.0")
  implementation ("androidx.camera:camera-view:1.1.0")
  implementation ("androidx.camera:camera-extensions:1.1.0")
  implementation ("androidx.camera:camera-camera2:1.0.0")
  implementation ("androidx.camera:camera-lifecycle:1.0.0")
  implementation ("androidx.camera:camera-view:1.0.0-alpha30")

}