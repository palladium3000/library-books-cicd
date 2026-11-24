/*
 * Jenkinsfile проекта library-books-cicd
 * ЛР4 ИППРПО, Гришкевич А.А., гр. 472302
 *
 * Основа - базовый Jenkinsfile из методички (Checkout, Toolchain, Build and Verify, Generate Reports, post).
 * Вариант 6 (Jenkins): после успешной сборки проверяется, что JAR действительно создан.
 * Вариант 6 (GitHub Actions) сначала отлажен здесь: новая сборка отменяет предыдущую
 * (abortPrevious) - аналог concurrency с cancel-in-progress в ci.yml.
 */
pipeline {
    agent { label 'windows-java21-agent' }

    parameters {
        // для негативного сценария можно указать неправильное имя файла
        string(name: 'EXPECTED_JAR', defaultValue: 'library-books-cicd-1.0.0.jar', description: 'Имя JAR, который должен появиться в target')
    }

    options {
        timestamps()
        // новая сборка останавливает ещё идущую старую (как cancel-in-progress)
        disableConcurrentBuilds(abortPrevious: true)
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                bat 'git log -1 --oneline'
            }
        }

        stage('Toolchain') {
            steps {
                bat 'java -version'
                bat 'mvn -version'
                bat 'git --version'
            }
        }

        stage('Build and Verify') {
            steps {
                bat 'mvn -B -ntp clean verify'
            }
        }

        // вариант 6: проверка наличия JAR
        stage('Check JAR') {
            steps {
                script {
                    def jar = "target/${params.EXPECTED_JAR}"
                    if (!fileExists(jar)) {
                        bat 'dir target\\*.jar'
                        error("JAR не найден: ${jar}")
                    }
                    // размер файла в байтах через PowerShell
                    def size = powershell(returnStdout: true, script: "(Get-Item '${jar}').Length").trim()
                    echo "JAR найден: ${jar}, размер ${size} байт"
                    if (size.toLong() < 1024) {
                        error("JAR подозрительно маленький (${size} байт)")
                    }
                }
            }
        }

        stage('Generate Reports') {
            steps {
                bat 'mvn -B -ntp surefire-report:report-only'
                bat 'mvn -B -ntp jacoco:report'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true,
                  testResults: 'target/surefire-reports/TEST-*.xml,target/failsafe-reports/TEST-*.xml'
            archiveArtifacts artifacts: 'target/surefire-reports/**,target/failsafe-reports/**,target/site/**',
                             allowEmptyArchive: true
        }
        success {
            // сюда доходим только если JAR прошёл проверку
            archiveArtifacts artifacts: "target/${params.EXPECTED_JAR}", fingerprint: true
        }
        failure {
            echo 'Сборка упала - смотри Console Output'
        }
        aborted {
            echo 'Сборка отменена (запущена более новая)'
        }
    }
}
