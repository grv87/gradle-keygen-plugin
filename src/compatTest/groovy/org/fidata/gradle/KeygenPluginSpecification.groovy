#!/usr/bin/env groovy
/*
 * Specification for org.fidata.keygen Gradle plugin
 * Copyright © 2018  Basil Peace
 *
 * This file is part of gradle-keygen-plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.fidata.gradle

import com.jcraft.jsch.KeyPairRSA
import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.BuildResult
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import java.nio.file.Files
import org.apache.commons.io.FileUtils

/**
 * Specification for {@link org.fidata.gradle.KeygenPlugin} class
 */
class KeygenPluginSpecification extends Specification {
  // fields
  boolean success = false

  final File testProjectDir = Files.createTempDirectory('compatTest').toFile()

  File buildFile = new File(testProjectDir, 'build.gradle')
  File settingsFile = new File(testProjectDir, 'settings.gradle')
  File propertiesFile = new File(testProjectDir, 'gradle.properties')

  // fixture methods

  // run before the first feature method
  // void setupSpec() { }

  // run before every feature method
  void setup() {
    buildFile << '''\
      plugins {
        id 'org.fidata.keygen'
      }
    '''.stripIndent()
  }

  // run after every feature method
  void cleanup() {
    /*
     * WORKAROUND:
     * Jenkins doesn't set CI environment variable
     * https://issues.jenkins-ci.org/browse/JENKINS-36707
     * <grv87 2018-06-27>
     */
    if (success || System.getenv().with { containsKey('CI') || containsKey('JENKINS_URL') }) {
      FileUtils.deleteDirectory(testProjectDir)
    }
  }

  // run after the last feature method
  // void cleanupSpec() { }

  // feature methods
  void 'generates ssh key by default'() {
    given: 'project build file'
    buildFile << '''\
      keygen {
        keyType = RSA
        keySize = 4096
      }

      task('generateSSHKey', type: GenerateSSHKeyTask) {
        privateKeyFile = new File(buildDir, 'ssh_key')
        email = 'test@example.com'
      }
    '''.stripIndent()

    when: 'generateSSHKey task is run'
    build('generateSSHKey')

    then: 'private key file is generated'
    File privateKeyFile = new File(testProjectDir, 'build/ssh_key')
    privateKeyFile.exists()

    and: 'public key file is generated'
    File publicKeyFile = new File(testProjectDir, 'build/ssh_key.pub')
    publicKeyFile.exists()

    when: 'generated key is loaded'
    JSch jSch = new JSch()
    KeyPair kpair = KeyPair.load(jSch, privateKeyFile.bytes, publicKeyFile.bytes)

    then: 'no exception is thrown'
    noExceptionThrown()

    and: 'key type is RSA'
    kpair.keyType == KeyPair.RSA

    and: 'key length is 4096'
    ((KeyPairRSA)kpair).keySize == 4096

    and: 'public key comment is set'
    kpair.publicKeyComment == 'test@example.com'

    (success = true) != null
  }

  // helper methods
  protected BuildResult build(String... arguments) {
    GradleRunner.create()
      .withGradleVersion(System.getProperty('compat.gradle.version'))
      .withProjectDir(testProjectDir)
      .withArguments([*arguments, '--full-stacktrace', '--refresh-dependencies', '--debug'])
      .withPluginClasspath()
      .build()
  }
}
