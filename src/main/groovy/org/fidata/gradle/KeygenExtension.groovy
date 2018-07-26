#!/usr/bin/env groovy
/*
 * KeygenConvention class
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

import com.jcraft.jsch.KeyPair

class KeygenExtension {
  static final int DSA = KeyPair.DSA
  static final int RSA = KeyPair.RSA
  static final int ECDSA = KeyPair.ECDSA
  /**
   * Type of the key. See constants in {@link KeyPair} for valid values
   */
  int keyType = RSA

  /**
   * Type of the key
   */
  int keySize = 4096
}
