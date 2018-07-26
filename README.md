gradle-keygen-plugin
====================

This plugin provides Gradle ability to generate SSH keys.

Usage:
```
plugins {
  id 'org.fidata.keygen'
}

keygen {
  keyType = RSA
  keySize = 4096
}

task('generateSSHKey', type: GenerateSSHKeyTask) {
  privateKeyFile = new File(buildDir, 'ssh_key')
  email = 'test@example.com'
}
```

This task, when run, would produce `ssh_key` and `ssh_key.pub` files
in `build` directory.

`keyType` and `keySize` properties can be set per-task via its properties.
Otherwise, project-wide values from `keygen` extension are used.





Other types of keys could be supported in the future.


------------------------------------------------------------------------
Copyright © 2018  Basil Peace

This file is part of gradle-keygen-plugin.

Copying and distribution of this file, with or without modification,
are permitted in any medium without royalty provided the copyright
notice and this notice are preserved.  This file is offered as-is,
without any warranty.
