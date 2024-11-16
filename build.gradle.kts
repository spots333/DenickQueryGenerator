plugins {
    id("java")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    //Gson for better json parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // https://mvnrepository.com/artifact/jakarta.servlet/jakarta.servlet-api
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    // https://mvnrepository.com/artifact/org.eclipse.jetty/jetty-server
    implementation("org.eclipse.jetty:jetty-server:12.0.15")

    // https://mvnrepository.com/artifact/org.eclipse.jetty/jetty-servlet
    implementation("org.eclipse.jetty:jetty-servlet:11.0.24")


}

tasks.test {
    useJUnitPlatform()
}

application.mainClass = "org.example.Main" //used for shadowjar plugin