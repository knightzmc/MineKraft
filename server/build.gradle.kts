import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("krypton.common")
    id("com.github.johnrengelman.shadow") version Versions.SHADOW
    `java-library`
}

evaluationDependsOn(":krypton-api")

sourceSets.main {
    java {
        srcDir("src/main/kotlin")
        srcDir("src/generated/java")
        srcDir("src/generated/kotlin")
    }
}

repositories {
    maven("https://repo.velocitypowered.com/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":krypton-api"))
    implementation(project(":krypton-api").dependencyProject.sourceSets["ap"].output)
    implementation(platform("io.netty:netty-bom:${Versions.NETTY}"))

    // Extra Kotlin stuff for the JVM
    implementation(kotlin("stdlib-jdk7"))
    implementation(kotlin("stdlib-jdk8"))

    // Netty
    implementation("io.netty", "netty-buffer")
    implementation("io.netty", "netty-handler")
    implementation("io.netty", "netty-transport")

    // Netty native transport
    implementation("io.netty", "netty-transport-native-epoll")
    implementation("io.netty", "netty-transport-native-kqueue")
    implementation("io.netty.incubator", "netty-incubator-transport-native-io_uring", Versions.NETTY_IO_URING)

    // Event
    implementation("net.kyori", "event-method-asm", Versions.EVENT)
    implementation("org.ow2.asm", "asm", Versions.ASM)

    // Logging
    runtimeOnly("org.apache.logging.log4j", "log4j-core")
    implementation("net.minecrell", "terminalconsoleappender", Versions.TCA)
    runtimeOnly("org.jline", "jline-terminal-jansi", Versions.JANSI)

    // HTTP
    implementation("com.squareup.retrofit2", "retrofit", Versions.RETROFIT)
    implementation("com.squareup.retrofit2", "converter-gson", Versions.RETROFIT)
    implementation("com.squareup.okhttp3", "okhttp", Versions.OKHTTP)

    // Caching
    implementation("com.github.ben-manes.caffeine", "caffeine", Versions.CAFFEINE)
    implementation("it.unimi.dsi", "fastutil", Versions.FASTUTIL)

    // Miscellaneous
    implementation("org.kryptonmc", "datafixerupper", Versions.DFU) // Slight performance enhanced version, courtesy of Paper
    implementation("org.jglrxavpok.nbt", "Hephaistos", Versions.HEPHAISTOS) // Custom fork with nice improvements
    implementation("com.github.ajalt.clikt", "clikt", Versions.CLIKT)
    implementation("org.bstats", "bstats-base", Versions.BSTATS)
    implementation("com.velocitypowered", "velocity-native", Versions.VELOCITY_NATIVE)
    implementation("de.articdive", "articdata", Versions.MINECRAFT)
}

tasks {
    jar {
        manifest {
            attributes("Main-Class" to "org.kryptonmc.krypton.KryptonKt")
            attributes("Implementation-Title" to "Krypton")
            attributes("Implementation-Version" to project.version.toString())
            attributes("Implementation-Vendor" to "KryptonMC")
            attributes("Multi-Release" to "true")
        }
    }
    withType<ShadowJar> {
        val buildNumberProperty = System.getenv("BUILD_NUMBER")
        val buildNumber = if (buildNumberProperty != null) "-$buildNumberProperty" else ""
        archiveFileName.set("Krypton-${project.version}$buildNumber.jar")
        transform(Log4j2PluginsCacheFileTransformer::class.java)

        exclude("it/unimi/dsi/fastutil/booleans/**")
        exclude("it/unimi/dsi/fastutil/bytes/**")
        exclude("it/unimi/dsi/fastutil/chars/**")
        exclude("it/unimi/dsi/fastutil/doubles/**")
        exclude("it/unimi/dsi/fastutil/floats/**")
        exclude("it/unimi/dsi/fastutil/io/**")
        exclude("it/unimi/dsi/fastutil/objects/*Reference*")
        exclude("it/unimi/dsi/fastutil/shorts/**")
    }
    withType<ProcessResources> {
        filter<ReplaceTokens>("tokens" to mapOf("version" to project.version.toString()))
    }
}

pitest {
    excludedClasses.set(setOf(
        "org.kryptonmc.krypton.KryptonKt*",
        "org.kryptonmc.krypton.KryptonCLI*",
        "org.kryptonmc.krypton.KryptonServer*",
        "org.kryptonmc.krypton.NettyProcess*",
        "org.kryptonmc.krypton.WatchdogProcess*",
        "org.kryptonmc.krypton.auth.MojangUUIDSerializer*",
        "org.kryptonmc.krypton.auth.requests.SessionService*"
    ))
    excludedMethods.set(setOf("write\$Self"))
}

license {
    header.set(project.rootProject.resources.text.fromFile("HEADER.txt"))
    newLine.set(false)
    exclude(
        "**/*.properties",
        "**/*.conf",
        "**/*.json",
        // Velocity derivatives, with a special header
        "**/plugin/KryptonEventManager.kt",
        "**/plugin/KryptonPluginContainer.kt",
        "**/plugin/KryptonPluginManager.kt",
        "**/plugin/PluginClassLoader.kt",
        "**/plugin/dependencies.kt",
        "**/plugin/loader/KryptonPluginDescription.kt",
        "**/plugin/loader/PluginLoader.kt",
        "**/scheduling/KryptonScheduler.kt",
        "**/util/bytebufs.kt",
        // Couldn't find the license header to use for Starlight, or for the utility classes from Tuinity
        // If you're reading this and you know what the license header should be, feel free to open an issue
        "**/util/CoordinateUtils.java",
        "**/util/IntegerUtil.java",
        "**/util/WorldUtil.java",
        "**/ca/spottedleaf/starlight/*.java"
    )
}
