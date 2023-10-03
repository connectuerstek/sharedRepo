def call(body) {
    List<String> artifactsList = []
    def pipelineParams = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        agent any

        stages {
            stage('Build Application') {
                steps {
                    // // mvn command but use inputs
                    // sh "mvn ${pipelineParams.mavengoals}"
                  script {
                    if (pipelineParams.buildtype == 'frontend'){
                      bat "cd && zip -r ${pipelineParams.zipFileName} ."
                      }
                    else if (pipelineParams.buildtype == 'backend'){
                      bat ""C:\\Program Files\\apache-maven-3.9.4\\bin\\mvn" ${pipelineParams.mavengoals}"
                    }
                  }
					
                }
            }
        }
    }
}
