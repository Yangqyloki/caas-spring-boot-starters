@Library(['pb-pipeline-library', 'piper-lib']) _


def project = 'caas-spring-boot-starters'
def podLabel = sanitizeLabel(env.JOB_NAME, env.BUILD_NUMBER)
def moreInfoDefaultValue = "*More information*\nMore details can be found in <https://github.wdf.sap.corp/CaaS20/caas-spring-boot-starters|README.md>"
def parameters = [label: podLabel, tools: ['gradle', 'rabbitmq', 'postgres', 'kafka', 'zookeeper']]

caasPodTemplate(parameters) {
    node(podLabel) {
        container(name: 'postgres') {
            sh 'echo Probe to make sure postgres container is up  and running, otherwise fail the build right away'
        }

        milestone()

        gitHubCheckout()

        withSettingBuildStatus(errorHandler: { e -> notifyBuildFailure exception: e, slackChannel: '#upscale-montreal-private' }) {
            buildAndSonarQube()

            if (branch.isDevelop()) {

                stage('deploy snapshot') {
                    container(name: 'gradle') {
                        withCredentials([usernamePassword(credentialsId: 'sap-artifactory-rw', usernameVariable: 'username', passwordVariable: 'password')]) {
                            sh "gradle publish -Partifactory_username=${username} -Partifactory_password=${password} --info"
                        }
                    }
                }

                def releaseInfos = requestReleaseInfo(changesets(), moreInfoDefaultValue)
                if (releaseInfos.RELEASE_SCOPE) {
                    def projectVersion = getProjectVersion()
                    def releaseVersion = calculateReleaseVersion(projectVersion, releaseInfos.RELEASE_SCOPE)

                    gitFlowRelease(releaseVersion, project)

                    stage('sap artifactory deploy') {
                        sh 'git checkout .'
                        sh 'git clean -f -d'
                        sh 'git checkout master'
                        container(name: 'gradle') {
                            withCredentials([usernamePassword(credentialsId: 'sap-artifactory-rw', usernameVariable: 'username', passwordVariable: 'password')]) {
                                sh "gradle publish -Partifactory_username=${username} -Partifactory_password=${password} --info"
                            }
                        }
                    }

                    if (releaseInfos.SLACK_NOTIFICATION) {
                        releaseInfos["RELEASE_NOTE"] = releaseInfos["RELEASE_NOTE"].replaceAll(~/CAAS-((\d)+)/, '<https://jira.hybris.com/browse/CAAS-$1|CAAS-$1>')
                        def releaseMessage = buildFullSlackMessage(project, "released!", null, releaseInfos, releaseVersion)
                        notifySlack('#upscale-montreal-private', releaseMessage)
                    }
                }

            } else if (branch.isMaster()) {

                // keep vulnerability and license check up to date with released version
                vulnerabilityAndLicenseCheck(securityOption: '--init-script sec-compliance.gradle')

            }
        }
    }
}
