import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val prod = (System.getenv("KLUTTER_ENABLE_PRODUCTION") ?: "FALSE") == "TRUE"

val properties = HashMap<String, String>().also { map ->
    File("${rootDir.absolutePath}/publish/" +
            "${if(prod) "_release" else "_develop"}.properties"
    ).normalize().also { file ->
        if (file.exists()) {
            file.forEachLine {
                val pair = it.split("=")
                if (pair.size == 2) {
                    map[pair[0]] = pair[1]
                }
            }
        }
    }
}

val libversion = (properties["gradle.plugin.version"] ?: "0.10.0")
    .also { println("VERSION GRADLE PLUGIN ==> $it") }

val repoUsername = (properties["repo.username"]
    ?: System.getenv("KLUTTER_PRIVATE_USERNAME"))
    ?: throw GradleException("missing repo.username")

val repoPassword = (properties["repo.password"]
    ?: System.getenv("KLUTTER_PRIVATE_PASSWORD"))
    ?: throw GradleException("missing repo.password")

val repoEndpoint = (properties["repo.url"]
    ?: System.getenv("KLUTTER_PRIVATE_URL"))
    ?: throw GradleException("missing repo.url")

val pluginGroupId = "dev.buijs.klutter"
val pluginArtifactId = "plugin"

plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.16.0"
    kotlin("jvm")
}

group = pluginGroupId
version = libversion

sourceSets {
    main {
        java {
            srcDirs("${projectDir.absolutePath}/src/main/kotlin")
        }
    }
}

pluginBundle {
    website = "https://buijs.dev/klutter/"
    vcsUrl = "https://github.com/buijs-dev/klutter"
    tags = listOf("klutter", "flutter", "kotlin", "multiplatform")
}

gradlePlugin {
    plugins {
        create("klutterGradlePlugin") {
            id = "dev.buijs.klutter.gradle"
            displayName = "Klutter plugin to generate boilerplate for connecting Flutter and Kotlin Multiplatform"
            description = "Klutter is a framework and tool set which uses Flutter to create the frontend and " +
                    "Kotlin Multiplatform for the backend. The connective layer is generated by the Klutter framework. " +
                    "This plugin contains all tasks needed to run a Klutter Project."
            implementationClass = "dev.buijs.klutter.plugins.gradle.KlutterGradlePlugin"
        }
    }
}

publishing {

    repositories {
        maven {
            url = uri(repoEndpoint)
            credentials {
                username = repoUsername
                password = repoPassword
            }
        }
    }

    publications {

        create<MavenPublication>("maven") {
            groupId = pluginGroupId
            artifactId = pluginArtifactId
            version = libversion

            artifact("$projectDir/build/libs/gradle-plugin-$libversion.jar")

            pom {
                name.set("Klutter: Gradle Plugin")
                description.set("Gradle plugin for the Klutter framework")
                url.set("https://buijs.dev/klutter/")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/buijs-dev/klutter/blob/main/LICENSE")
                    }
                }

                developers {
                    developer {
                        id.set("buijs-dev")
                        name.set("Gillian Buijs")
                        email.set("info@buijs.dev")
                    }
                }

                scm {
                    connection.set("git@github.com:buijs-dev/klutter.git")
                    developerConnection.set("git@github.com:buijs-dev/klutter.git")
                    url.set("https://github.com/buijs-dev/klutter")
                }
            }
        }
    }
}

repositories {

    google()
    gradlePluginPortal()
    mavenCentral()
    maven {
        url = uri(repoEndpoint)
        credentials {
            username = repoUsername
            password = repoPassword
        }
    }

}

dependencies {

    val annotationsVersion = (properties["annotations.version"] ?: "0.10.0")
        .also { println("VERSION ANNOTATIONS (GRADLE) ==> $it") }

    val coreVersion = (properties["core.version"] ?: "0.11.6")
        .also { println("VERSION CORE (GRADLE) ==> $it") }

    //Klutter
    implementation("dev.buijs.klutter:annotations-jvm:$annotationsVersion")
    implementation("dev.buijs.klutter:core:$coreVersion")

    //Kotlin
    implementation(kotlin("stdlib", "1.6.10"))
    implementation("org.jetbrains.kotlin:kotlin-compiler:1.6.10")

    //Jackson for XML
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.1")

    //Testing
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    //Gradle Testing
    testImplementation(gradleTestKit())
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.10")

    //Mocking
    testImplementation("org.mockito:mockito-core:4.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
}