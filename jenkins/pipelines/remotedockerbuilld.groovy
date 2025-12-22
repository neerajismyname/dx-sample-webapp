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
                        # docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                        ls -al
EOF
                    """
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
                        # docker run -d --name my-fastapi-container -p 8000:80 ${IMAGE_NAME}:${IMAGE_TAG}
                        echo "Deploy Container Empty Stage"
EOF
                    """
                }
            }
        }
    }
}