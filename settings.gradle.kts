rootProject.name = "semantic-core"

pluginManagement {
    resolutionStrategy {
        repositories {
            gradlePluginPortal()
            maven("https://dl.bintray.com/kotlin/kotlin-eap")
            maven("https://dl.bintray.com/kotlin/kotlin-dev")
        }
        eachPlugin {
            if (requested.id.id == "org.springframework.boot") {
                useModule("org.springframework.boot:spring-boot-gradle-plugin:${requested.version}")
            }
        }
    }
}

include("shared")