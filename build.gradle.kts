import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.samordning"
version = "1"
description = "samordning-hendelse-api"

val logbackClassicVersion = "1.2.11"
val logstashLogbackEncoder = "7.2"

plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.spring") version "1.7.0"
    id("org.springframework.boot") version "2.7.2"
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
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
    implementation("com.auth0","java-jwt","3.8.3")
    implementation("com.vladmihalcea","hibernate-types-52","2.9.10")
    implementation("io.micrometer","micrometer-registry-prometheus","1.5.1")
    implementation("org.postgresql","postgresql","42.2.12")
    implementation("org.hibernate.validator","hibernate-validator","6.0.10.Final")
    implementation("org.springframework.boot","spring-boot-starter-web")
    implementation("org.springframework.boot","spring-boot-starter-jdbc")
    implementation("org.springframework.boot","spring-boot-starter-webflux")
    implementation("org.springframework.boot","spring-boot-starter-actuator")
    implementation("org.springframework.boot","spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.security.oauth","spring-security-oauth2","2.5.0.RELEASE")
    implementation("org.springframework.cloud","spring-cloud-vault-config-databases","2.2.2.RELEASE")
    runtimeOnly("org.springframework.cloud","spring-cloud-starter-vault-config","2.1.3.RELEASE")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.springframework.boot","spring-boot-starter-test")
    testImplementation("com.h2database","h2","1.4.200")
    testImplementation("org.testcontainers","postgresql","1.15.1")
    testImplementation("org.testcontainers","mockserver","1.14.3")
    testImplementation("org.mock-server","mockserver-client-java","3.12")

    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoder")
}

tasks{
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}
