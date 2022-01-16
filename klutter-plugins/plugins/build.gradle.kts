import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "dev.buijs.klutter"
version = "0.6.9"
//version = "2022-pre-alpha-1"

plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.16.0"
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
    kotlin("jvm")
}

pluginBundle {
    website = "https://buijs.dev/klutter/"
    vcsUrl = "https://github.com/buijs-dev/klutter"
    tags = listOf("klutter", "flutter", "kotlin", "multiplatform")
}

gradlePlugin {
    plugins {
        create("klutterPlugin") {
            id = "dev.buijs.klutter.gradle"
            displayName = "Klutter plugin to generate boilerplate for connecting Flutter and Kotlin Multiplatform"
            description = "Klutter is a framework and tool set which uses Flutter to create the frontend and " +
                    "Kotlin Multiplatform for the backend. The connective layer is generated by the Klutter framework. " +
                    "Klutter combines industry best practices for everything from app design to CICD into a single cohesive framework. " +
                    "This plugin contains all tasks needed to run a Klutter Project"
            implementationClass = "dev.buijs.klutter.plugins.gradle.KlutterPlugin"
        }
    }
}

publishing {
    val file = File("${rootDir.absolutePath}/dev.properties").normalize()

    if(!file.exists()) {
        throw GradleException("missing dev.properties file in ${file.absolutePath}")
    }

    val properties = HashMap<String, String>()

    file.forEachLine {
        val pair = it.split("=")
        if(pair.size == 2){
            properties[pair[0]] = pair[1]
        }
    }

    val user = properties["private.repo.username"]
        ?:throw GradleException("missing private.repo.username in dev.properties")

    val pass = properties["private.repo.password"]
        ?:throw GradleException("missing private.repo.password in dev.properties")

    val endpoint = properties["private.repo.url"]
        ?:throw GradleException("missing private.repo.url in dev.properties")

    repositories {
        maven {
            url = uri(endpoint)
            credentials {
                username = user
                password = pass
            }
        }
    }

}

dependencies {
    implementation("dev.buijs.klutter:annotations-jvm:0.5.0")
    implementation("dev.buijs.klutter:core:0.6.12")
    implementation("dev.buijs.klutter:annotations-processor:0.6.13")

    implementation(kotlin("stdlib", "1.6.10"))
    implementation("org.jetbrains.kotlin:kotlin-compiler:1.6.10")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.1")

    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(gradleTestKit())
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.10")

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
    kotlinOptions.jvmTarget = "1.8"
}
