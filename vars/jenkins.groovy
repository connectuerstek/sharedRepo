def call() {
       pipeline {
       agent any

    stages {
        stage('build') {
            steps {
                bat '"C:\\Program Files\\apache-maven-3.9.4\\bin\\mvn" clean install'
          
                }
        }  
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarserver') {
                    bat '"C:\\Program Files\\apache-maven-3.9.4\\bin\\mvn" clean package sonar:sonar'
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
                    def server = Artifactory.server('artifactory') // Use the ID defined in Jenkins Credential Manager
                    def rtMaven = Artifactory.newMavenBuild()
                    def buildInfo
                    // Configure Artifactory server details
                    rtMaven.tool = 'Maven'
                    
                    rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
                    //rtMaven.resolver releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
                    
                    buildInfo = rtMaven.run pom: 'pom.xml', goals: 'clean install'
                    server.publishBuildInfo buildInfo
                    //rtMaven.deployer.deployArtifacts(buildInfo) // Deploy artifacts to Artifactory
                    //rtMaven.deployer.deployBuildInfo(buildInfo) // Publish build information to Artifactory
                }
            }
        }
    }
}
}
