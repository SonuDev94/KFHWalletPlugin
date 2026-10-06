const fs = require("fs");
const path = require("path");

module.exports = function (ctx) {
    const projectRoot = ctx.opts.projectRoot;

    // Paths
    const androidPlatform = path.join(projectRoot, "platforms", "android");
    const appGradle = path.join(androidPlatform, "app", "build.gradle");

    // Your exact FINALIZATION FILES path
    // const FINALIZATION_DIR = "/Users/sonukumar/Documents/KFH/Source-KMPSDK1.11.1-251113/wallet-agent-white-label-app-release-1.1.11-kmp-ebc/FINALIZATION FILES";
// flatDir {
    // dirs "${FINALIZATION_DIR}"
// }
    if (!fs.existsSync(appGradle)) {
        console.log("⚠️ Android app/build.gradle not found. Skipping Finalizer injection.");
        return;
    }

    console.log("🔧 Injecting Finalizer plugin into app/build.gradle...");

    let gradle = fs.readFileSync(appGradle, "utf8");

    // Prevent duplication
    if (gradle.includes("ai.digital.finalize-android")) {
        console.log("ℹ️ Finalizer plugin already applied. Skipping.");
        return;
    }

    const inject = `
/* --- Auto-injected Finalizer Plugin (Cordova Hook) --- */

buildscript {
    repositories {
        maven { url "https://jcenter.bintray.com" }
        flatDir {
            dirs 'libs'
        }
    }
    dependencies {
        // classpath "ai.digital.protect-android:ai.digital.protect-android:1.0.0"
        classpath "ai.digital.harden-android:ai.digital.harden-android:1.0.0"
    }
}
    
apply plugin: "ai.digital.finalize-android"

def finalizerPath = "$projectDir/libs/finalizer"
// def finalizerPath = "/EBC/finalizer"
def finFilePath = "app/libs/aap.fin"

finalizeAndroid {
    buildVariants {
        android.applicationVariants.configureEach { variant ->
            create("\${variant.name}") {
                disabled = false
                finalizeAndroid = finalizerPath
                finalizationFiles(finFilePath)
            }
        }
    }
}

/* --- End Finalizer Plugin Injection --- */
`;

    // Insert at TOP of file
    // gradle = inject + "\n" + gradle;
    gradle = gradle + "\n" + inject;

    fs.writeFileSync(appGradle, gradle, "utf8");

    console.log("✅ Finalizer plugin successfully injected into app/build.gradle");
};