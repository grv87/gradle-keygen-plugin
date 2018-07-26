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
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair

/**
 * Specification for {@link org.fidata.gradle.KeygenPlugin} class
 */
class KeygenPluginSpecification extends Specification {
  // fields
  final File testProjectDir = Files.createTempDirectory('compatTest').toFile()

  File buildFile = new File(testProjectDir, 'build.gradle')
  File settingsFile = new File(testProjectDir, 'settings.gradle')
  File propertiesFile = new File(testProjectDir, 'gradle.properties')

  // fixture methods

  // run before the first feature method
  // void setupSpec() { }

  // run before every feature method
  void setup() {
    project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    project.apply plugin: 'org.fidata.keygen'
  }

  // run after every feature method
  // void cleanup() { }

  // run after the last feature method
  // void cleanupSpec() { }

  // feature methods
  void 'generates ssh key by default'() {
    given: 'project build file'
    project.keygen {
      keyType = RSA
      keySize = 4096
    }
      
    project.task('generateSSHKey', type: GenerateSSHKeyTask) {
      privateKeyFile = new File(buildDir, 'ssh_key')
      email = 'test@example.com'
    }

    when: 'generateSSHKey task is run'
    build('generateSSHKey')

    then: 'private key file is generated'
    File privateKeyFile = new File(testProjectDir, 'ssh_key')
    privateKeyFile.exists()

    and: 'public key file is generated'
    File publicKeyFile = new File(testProjectDir, 'ssh_key.pub')
    publicKeyFile.exists()

    when: 'generated key is loaded'
    JSch jSch = new JSch()
    KeyPair kpair = KeyPair.load(JSch, privateKeyFile.bytes, publicKeyFile.bytes)

    then: 'no exception is thrown'
    noExceptionThrown()

    and: 'key type is RSA'
    kpair.keyType == KeyPair.RSA

    and: 'key length is 4096'
    privateKeyFile.length() == 4096

    and: 'public key comment is set'
    kpair.publicKeyComment == 'test@example.com'

    (success = true) != null
  }

  // helper methods
  protected BuildResult build(String... arguments) {
    GradleRunner.create()
      .withGradleVersion(System.getProperty('compat.gradle.version'))
      .withProjectDir(testProjectDir)
      .withArguments([*arguments, '--stacktrace', '--refresh-dependencies'])
      .withPluginClasspath()
      .build()
  }
}
