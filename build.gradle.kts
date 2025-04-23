import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "no.nav.samordning"
version = "1"
description = "samordning-hendelse-api"
java.sourceCompatibility = JavaVersion.VERSION_21

val logstashEncoderVersion = "8.0"
val springkafkaVersion= "3.3.5"
val jakartaAnnotationApiVersion = "3.0.0"
val jakartaInjectApiVersion = "2.0.1"
val junitplatformVersion = "1.11.0"
val mockkVersion = "1.13.12"

plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    kotlin("plugin.jpa") version "2.1.20"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/navikt/maskinporten-validation")
        credentials {
            username = "token"
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.3")
    implementation("io.micrometer", "micrometer-core", "1.13.4")
    implementation("io.micrometer", "micrometer-registry-prometheus", "1.13.4")
    implementation("no.nav", "vault-jdbc" ,"1.3.10")
    implementation("no.nav.pensjonsamhandling", "maskinporten-validation-spring", "2.0.3")
    implementation("net.logstash.logback", "logstash-logback-encoder", logstashEncoderVersion)
    implementation("org.postgresql", "postgresql", "42.7.4")
    implementation("org.hibernate.validator", "hibernate-validator", "8.0.1.Final")
    implementation("org.springframework.boot", "spring-boot-starter-web")
    implementation("org.springframework.boot", "spring-boot-starter-webflux")
    implementation("org.springframework.boot", "spring-boot-starter-data-jpa")
    implementation("org.springframework.boot", "spring-boot-starter-actuator")
    implementation("org.springframework.boot", "spring-boot-starter-validation")
    implementation("org.springframework.boot", "spring-boot-starter-json")
    implementation("org.springframework", "spring-core")
    implementation("org.springframework.kafka:spring-kafka:$springkafkaVersion")
    implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationApiVersion")
    implementation("jakarta.inject:jakarta.inject-api:$jakartaInjectApiVersion")

    testImplementation("org.springframework.kafka:spring-kafka-test:$springkafkaVersion")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("io.zonky.test", "embedded-database-spring-test", "2.5.1")
    testImplementation("io.zonky.test", "embedded-postgres", "2.0.7")
    testImplementation("jakarta.el", "jakarta.el-api", "6.0.1")
    testImplementation("com.ninja-squad", "springmockk", "4.0.2")
    testImplementation("org.mock-server", "mockserver-spring-test-listener-no-dependencies", "5.15.0")
    //testImplementation("org.junit.platform:junit-platform-suite-api:$junitplatformVersion")
    testImplementation("no.nav.pensjonsamhandling", "maskinporten-validation-spring-test", "2.0.3")

}

tasks{
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    test {
        jvmArgs("-Dspring.aot.enabled=false")
        useJUnitPlatform()
        failFast = true
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}
