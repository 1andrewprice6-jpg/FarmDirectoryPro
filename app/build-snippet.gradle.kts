// Append to app/build.gradle.kts dependencies {} block.
// (CameraX, ML Kit, Room, kotlinx-serialization should already be present
//  from the vision base bundle.)

dependencies {
    // WorkManager (for CaptureSyncWorker)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
