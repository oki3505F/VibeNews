#!/bin/bash
set -e

# Configuration
PROJECT_DIR=$(pwd)
SETUP_DIR="$PROJECT_DIR/.build_setup"
JDK_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.10_7.tar.gz"
CMD_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
GH_CLI="./gh_cli"

# Colors for the Boss
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}🚀 Starting 'Hacker Build' v2.1 Overhaul...${NC}"

# Parse flags
RELEASE=false
if [[ "$1" == "--release" ]]; then
    RELEASE=true
    echo -e "${BLUE}📦 Release Mode Armed.${NC}"
fi

mkdir -p "$SETUP_DIR"

# 1. Install Portable Java
if [ ! -d "$SETUP_DIR/jdk" ]; then
    echo -e "⬇️  Downloading JDK 17..."
    curl -L -o "$SETUP_DIR/jdk.tar.gz" "$JDK_URL"
    mkdir -p "$SETUP_DIR/jdk"
    tar -xzf "$SETUP_DIR/jdk.tar.gz" -C "$SETUP_DIR/jdk" --strip-components=1
fi
export JAVA_HOME="$SETUP_DIR/jdk"
export PATH="$JAVA_HOME/bin:$PATH"

# 2. Install Android SDK Tools
if [ ! -d "$SETUP_DIR/cmdline-tools" ]; then
    echo -e "⬇️  Downloading Android Command Line Tools..."
    curl -o "$SETUP_DIR/cmdline.zip" "$CMD_TOOLS_URL"
    mkdir -p "$SETUP_DIR/cmdline-tools/latest"
    unzip -q "$SETUP_DIR/cmdline.zip" -d "$SETUP_DIR/temp_cmd"
    mv "$SETUP_DIR/temp_cmd/cmdline-tools/"* "$SETUP_DIR/cmdline-tools/latest/"
    rm -rf "$SETUP_DIR/temp_cmd"
fi
export ANDROID_HOME="$SETUP_DIR/android-sdk"
export PATH="$SETUP_DIR/cmdline-tools/latest/bin:$PATH"

# 3. Accept Licenses & Install SDK Components
echo -e "📜 Preparing SDK..."
yes | sdkmanager --sdk_root="$ANDROID_HOME" --licenses > /dev/null 2>&1 || true
sdkmanager --sdk_root="$ANDROID_HOME" "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# 4. Setup Gradle
if [ ! -d "$SETUP_DIR/gradle" ]; then
    echo -e "⬇️  Downloading Gradle 8.2..."
    curl -L -o "$SETUP_DIR/gradle.zip" "https://services.gradle.org/distributions/gradle-8.2-bin.zip"
    unzip -q "$SETUP_DIR/gradle.zip" -d "$SETUP_DIR/gradle"
fi
GRADLE_BIN="$SETUP_DIR/gradle/gradle-8.2/bin/gradle"
echo "sdk.dir=$ANDROID_HOME" > "$PROJECT_DIR/local.properties"

# Keystore
KEYSTORE_PATH="app/keystore.jks"
if [ ! -f "$KEYSTORE_PATH" ]; then
    echo -e "🔐 Generating Keystore..."
    $JAVA_HOME/bin/keytool -genkeypair -v -keystore "$KEYSTORE_PATH" -keyalg RSA -keysize 2048 -validity 10000 -alias key0 -dname "CN=VibeNews, OU=Dev, O=VibeNews Corp, L=Cyber, S=Space, C=US" -storepass password -keypass password
fi

# 5. Build
VERSION_NAME=$(grep "versionName =" app/build.gradle.kts | cut -d'"' -f2)
echo -e "${GREEN}🏗️  Building VibeNews v$VERSION_NAME...${NC}"
chmod +x "$GRADLE_BIN"
"$GRADLE_BIN" clean assembleRelease --no-daemon

# 6. Success Check
APK_PATH=$(find app/build/outputs/apk/release/ -name "*.apk" | head -n 1)
if [ -f "$APK_PATH" ]; then
    echo -e "${GREEN}✅ Build Success: $APK_PATH${NC}"
    
    if [ "$RELEASE" = true ]; then
        echo -e "${CYAN}🚀 Triggering Automated Release Sequence...${NC}"
        git add .
        git commit -m "Release: v$VERSION_NAME"
        git push origin master
        
        $GH_CLI release create "v$VERSION_NAME" "$APK_PATH" --title "VibeNews v$VERSION_NAME" --notes "Automated release of v$VERSION_NAME."
        echo -e "${GREEN}🔥 Release v$VERSION_NAME is LIVE!${NC}"
    fi
else
    echo -e "${RED}❌ Build FAILED.${NC}"
    exit 1
fi
