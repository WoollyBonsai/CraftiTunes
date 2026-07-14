# Craftitunes Mod Development Setup

This document records the steps taken to set up the Fabric Modding environment for Minecraft 1.21.8. It is designed to be reproducible.

## Objective
- Set up a Fabric modding environment for Minecraft 1.21.8.
- Ensure the setup does not conflict with existing Android development environments on the same system.

## Steps Taken

### 1. Initialize the Project Template
We used the official Fabric Example Mod template.
```bash
git clone https://github.com/FabricMC/fabric-example-mod.git .
```

### 2. Configure for Minecraft 1.21.8
The Fabric Example Mod repository maintains branches for different Minecraft versions. We checked out the `1.21.8` branch to get the exact dependencies and versions required.
```bash
git checkout 1.21.8
```

### 3. Isolate Java Version (Avoid Android Dev Conflicts)
Minecraft 1.21.8 requires **Java 21**. Since Android development may rely on different Java versions (e.g., Java 17, 11, or 8) depending on the project, we need to ensure this Minecraft mod uses Java 21 without changing system-wide environment variables (like `JAVA_HOME`).

To achieve this, we configured Gradle to use **Java Toolchains**. This feature tells Gradle to automatically detect or download the specified JDK version and use it for compilation and execution, ignoring the system default.

**Changes made to `build.gradle`:**
We replaced `sourceCompatibility` and `targetCompatibility` with a `toolchain` declaration:
```gradle
java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

**Changes made to `settings.gradle`:**
We added the Foojay Toolchain Resolver plugin to allow Gradle to automatically download Java 21 if it's missing on the system:
```gradle
plugins {
	id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0'
}
```

### 4. Verification
We verified the setup by running the Gradle build and launching the client:
```bash
./gradlew build
./gradlew runClient
```
