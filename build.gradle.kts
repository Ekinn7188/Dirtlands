//file:noinspection GroovyAssignabilityCheck
plugins {
    `java-library`
    id("nu.studer.jooq") version "6.0.1"
    id("org.flywaydb.flyway") version "8.0.5"
    id("io.papermc.paperweight.userdev") version "1.3.6"
    id("xyz.jpenilla.run-paper") version "1.0.6"
}

group = "net.dirtlands"
version = "1.0"


java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    mavenLocal()
//    maven {
//        name = "sonatype-oss-snapshots"
//        url = "https://oss.sonatype.org/content/repositories/snapshots/"
//    }
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.citizensnpcs.co/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    //minecraft
    paperDevBundle("1.18.2-R0.1-SNAPSHOT")
    compileOnly ("net.luckperms:api:5.4")
    compileOnly ("com.sk89q.worldguard:worldguard-bukkit:7.0.5")
    implementation ("org.reflections:reflections:0.10.2")
    compileOnly ("net.citizensnpcs:citizens-main:2.0.28-SNAPSHOT")
    implementation ("jeeper.utils:PaperPluginUtils:1.2")
    implementation ("net.wesjd:anvilgui:1.5.3-SNAPSHOT")

    //database
    implementation("org.jooq:jooq:3.16.6")
    compileOnly ("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation ("org.flywaydb:flyway-core:8.5.11")
    implementation ("ch.qos.logback:logback-classic:1.2.11")
    jooqGenerator ("org.xerial:sqlite-jdbc:3.36.0.3")
}

flyway {
    url = "jdbc:sqlite:${buildDir}/generate-source.db"
//    locations = ['classpath:db/migration']
}

jooq {
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.sqlite.JDBC"
                    url = "jdbc:sqlite:${buildDir}/generate-source.db"
                }
                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.sqlite.SQLiteDatabase"
                        excludes = "flyway_schema_history|sqlite_master|sqlite_sequence"
                    }
                    target.apply {
                        packageName = "dirtlands.db"
                    }
                }
            }
        }
    }
}



val cleanGenerateSourceDB by tasks.registering {
    delete(file("${buildDir}/generate-source.db"))
}

val cleanServer by tasks.registering {
    delete(files("${projectDir}/dirtlands.db"))
    delete(files("${projectDir}/run/usercache.json"))
    delete(files("${projectDir}/run/plugins/Dirtlands"))
    delete(files("${projectDir}/run/world/playerdata"))
}

val emptyConfig by tasks.registering {
    delete(
        fileTree(file("${projectDir}/run/plugins/Dirtlands")) {
            include("**.yml")
        }
    )
}

tasks {
    runServer {
        serverJar(file ("${projectDir}/run/server.jar"))
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.18")
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    flywayMigrate {
        dependsOn(cleanGenerateSourceDB)
    }

    named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
        dependsOn(flywayMigrate)
    }

    classes {
        dependsOn(named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq"))
    }


}

