#!/usr/bin/env groovy
/*
 * GenerateSSHKeyTask Gradle task class
 * Copyright © 2017-2018  Basil Peace
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
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task to generate SSH key
 */
class GenerateSSHKeyTask extends DefaultTask {
  private final static JSch JSCH = new JSch()

  private File privateKeyFile
  private File publicKeyFile

  /**
   * @return private key file
   */
  @OutputFile
  File getPrivateKeyFile() { privateKeyFile }
  /**
   * @param privateKeyFile private key file
   */
  void setPrivateKeyFile(File privateKeyFile) {
    this.privateKeyFile = privateKeyFile
    this.publicKeyFile = project.file("${ privateKeyFile }.pub")
  }
  /**
   * @return public key file
   */
  @OutputFile
  File getPublicKeyFile() { publicKeyFile }

  /**
   * Type of the key. See constants in {@link KeyPair} for valid values
   */
  @Input
  int keyType = KeyPair.RSA
  /**
   * Type of the key
   */
  @Input
  int keySize = 4096
  /**
   * Email to add to public key as comment
   */
  @Input
  String email

  GenerateSSHKeyTask() {
    onlyIf {
      !privateKeyFile.exists() || !publicKeyFile.exists()
    }
  }

  /**
   * Generate key
   */
  @TaskAction
  void generate() {
    KeyPair kpair = KeyPair.genKeyPair(JSCH, keyType, keySize)
    kpair.writePrivateKey(privateKeyFile.path)
    kpair.writePublicKey(publicKeyFile.path, email)
    kpair.dispose()
  }
}
