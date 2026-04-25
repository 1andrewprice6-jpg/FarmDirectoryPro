// Paste into app/build.gradle.kts dependencies {} block.

dependencies {
    // --- CameraX ---
    val camerax = "1.3.4"
    implementation("androidx.camera:camera-core:$camerax")
    implementation("androidx.camera:camera-camera2:$camerax")
    implementation("androidx.camera:camera-lifecycle:$camerax")
    implementation("androidx.camera:camera-view:$camerax")

    // --- ML Kit Text Recognition (on-device, Latin script) ---
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // --- Serialization (ParsedFields <-> Room JSON column) ---
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // --- Compose / lifecycle (should already be present) ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.compose.material:material-icons-extended:1.6.7")

    // --- Room (should already be present) ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}

// Ensure this is in the plugins block:
//   kotlin("plugin.serialization") version "2.0.0"
