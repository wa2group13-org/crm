import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import java.util.*

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("kapt") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("idea")
    id("org.springframework.boot") version "3.2.4"
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
    id("org.openapi.generator") version "7.5.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.asciidoctor.jvm.convert") version "4.0.2"
}

group = "it.polito.wa2.g13"
version = "1.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("com.github.therapi:therapi-runtime-javadoc:0.15.0")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    runtimeOnly("org.postgresql:postgresql")

    kapt("com.github.therapi:therapi-runtime-javadoc-scribe:0.15.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.reactivestreams:reactive-streams")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<BootBuildImage>("bootBuildImage") {
    imageName = "${System.getenv("DOCKER_USERNAME")}/${project.name}:${project.version}"
    docker {
        publishRegistry {
            username = System.getenv("DOCKER_USERNAME")
            password = System.getenv("DOCKER_PASSWORD")
        }
    }
}

abstract class ProjectVersion : DefaultTask() {
    @TaskAction
    fun action() {
        print(project.version)
    }
}

tasks.register<ProjectVersion>("projectVersion")

// Store the version of the current project in the project.properties file
// in the build directory
tasks.withType<ProcessResources> {
    doLast {
        val propertiesFile = file("${layout.buildDirectory.get()}/resources/main/project.properties")
        propertiesFile.parentFile.mkdirs()
        val properties = Properties().apply {
            this.setProperty("project.version", project.version.toString())
        }
        println(properties.toString())

        propertiesFile.writer().use { properties.store(it, null) }
    }
}

tasks.generateOpenApiDocs {
    waitTimeInSeconds = 60 * 3
}

// Make the output generation much easier by using an ENV for specifying the
// generatorName
tasks.openApiGenerate {
    dependsOn(tasks.generateOpenApiDocs)
    generatorName = System.getenv("GENERATOR_NAME")
    inputSpec = "${layout.buildDirectory.get()}/openapi.json"
    outputDir = "${layout.buildDirectory.get()}/openapi-gen/${System.getenv("GENERATOR_NAME")}"
}

tasks.asciidoctor {
    setSourceDir(file("${layout.buildDirectory.get()}/openapi-gen/asciidoc"))
    setOutputDir(file("${layout.buildDirectory.get()}/docs"))
}