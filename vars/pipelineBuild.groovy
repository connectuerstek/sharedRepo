def call(body) {
    def pipelineParams = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        agent any

        stages {
            stage('Build Application') {
                steps {
                    script {
                        if (pipelineParams.buildtype == 'frontend') {
                            // Create a zip file in the current directory
                            bat "cd frontend && zip -r ${pipelineParams.zipFileName} ."
                        } else if (pipelineParams.buildtype == 'backend') {
                            // Use the Maven executable with the provided goals
                            bat "\"C:\\Program Files\\apache-maven-3.9.4\\bin\\mvn\" ${pipelineParams.mavengoals}"
                        } else {
                            error 'Invalid buildtype provided. Use "frontend" or "backend".'
                        }
                    }
                }
	      stage('Publish to Aritifactory') {
		      script {
			      def serverId = 'artifactory'
			      if (pipelineParams.buildtype == 'frontend') {
				      def targetPath = "npm-local/com/ui/test/${pipelineParams.zipFileName}"
				      rtUpload (
					      serverId: serverId,
					      spec: """{
	                                          "files": [
					              {
		                                         "pattern": "${pipelineParams.zipFileName}",
                                                         "target": "${targetPath}"
							 }
	                                           ]
					      }"""
				      )
			      }
			      else if (pipelineParams.buildtype == 'backend') {
				      def server = Artifactory.server(serverId)
				      def rtMaven =Artifactory.newMavenBuild()

				      rtMaven.tool = 'Maven'
				      rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server

				      def buildInfo = rtMaven.run pom: 'pom.xml', goals: '${pipelineParams.mavengoals}'
				      server.publishBuildInfo buildInfo
				      
			      }
		      }
		      
	      }
            }
        }
    }
}
