#!/usr/bin/env groovy
/*
 * GenerateSSHKey Gradle task class
 * Copyright © 2017-2018  Basil Peace
 *
 * This file is part of gradle-keygen-plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.fidata.gradle.tasks

import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import groovy.transform.CompileStatic
import org.fidata.gradle.KeygenExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task to generate SSH key
 */
@CompileStatic
class GenerateSSHKey extends DefaultTask {
  /*
   * CAVEAT:
   * JSch is not thread-safe.
   * See https://sourceforge.net/p/jsch/mailman/message/3862293/
   * <grv87 2018-12-02>
   */
  private static final ThreadLocal<JSch> JSCH = new ThreadLocal<JSch>() {
    @Override
    protected JSch initialValue() {
      new JSch()
    }
  }

  /**
   * Private key file
   */
  @OutputFile
  final RegularFileProperty privateKeyFile = newOutputFile()
  /**
   * Public key file
   */
  @OutputFile
  final Provider<RegularFile> publicKeyFile = project.layout.projectDirectory.file(project.provider { "${ privateKeyFile.get()?.asFile }.pub" })

  static final int DSA = KeyPair.DSA
  static final int RSA = KeyPair.RSA
  static final int ECDSA = KeyPair.ECDSA
  /**
   * Type of the key. See constants in {@link KeyPair} for valid values
   */
  @Optional
  @Input
  final Property<Integer> keyType = project.objects.property(Integer)
  /**
   * Size of the key
   */
  @Optional
  @Input
  final Property<Integer> keySize = project.objects.property(Integer)
  /**
   * Email to add to public key as comment
   */
  @Input
  String email

  GenerateSSHKey() {
    /*
     * WORKAROUND:
     * We have to reset standard type value to null manually
     * https://github.com/gradle/gradle/issues/6108
     * <grv87 2018-07-27>
     */
    keyType.set project.provider { project.extensions.getByType(KeygenExtension).keyType }
    keySize.set project.provider { project.extensions.getByType(KeygenExtension).keySize }
  }

  /**
   * Generate key
   */
  @TaskAction
  void generate() {
    didWork = !privateKeyFile.get().asFile.exists() && !publicKeyFile.get().asFile.exists()
    if (didWork) {
      KeyPair kpair = KeyPair.genKeyPair(JSCH.get(), keyType.get(), keySize.get())
      kpair.writePrivateKey(privateKeyFile.get().asFile.path)
      kpair.writePublicKey(publicKeyFile.get().asFile.path, email)
      kpair.dispose()
    } else {
      project.logger.warn('{}: private or public key file already exists and was not replaced', this.path)
    }
  }
}
