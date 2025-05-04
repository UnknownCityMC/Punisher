plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.8"
    id("xyz.jpenilla.run-velocity") version "2.3.0"
    kotlin("jvm") version "1.8.22"
}

group = "de.unknowncity"
version = "0.1.0"

val shadeBasePath = "${group}.${rootProject.name.lowercase()}.libs."

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    implementation("cloud.commandframework", "cloud-velocity", "1.8.4")
    implementation("cloud.commandframework", "cloud-minecraft-extras", "1.8.4")
    implementation("de.chojo.sadu", "sadu", "1.4.1")
    implementation("org.spongepowered", "configurate-yaml", "4.1.2")
    implementation("org.spongepowered", "configurate-hocon", "4.1.2")

    implementation("redis.clients", "jedis", "5.1.0")

    implementation("org.mariadb.jdbc", "mariadb-java-client", "3.3.2")

    annotationProcessor("com.velocitypowered", "velocity-api", "3.3.0-SNAPSHOT")

    compileOnly("com.velocitypowered", "velocity-api", "3.3.0-SNAPSHOT")
    compileOnly("me.clip", "placeholderapi", "2.11.5")


    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter", "junit-jupiter")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks{
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    runVelocity {
        velocityVersion("3.2.0-SNAPSHOT")
    }

    shadowJar {
        relocate("org.spongepowered", shadeBasePath + "configurate")
        relocate("de.chojo.sadu", shadeBasePath + "sadu")
        relocate("cloud.commandframework", shadeBasePath + "cloud")
        relocate("redis.clients", shadeBasePath + "redis")


    }

    compileJava {
        options.encoding = "UTF-8"
    }

    jar {
        archiveBaseName.set(rootProject.name)
        archiveVersion.set(rootProject.version.toString())
    }

    register<Copy>("copyToServer") {
        val path = System.getenv("SERVER_DIR")
        if (path.toString().isEmpty()) {
            println("No SERVER_DIR env variable set")
            return@register
        }
        from(shadowJar)
        destinationDir = File(path.toString())
    }
}

