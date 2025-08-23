plugins {
	java
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.company"
version = "0.0.1-SNAPSHOT"
description = "Dota Admin Backend"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-graphql")
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	// Use Spring MVC (servlet) instead of WebFlux to match SecurityFilterChain (HttpSecurity)
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
	// HTTP Client for KakaoTalk API
	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	// Email support
	implementation("org.springframework.boot:spring-boot-starter-mail")
	runtimeOnly("org.postgresql:postgresql")
	// In-memory DB for local/dev default profile to allow app startup without external DB
	runtimeOnly("com.h2database:h2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.graphql:spring-graphql-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// JSP support
	implementation("org.apache.tomcat.embed:tomcat-embed-jasper")
	implementation("jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api")
	implementation("org.glassfish.web:jakarta.servlet.jsp.jstl")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Ensure consistent UTF-8 source encoding (prevents BOM / platform default issues)
tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

// Strip UTF-8 BOM from any accidentally committed source files before compile (defensive)
val stripBom by tasks.registering {
	doLast {
		val targets = fileTree("src") {
			include("**/*.java", "**/*.kt")
		}
		targets.forEach { f ->
			val bytes = f.readBytes()
			if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
				f.writeBytes(bytes.copyOfRange(3, bytes.size))
				println("[stripBom] Stripped BOM: ${f.relativeTo(project.projectDir)}")
			}
		}
	}
}

tasks.named("compileJava") { dependsOn(stripBom) }
