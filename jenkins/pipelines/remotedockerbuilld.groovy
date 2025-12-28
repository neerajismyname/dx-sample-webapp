// Jenkinsfile
pipeline {
    agent any
    
    // Define environment variables for the build
    environment {
        DOCKER_HOST_CREDENTIALS_ID = 'dockerinstance' // MUST match the ID of your SSH credential in Jenkins
        DOCKER_HOST_IP = '192.168.122.75'
        USERNAME = 'jenkinsuser'
        IMAGE_NAME = 'fastapi-sample-app'
        IMAGE_TAG = '1.0'
        DOCKER_REGISTRY_NAME = 'fastapi-sample-app'
        DOCKER_CREDS = 'dockerpatjenkins'
    }

    stages {
        stage('Checkout Code') {
            steps{
                git url: 'https://github.com/neerajismyname/dx-sample-webapp.git',
                branch: 'dev',
                credentialsId: 'GitAccessTokenDX'

                sh 'echo "Listing project files"'
                sh 'ls -al' 
            }   
        }

        stage('Build on Remote Docker Host') {
            steps {
                withCredentials([usernamePassword(credentialsId: env.DOCKER_CREDS, passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
                    sshagent(credentials: ['dockerinstance']) { // Uses the secure SSH key credential
                        sh """
                        # 1. Create target directory on remote host
                        ssh $USERNAME@$DOCKER_HOST_IP "mkdir -p /tmp/workspace/${JOB_NAME}"

                        # 2. Transfer code to the remote machine using rsync (reliable file transfer)
                        rsync -az --delete ./ $USERNAME@$DOCKER_HOST_IP:/tmp/workspace/${JOB_NAME}

                        # 3. Execute Docker build command securely via SSH
                        ssh $USERNAME@$DOCKER_HOST_IP << 'EOF'
                            cd /tmp/workspace/${JOB_NAME}
                            cd containerization/fastapi-docker-app
                            sudo docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}
                            sudo docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                            sudo docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_USER}/${DOCKER_REGISTRY_NAME}:${IMAGE_TAG}
                            sudo docker push ${DOCKER_USER}/${DOCKER_REGISTRY_NAME}:${IMAGE_TAG}
                            sudo docker logout
                            echo done!
EOF
                        """
                    }
                }
            }
        }
        
        stage('Deploy Container') {
            steps {
                // Use the sshagent block again for the deployment step
                sshagent(credentials: [env.DOCKER_HOST_CREDENTIALS_ID]) {
                    sh """
                    # 5. SSH back in to run the container
                    ssh $USERNAME@${DOCKER_HOST_IP} << 'EOF'
                        # Stop and remove any existing container
                        # docker stop my-fastapi-container || true
                        # docker rm my-fastapi-container || true

                        # Run the new container, mapping port 8000 on the host to port 80 in the container
                        #sudo docker run -d --name my-fastapi-container -p 8000:80 ${IMAGE_NAME}:${IMAGE_TAG}
                        echo "Deploy Container Empty Stage"
EOF
                    """
                }
            }
        }
    }
}