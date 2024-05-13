plugins {
    kotlin("jvm")
    id("klutter")
    id("groovy")
    id("java-library")
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

sourceSets {
    main {
        java {
            srcDirs("${projectDir.absolutePath}/src/main/kotlin")
        }
    }

    test {
        java {
            srcDirs("${projectDir.absolutePath}/src/test/kotlin")
        }
    }
}

dependencies {
    //Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
    implementation("org.jetbrains.kotlin:kotlin-compiler:1.7.10")

    //Jackson for XML
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.16.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.16.1")

    //Logging
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("io.github.microutils:kotlin-logging:3.0.5")

    // Gradle
    api(gradleApi())
    api(gradleTestKit())

    // Spock
    api("org.codehaus.groovy:groovy-all:3.0.17")
    api("org.spockframework:spock-core:2.2-M1-groovy-3.0")

    // Mockingjay
    api("org.mockito:mockito-core:4.6.1")
    api("org.mockito.kotlin:mockito-kotlin:4.0.0")

    // Kotlin Test
    @Suppress("GradleDependency") // 30-07-2022 newest 3.4.2 throws exceptions
    api("io.kotlintest:kotlintest-runner-junit5:3.3.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}