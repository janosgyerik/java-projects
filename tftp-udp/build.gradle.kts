plugins {
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
}


subprojects {
    repositories {
        jcenter()
    }

    apply(plugin = "io.spring.dependency-management")

    dependencyManagement {
        dependencies {
            dependency("org.slf4j:slf4j-simple:1.6.1")
            dependency("org.junit.jupiter:junit-jupiter-api:5.6.0")
            dependency("org.junit.jupiter:junit-jupiter-engine:5.6.0")
        }
    }
}
