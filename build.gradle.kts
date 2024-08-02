import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.samordning"
version = "1"
description = "samordning-hendelse-api"

val logstashEncoderVersion = "7.2"
val springkafkaVersion="3.2.2"

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
    kotlin("plugin.jpa") version "1.9.0"
    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.2"
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

dependencyManagement{
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2022.0.3")
    }
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.auth0", "java-jwt", "3.8.3")
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.15.2")
    implementation("io.micrometer", "micrometer-core", "1.11.0")
    implementation("io.micrometer", "micrometer-registry-prometheus", "1.11.0")
    implementation("no.nav", "vault-jdbc" ,"1.3.10")
    implementation("net.logstash.logback", "logstash-logback-encoder", logstashEncoderVersion)
    implementation("org.postgresql", "postgresql", "42.6.0")
    implementation("org.hibernate.validator", "hibernate-validator", "8.0.0.Final")
    implementation("org.springframework.boot", "spring-boot-starter-web")
    implementation("org.springframework.boot", "spring-boot-starter-webflux")
    implementation("org.springframework.boot", "spring-boot-starter-data-jpa")
    implementation("org.springframework.boot", "spring-boot-starter-actuator")
    implementation("org.springframework.boot", "spring-boot-starter-validation")
    implementation("org.springframework.boot", "spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.security.oauth", "spring-security-oauth2", "2.5.2.RELEASE")
    implementation("org.springframework.kafka:spring-kafka:$springkafkaVersion")
    testImplementation("org.springframework.kafka:spring-kafka-test:$springkafkaVersion")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.springframework.security", "spring-security-test")
    testImplementation("io.zonky.test", "embedded-database-spring-test", "2.3.0")
    testImplementation("io.zonky.test", "embedded-postgres", "2.0.4")
    testImplementation("javax.el", "javax.el-api", "3.0.0")
    testImplementation("com.ninja-squad", "springmockk", "3.1.0")
    testImplementation("org.testcontainers", "postgresql", "1.15.1")
    testImplementation("org.testcontainers", "mockserver", "1.14.3")
    testImplementation("org.mock-server", "mockserver-client-java", "3.12")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")

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
