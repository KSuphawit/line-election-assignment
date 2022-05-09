import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.7"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	// Flyway
	implementation("org.flywaydb:flyway-core")
	// Database
	implementation("com.microsoft.sqlserver:mssql-jdbc:7.0.0.jre8")
	// CSV
	implementation("com.opencsv:opencsv:5.6")
	// Web Socket
	implementation("org.springframework:spring-websocket:5.3.19")
	implementation("org.springframework:spring-messaging:5.3.19")
	// Oauth
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("io.mockk:mockk:1.12.3")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	setForkEvery(200) // prevent Java OOM error on over 1 GB of mem usage
	testLogging {
		// set options for log level LIFECYCLE
		events = setOf(
				org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
				org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
				org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
		)
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
		showExceptions = true
		showCauses = true
		showStackTraces = true

		afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
			if (desc.parent == null) { // will match the outermost suite
				val output =
						"|  Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)  |"
				val repeatLength = output.length
				val extraString = "".padEnd(repeatLength, '-')
				println(extraString + "\n" + output + "\n" + extraString)
			}
			null
		}))

	}
}
