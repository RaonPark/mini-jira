buildscript {
    repositories { mavenCentral() }
    dependencies {
        // Flyway Gradle 플러그인은 프로젝트 classpath 가 아니라 build classpath 를 쓴다.
        // Flyway 10 부터 DB 별 모듈이 분리되어서, 여기에 따로 넣어주지 않으면
        // "No Flyway database plugin found to handle ..." 로 실패한다.
        classpath("org.flywaydb:flyway-database-postgresql:13.3.0")
    }
}

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.3.21"
    id("org.flywaydb.flyway") version "13.3.0"
    id("org.jooq.jooq-codegen-gradle") version "3.21.7"
}

group = "org.raonpark"
version = "0.0.1-SNAPSHOT"
description = "backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

flyway {
    url = "jdbc:postgresql://localhost:55432/minijira"
    user = "minijira"
    password = "minijira"
    locations = arrayOf("filesystem:${projectDir}/src/main/resources/db/migration")
}

jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = "jdbc:postgresql://localhost:55432/minijira"
            user = "minijira"
            password = "minijira"
        }
        generator {
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
                excludes = "flyway_schema_history"

                forcedTypes {
                    forcedType {
                        userType = "org.raonpark.backend.task.enums.TaskStatus"
                        isEnumConverter = true
                        includeExpression = "public\\.task\\.status"
                    }
                    forcedType {
                        userType = "org.raonpark.backend.task.enums.TaskPriority"
                        isEnumConverter = true
                        includeExpression = "public\\.task\\.priority"
                    }
                }
            }
            target {
                packageName = "org.raonpark.backend.jooq"
            }
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // kotlin-logging : SLF4J 파사드. Boot 가 이미 붙여둔 logback 위에서 그대로 동작한다.
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.14")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")
    runtimeOnly("org.postgresql:postgresql")
    jooqCodegen("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// jOOQ 가 생성한 코드는 build/generated-sources/jooq 에 떨어지지만
// 플러그인이 소스셋에 자동 등록해 주지는 않는다 (compileJava 가 NO-SOURCE 로 뜬다).
// 이 블록이 있어야 코드에서 org.raonpark.backend.jooq.Tables 를 import 할 수 있다.
sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generated-sources/jooq"))
    }
}
