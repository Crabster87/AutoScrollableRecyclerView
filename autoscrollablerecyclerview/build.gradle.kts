plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "io.github.crabster87.autoscrollablerecyclerview"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.material)

}

// Deploy

android {
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

version = properties["VERSION_NAME"].toString()
description = properties["POM_DESCRIPTION"].toString()

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = properties["GROUP"].toString()
            artifactId = properties["POM_ARTIFACT_ID"].toString()

            pom {
                name.set(project.properties["POM_NAME"].toString())
                description.set(project.properties["POM_DESCRIPTION"].toString())
                url.set("https://github.com/Crabster87/AutoScrollableRecyclerView")
                issueManagement {
                    url.set("https://github.com/Crabster87/AutoScrollableRecyclerView/issues")
                }

                scm {
                    url.set("https://github.com/Crabster87/AutoScrollableRecyclerView")
                    connection.set("scm:git://github.com/Crabster87/AutoScrollableRecyclerView.git")
                    developerConnection.set("scm:git://github.com/Crabster87/AutoScrollableRecyclerView.git")
                }

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("Crabster87")
                        name.set("Alexey Rudakov")
                        email.set("iar876@yandex.ru")
                    }
                }

                afterEvaluate {
                    from(components["release"])
                }
            }
        }
    }
    repositories {
        maven {
            setUrl(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}