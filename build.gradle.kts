plugins {
    id("java")
    id("dev.architectury.loom") version("1.11-SNAPSHOT")
    id("architectury-plugin") version("3.4-SNAPSHOT")
    kotlin("jvm") version("2.2.20")
}


group = "io.github.jaypixl"
version = "0.3.3"

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
    maven("https://maven.neoforged.net/releases")
    maven("https://cursemaven.com")
}

dependencies {
    minecraft("net.minecraft:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    neoForge("net.neoforged:neoforge:21.1.230") // prev 21.1.182

    modImplementation("com.cobblemon:neoforge:1.8.0+1.21.1")
    //Needed for cobblemon
    implementation("thedarkcolour:kotlinforforge-neoforge:5.10.0") {
        exclude("net.neoforged.fancymodloader", "loader")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    modImplementation("curse.maven:yawp-663276:8034329")
    modImplementation("curse.maven:lightmans-currency-472521:8366260")
    //modImplementation("curse.maven:jobs-remastered-916985:8125195")
    //modImplementation("curse.maven:arc-lib-883593:8125132")
    //modImplementation("curse.maven:item-restrictions-887774:8125177")
    //modImplementation("curse.maven:carved-wood-1335174:8437529")
    modImplementation("curse.maven:cobblemon-occupied-pokeballs-reforged-1561082:8859871")

    modImplementation("curse.maven:forge-config-api-port-547434:7213611")
    //modImplementation("curse.maven:architectury-api-419699:5786327")

    //runtimeOnly("curse.maven:ui-lib-933200:8125054")
    //runtimeOnly("curse.maven:yaml-config-1128669:8125096")
    //runtimeOnly("curse.maven:knot-1470052:7987037")
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