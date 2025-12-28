
pipeline {
  agent any

  stages {
    stage('Clone Repo'){
            steps{
                git url: 'https://github.com/neerajismyname/dx-k8s-manifests.git',
                branch: 'dev',
                credentialsId: 'GitAccessTokenDX'

                sh 'echo "Listing project files"'
                sh 'ls -al'
            }
        }

    stage('Access K8s') {
      steps {
        withKubeConfig([credentialsId: 'kubeconfig-jenkins']) {
          sh 'kubectl get pods -n jenkins-deploy'
        }
      }
    }

    stage('Deploy nginx') {
      steps {
        withKubeConfig([credentialsId: 'kubeconfig-jenkins']) {
          sh 'kubectl -n jenkins-deploy apply -f nginx-pod.yaml'
        }
      }
    }
  }
}