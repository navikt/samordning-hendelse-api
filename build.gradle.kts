import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.samordning"
version = "1"
description = "samordning-hendelse-api"

val logbackClassicVersion = "1.2.3"
val logstashLogbackEncoder = "5.2"
val log4jVersion = "2.13.3"
val maskinportenVersion = "0.1.7"
val slf4jVersion = "1.7.32"
val springCloudVersion = "3.0.4"

plugins {
    kotlin("jvm") version "1.6.0-RC"
    kotlin("plugin.noarg") version "1.6.0-RC"
    kotlin("plugin.spring") version "1.6.0-RC"
    id("org.springframework.boot") version "2.5.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.github.com/navikt/maskinporten-validation") {
        credentials {
            username = "token"
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("ch.qos.logback", "logback-classic", logbackClassicVersion)
    implementation("com.vladmihalcea", "hibernate-types-52", "2.9.10")
    implementation("io.micrometer", "micrometer-registry-prometheus", "1.5.1")
    implementation("net.logstash.logback", "logstash-logback-encoder", logstashLogbackEncoder)
    implementation("no.nav.pensjonsamhandling", "maskinporten-validation-spring", maskinportenVersion)
    implementation("org.postgresql", "postgresql", "42.2.12")
    implementation("org.hibernate.validator", "hibernate-validator", "6.0.10.Final")
    implementation("org.apache.logging.log4j", "log4j-api", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-core", log4jVersion)
    implementation("org.slf4j", "slf4j-api", slf4jVersion)
    implementation("org.springframework.boot", "spring-boot-starter-web")
    implementation("org.springframework.boot", "spring-boot-starter-jdbc")
    implementation("org.springframework.boot", "spring-boot-starter-webflux")
    implementation("org.springframework.boot", "spring-boot-starter-actuator")
    implementation("org.springframework.cloud", "spring-cloud-starter-bootstrap", springCloudVersion)
    implementation("org.springframework.cloud", "spring-cloud-starter-vault-config", springCloudVersion)
    implementation("org.springframework.cloud", "spring-cloud-vault-config-databases", springCloudVersion)
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.springframework.boot", "spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("com.h2database", "h2", "1.4.200")
    testImplementation("com.ninja-squad", "springmockk", "3.0.1")
    testImplementation("no.nav.pensjonsamhandling", "maskinporten-validation-spring-test", maskinportenVersion)
    testImplementation("org.testcontainers", "postgresql", "1.15.1")
    testImplementation("org.testcontainers", "mockserver", "1.14.3")
    testImplementation("org.mock-server", "mockserver-client-java", "3.12")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}

noArg {
    annotation("no.nav.samordning.hendelser.hendelse.NoArg")
}