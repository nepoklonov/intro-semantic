plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

val kotlinVersion = project.property("kotlin.version") as String
val kotlinxSerializationVersion = project.property("kotlinx.serialization.version") as String
val kotlinxCoroutinesVersion = project.property("kotlinx.coroutines.version") as String
val ktorVersion = project.property("ktor.version") as String
val kotlinWrappersSuffix = project.property("kotlin.wrappers.suffix") as String

val logbackVersion = project.property("logback.version") as String
val exposedVersion = project.property("exposed.version") as String
val h2Version = project.property("h2.version") as String
val janusgraphVersion = project.property("janusgraph.version") as String
val springBootVersion = project.property("springboot.version") as String
val owlApiVersion = project.property("owl-api.version") as String

kotlin {
    jvm()
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
        }
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
                implementation("org.jetbrains:kotlin-css:1.0.0-$kotlinWrappersSuffix")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }


        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
                implementation("org.janusgraph:janusgraph-core:$janusgraphVersion")
                implementation("org.apache.tinkerpop:gremlin-driver:3.4.8")
                implementation("org.apache.tinkerpop:gremlin-server:3.4.8")
                implementation("org.janusgraph:janusgraph-cql:$janusgraphVersion")
                implementation("org.janusgraph:janusgraph-es:$janusgraphVersion")
//                implementation("org.janusgraph:janusgraph-cassandra:$janusgraphVersion")
//                хз почему, но строчка выше -- ломает тесты
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-client-apache:$ktorVersion")
                implementation("io.ktor:ktor-jackson:$ktorVersion")
                implementation("io.ktor:ktor-html-builder:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")
                implementation("org.jetbrains:kotlin-css:1.0.0-$kotlinWrappersSuffix")
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("com.h2database:h2:$h2Version")
                implementation("org.reflections:reflections:0.9.10")
                implementation("org.apache.poi:poi:5.0.0")
                implementation("org.apache.poi:poi-ooxml:5.0.0")
                implementation(kotlin("script-runtime"))
                implementation("com.zaxxer:HikariCP:4.0.3")
                implementation("org.mindrot:jbcrypt:0.4")
                implementation("io.ktor:ktor-auth:$ktorVersion")
                implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
                implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
                implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
                implementation("net.sourceforge.owlapi:owlapi-distribution:$owlApiVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-gson:$ktorVersion")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
    }
}