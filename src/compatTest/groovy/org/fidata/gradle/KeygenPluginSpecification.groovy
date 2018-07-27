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

import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.BuildResult
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import com.jcraft.jsch.KeyPairRSA
import com.jcraft.jsch.KeyPairDSA
import java.nio.file.Files
import org.apache.commons.io.FileUtils

/**
 * Specification for {@link org.fidata.gradle.KeygenPlugin} class
 */
class KeygenPluginSpecification extends Specification {
  // fields
  boolean success = false

  final File testProjectDir = Files.createTempDirectory('compatTest').toFile()
  final File testProjectDir2 = File.createTempDir('compatTest', null)

  final File buildDir = new File(testProjectDir, 'build')

  File buildFile = new File(testProjectDir, 'build.gradle')

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
  void 'generates ssh key with project-wide settings'() {
    given: 'build file'
    buildFile << '''\
      keygen {
        keyType = RSA
        keySize = 2048
      }

      task('generateSSHKey', type: GenerateSSHKeyTask) {
        privateKeyFile = new File(buildDir, 'ssh_key')
        email = 'test@example.com'
      }
    '''.stripIndent()

    when: 'generateSSHKey task is run'
    build('generateSSHKey')

    then: 'private key file is generated'
    File privateKeyFile = new File(buildDir, 'ssh_key')
    privateKeyFile.exists()

    and: 'public key file is generated'
    File publicKeyFile = new File(buildDir, 'ssh_key.pub')
    publicKeyFile.exists()

    when: 'generated key is loaded'
    JSch jSch = new JSch()
    KeyPair kpair = KeyPair.load(jSch, privateKeyFile.bytes, publicKeyFile.bytes)

    then: 'no exception is thrown'
    noExceptionThrown()

    and: 'key type equals to requested'
    kpair.keyType == KeyPair.RSA

    and: 'key length equals to requested'
    ((KeyPairRSA)kpair).keySize == 2048

    and: 'public key comment is set'
    kpair.publicKeyComment == 'test@example.com'

    (success = true) != null
  }

  // feature methods
  void 'generates ssh key with per-task settings'() {
    given: 'build file'
    buildFile << '''\
      keygen {
        keyType = RSA
        keySize = 4096
      }

      task('generateSSHKey', type: GenerateSSHKeyTask) {
        privateKeyFile = new File(buildDir, 'ssh_key')
        keyType = DSA
        keySize = 2048
        email = 'test@example.com'
      }
    '''.stripIndent()

    when: 'generateSSHKey task is run'
    build('generateSSHKey')

    then: 'key type equals to requested'
    File privateKeyFile = new File(buildDir, 'ssh_key')
    File publicKeyFile = new File(buildDir, 'ssh_key.pub')
    JSch jSch = new JSch()
    KeyPair kpair = KeyPair.load(jSch, privateKeyFile.bytes, publicKeyFile.bytes)
    kpair.keyType == KeyPair.DSA

    and: 'key length equals to requested'
    ((KeyPairDSA)kpair).keySize == 2048

    (success = true) != null
  }

  // feature methods
  void 'don\'t override existing key files'() {
    given: 'build file'
    buildFile << '''\
      task('generateSSHKey', type: GenerateSSHKeyTask) {
        privateKeyFile = new File(buildDir, 'ssh_key')
        email = 'test@example.com'
      }
    '''.stripIndent()

    and: 'private key file exists'
    buildDir.mkdir()
    String dummyKey = 'Dummy key'
    File privateKeyFile = new File(buildDir, 'ssh_key')
    privateKeyFile.text = dummyKey
    File publicKeyFile = new File(buildDir, 'ssh_key.pub')
    publicKeyFile.text = dummyKey

    when: 'generateSSHKey task is run'
    build('generateSSHKey')

    then: 'private key file is not overriden'
    privateKeyFile.text == dummyKey

    and: 'public key file is not overriden'
    publicKeyFile.text == dummyKey

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
