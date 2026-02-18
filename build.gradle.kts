import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "no.nav.samordning"
version = "1"
description = "samordning-hendelse-api"
java.sourceCompatibility = JavaVersion.VERSION_21

val logstashEncoderVersion = "9.0"
val jakartaAnnotationApiVersion = "3.0.0"
val jakartaInjectApiVersion = "2.0.1"
val mockkVersion = "1.14.7"

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    kotlin("plugin.jpa") version "2.3.0"
    id("org.springframework.boot") version "4.0.2"
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
    implementation("tools.jackson.module", "jackson-module-kotlin", "3.0.3")
    implementation("tools.jackson.datatype", "jackson-datatype-jsr310", "3.0.0-rc2")
    implementation("io.micrometer", "micrometer-core", "1.16.2")
    implementation("io.micrometer", "micrometer-registry-prometheus", "1.16.2")
    implementation("no.nav", "vault-jdbc" ,"1.3.10")
    implementation("no.nav.pensjonsamhandling", "maskinporten-validation-spring", "3.1.0")
    implementation("net.logstash.logback", "logstash-logback-encoder", logstashEncoderVersion)
    implementation("org.postgresql", "postgresql", "42.7.10")
    implementation("org.hibernate.validator", "hibernate-validator", "9.1.0.Final")
    implementation("org.springframework.boot", "spring-boot-starter-web")
    implementation("org.springframework.boot", "spring-boot-starter-webflux")
    implementation("org.springframework.boot", "spring-boot-starter-data-jpa")
    implementation("org.springframework.boot", "spring-boot-starter-actuator")
    implementation("org.springframework.boot", "spring-boot-starter-validation")
    implementation("org.springframework.boot", "spring-boot-starter-flyway")
    implementation("org.springframework.boot", "spring-boot-starter-jackson")
    implementation("org.springframework.boot", "spring-boot-starter-kafka")
    implementation("org.springframework", "spring-core")
    implementation("jakarta.annotation", "jakarta.annotation-api", jakartaAnnotationApiVersion)
    implementation("jakarta.inject", "jakarta.inject-api", jakartaInjectApiVersion)
    implementation("org.flywaydb", "flyway-core", "12.0.1")
    implementation("org.flywaydb", "flyway-database-postgresql", "12.0.1")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.springframework.boot", "spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot", "spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot", "spring-boot-starter-kafka-test")
    testImplementation("io.zonky.test", "embedded-database-spring-test", "2.7.1")
    testImplementation("io.zonky.test", "embedded-postgres", "2.2.0")
    testImplementation("jakarta.el", "jakarta.el-api", "6.0.1")
    testImplementation("com.ninja-squad", "springmockk", "5.0.1")
    testImplementation("org.mock-server", "mockserver-spring-test-listener-no-dependencies", "5.15.0")
    testImplementation("no.nav.pensjonsamhandling", "maskinporten-validation-spring-test", "3.1.0")

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
