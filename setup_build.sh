#!/bin/bash
set -e

# Configuration
PROJECT_DIR=$(pwd)
SETUP_DIR="$PROJECT_DIR/.build_setup"
JDK_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.10_7.tar.gz"
CMD_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
WRAPPER_JAR_URL="https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar"

echo "🚀 Starting 'Hacker Build' Sequence..."
mkdir -p "$SETUP_DIR"

# 1. Install Portable Java
if [ ! -d "$SETUP_DIR/jdk" ]; then
    echo "⬇️  Downloading JDK 17..."
    curl -L -o "$SETUP_DIR/jdk.tar.gz" "$JDK_URL"
    mkdir -p "$SETUP_DIR/jdk"
    tar -xzf "$SETUP_DIR/jdk.tar.gz" -C "$SETUP_DIR/jdk" --strip-components=1
fi
export JAVA_HOME="$SETUP_DIR/jdk"
export PATH="$JAVA_HOME/bin:$PATH"
echo "✅ Java Setup: $(java -version 2>&1 | head -n 1)"

# 2. Install Android SDK Command Line Tools
if [ ! -d "$SETUP_DIR/cmdline-tools" ]; then
    echo "⬇️  Downloading Android Command Line Tools..."
    curl -o "$SETUP_DIR/cmdline.zip" "$CMD_TOOLS_URL"
    mkdir -p "$SETUP_DIR/cmdline-tools/latest"
    unzip -q "$SETUP_DIR/cmdline.zip" -d "$SETUP_DIR/temp_cmd"
    mv "$SETUP_DIR/temp_cmd/cmdline-tools/"* "$SETUP_DIR/cmdline-tools/latest/"
    rm -rf "$SETUP_DIR/temp_cmd"
fi
export ANDROID_HOME="$SETUP_DIR/android-sdk"
export PATH="$SETUP_DIR/cmdline-tools/latest/bin:$PATH"

# 3. Accept Licenses & Install SDK Components
echo "📜 Accepting Licenses & Installing SDK..."
yes | sdkmanager --sdk_root="$ANDROID_HOME" --licenses > /dev/null 2>&1 || true
sdkmanager --sdk_root="$ANDROID_HOME" "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# 4. Setup Gradle (Full Distro)
echo "🔧 Setting up Gradle..."
GRADLE_ZIP="$SETUP_DIR/gradle.zip"
if [ ! -d "$SETUP_DIR/gradle" ]; then
    echo "⬇️  Downloading Gradle 8.2..."
    curl -L -o "$GRADLE_ZIP" "https://services.gradle.org/distributions/gradle-8.2-bin.zip"
    unzip -q "$GRADLE_ZIP" -d "$SETUP_DIR/gradle"
fi
GRADLE_BIN="$SETUP_DIR/gradle/gradle-8.2/bin/gradle"

# 4a. Create local.properties
echo "sdk.dir=$ANDROID_HOME" > "$PROJECT_DIR/local.properties"

# 4b. Generate Keystore (Release Mode)
KEYSTORE_PATH="app/keystore.jks"
if [ ! -f "$KEYSTORE_PATH" ]; then
    echo "🔐 Generating Release Keystore..."
    $JAVA_HOME/bin/keytool -genkeypair -v \
    -keystore "$KEYSTORE_PATH" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -alias key0 \
    -dname "CN=VibeNews, OU=Dev, O=VibeNews Corp, L=Cyber, S=Space, C=US" \
    -storepass password \
    -keypass password
fi

# 5. Build
echo "🏗️  Building RELEASE APK..."
chmod +x "$GRADLE_BIN"
"$GRADLE_BIN" clean
"$GRADLE_BIN" assembleRelease --stacktrace > build.log 2>&1 || true

echo "📜 Reading Build Log (Errors only)..."
grep -A 20 "FAILED" build.log || true
grep -A 20 "Caused by" build.log || true

echo "🎉 Build Complete! APK should be in app/build/outputs/apk/release/"
find app/build/outputs/apk/release/ -name "*.apk"
