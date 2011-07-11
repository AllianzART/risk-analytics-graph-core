grails.project.dependency.resolution = {
    inherits "global" // inherit Grails' default dependencies
    log "warn"

    repositories {
        grailsHome()
        grailsCentral()
    }

    //even though this plugin does not need anything from this repo, it has to be added for the deploy script to check existing plugins
    mavenRepo "https://build.intuitive-collaboration.com/maven/plugins/"

    plugins {
        runtime ":background-thread:1.3"
        runtime ":hibernate:1.3.7"
        runtime ":joda-time:0.5"
        runtime ":maven-publisher:0.7.5"
        runtime ":quartz:0.4.2"
        runtime ":spring-security-core:1.1.2"
        runtime ":tomcat:1.3.7"

        test ":code-coverage:1.2.2"

        if (appName == "RiskAnalyticsGraphCore") {
            runtime "org.pillarone:risk-analytics-core:1.4-ALPHA-2.5.5-kti"
            //runtime ("org.pillarone:risk-analytics-property-casualty:1.4-ALPHA-1.2-kti") { transitive = false }
        }
    }
}

grails.project.dependency.distribution = {
    String passPhrase = ""
    String scpUrl = ""
    try {
        Properties properties = new Properties()
        properties.load(new File("${userHome}/deployInfo.properties").newInputStream())

        passPhrase = properties.get("passPhrase")
        scpUrl = properties.get("url")
    } catch (Throwable t) {
    }
    remoteRepository(id: "pillarone", url: scpUrl) {
        authentication username: 'root', privateKey: "${userHome.absolutePath}/.ssh/id_rsa", passphrase: passPhrase
    }
}

coverage {
    exclusions = [
            'models/**',
            '**/*Test*',
            '**/com/energizedwork/grails/plugins/jodatime/**',
            '**/grails/util/**',
            '**/org/codehaus/**',
            '**/org/grails/**',
            '**GrailsPlugin**',
            '**TagLib**'
    ]

}

//grails.plugin.location.'risk-analytics-core' = "../RiskAnalyticsCore"
//grails.plugin.location.'risk-analytics-pc' = "../RiskAnalyticsPC"
