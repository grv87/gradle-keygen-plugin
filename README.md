gradle-keygen-plugin
====================

This plugin provides Gradle ability to generate SSH keys.

It uses [JSch](http://www.jcraft.com/jsch/) Java library,
no installation of extra tools is required.

## Usage
```

plugins {
  id 'org.fidata.keygen' version '1.1.0'
}

keygen {
  keyType = RSA
  keySize = 4096
}

task('generateSSHKey', type: GenerateSSHKey) {
  privateKeyFile = new File(buildDir, 'ssh_key')
  email = 'test@example.com'
}
```

This task, when run, would produce `ssh_key` and `ssh_key.pub` files
in `build` directory.

`keyType` and `keySize` properties can be set per-task via its properties.
Otherwise, project-wide values from `keygen` extension are used.

List of supported `keyType` values is the same as for JSch:
*   `RSA`
*   `DSA`
*   `ECDSA`

Note that generated keys are stored in plain-text.
This could become a security issue and should be used cautiously.

Since keys can't be restored, to prevent key loss task will run
**only if either private or public key file doesn't exist**.
If you want to regenerate key (including when you change desired key
properties) you should clean them manually beforehand.

Other types of keys (non-SSH) could be supported in the future.


### Compatibility

*   Gradle >= 4.4
*   Built and tested with JDK 8


------------------------------------------------------------------------
Copyright © 2018  Basil Peace

This file is part of gradle-keygen-plugin.

Copying and distribution of this file, with or without modification,
are permitted in any medium without royalty provided the copyright
notice and this notice are preserved.  This file is offered as-is,
without any warranty.
