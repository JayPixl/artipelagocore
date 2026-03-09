plugins {
    id("java")
    id("dev.architectury.loom") version("1.11-SNAPSHOT")
    id("architectury-plugin") version("3.4-SNAPSHOT")
    kotlin("jvm") version("2.2.20")
}


group = "io.github.jaypixl"
version = "0.1.0"

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    silentMojangMappingsLicense()
}

repositories {
    mavenCentral()
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://maven.neoforged.net")
    maven("https://cursemaven.com")
}

dependencies {
    minecraft("net.minecraft:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    neoForge("net.neoforged:neoforge:21.1.197") // prev 21.1.182

    modImplementation("com.cobblemon:neoforge:1.7.3+1.21.1")
    //Needed for cobblemon
    implementation("thedarkcolour:kotlinforforge-neoforge:5.10.0") {
        exclude("net.neoforged.fancymodloader", "loader")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    modImplementation("curse.maven:yawp-663276:7166805")
    modImplementation("curse.maven:lightmans-currency-472521:7697352")
    modImplementation("curse.maven:jobs-remastered-916985:7239650")
    modImplementation("curse.maven:arc-lib-883593:6205985")
    modImplementation("curse.maven:item-restrictions-887774:6206057")

    modImplementation("curse.maven:forge-config-api-port-547434:7213611")
    modImplementation("curse.maven:architectury-api-419699:5786327")

    runtimeOnly("curse.maven:ui-lib-933200:6114803")
    runtimeOnly("curse.maven:yaml-config-1128669:6122649")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(project.properties)
    }
}