pipeline {
    agent {
        label 'master'
    }
    stages {
        stage('Build') {            
            steps {                
                echo 'Building'
                sh 'ls -l'
                sh './gradlew x-apm-app:assembleDebug'
            }        
        }        
        stage('Test') {            
            steps {                
                echo 'Testing'            
            }        
        }
        stage('Deploy - Staging') {            
            steps {                
                echo './deploy staging'                
                echo './run-smoke-tests'            
            }        
        }        
        stage('Sanity check') {            
            steps {                
                input "Does the staging environment look ok?"            
            }        
        }        
        stage('Deploy - Production') {            
            steps {                
                echo './deploy production'
                sh 'tree x-apm-app/build/outputs/apk/ '
            }        
        }    
    }
 
    post {        
        always {            
            echo 'One way or another, I have finished'            
            deleteDir() /* clean up our workspace */        
        }        
        success {            
            echo 'I succeeeded!'        
        }        
        unstable {            
            echo 'I am unstable :/'        
        }        
        failure {            
            echo 'I failed :('        
        }        
        changed {            
            echo 'Things were different before...'        
        }    
    }
}

