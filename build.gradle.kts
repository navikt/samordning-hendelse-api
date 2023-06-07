import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.samordning"
version = "1"
description = "samordning-hendelse-api"

val logstashEncoderVersion = "7.2"

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21"
    kotlin("plugin.jpa") version "1.8.21"
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
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
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2021.0.3")
    }
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.auth0","java-jwt","3.8.3")
    implementation("io.micrometer","micrometer-core","1.11.0")
    implementation("io.micrometer","micrometer-registry-prometheus","1.11.0")
    implementation("no.nav", "vault-jdbc" ,"1.3.10")
    implementation("net.logstash.logback", "logstash-logback-encoder", logstashEncoderVersion)
    implementation("org.postgresql","postgresql","42.6.0")
    implementation("org.hibernate.validator","hibernate-validator","6.0.10.Final")
    implementation("org.springframework.boot","spring-boot-starter-web")
    implementation("org.springframework.boot","spring-boot-starter-webflux")
    implementation("org.springframework.boot","spring-boot-starter-data-jpa")
    implementation("org.springframework.boot","spring-boot-starter-actuator")
    implementation("org.springframework.boot","spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.security.oauth","spring-security-oauth2","2.5.0.RELEASE")
    implementation("org.springframework.cloud","spring-cloud-starter-bootstrap")
    implementation("org.springframework.cloud","spring-cloud-vault-config-databases")
    runtimeOnly("org.springframework.cloud","spring-cloud-starter-vault-config")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.springframework.boot","spring-boot-starter-test")
    testImplementation("org.springframework.security","spring-security-test")
    testImplementation("com.h2database","h2","2.1.214")
    testImplementation("com.ninja-squad","springmockk","3.1.0")
    testImplementation("org.testcontainers","postgresql","1.15.1")
    testImplementation("org.testcontainers","mockserver","1.14.3")
    testImplementation("org.mock-server","mockserver-client-java","3.12")
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
