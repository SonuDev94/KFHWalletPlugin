const fs = require("fs");
const path = require("path");

module.exports = function (ctx) {
  const gradleConfigPath = path.join(ctx.opts.projectRoot, "platforms/android/cdv-gradle-config.json");

  if (fs.existsSync(gradleConfigPath)) {
    const json = JSON.parse(fs.readFileSync(gradleConfigPath, "utf8"));
    json.IS_GRADLE_PLUGIN_KOTLIN_ENABLED = true;
    json.IS_GRADLE_PLUGIN_GOOGLE_SERVICES_ENABLED = true;
    fs.writeFileSync(gradleConfigPath, JSON.stringify(json, null, 2), "utf8");
    console.log("✅ Updated cdv-gradle-config.json with Kotlin + Google Services enabled");
  } else {
    console.warn("⚠ cdv-gradle-config.json not found, skipping update.");
  }
};