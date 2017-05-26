# Minecraft Server Backup Utility
A simple utility to back up multiple Minecraft servers to a specified location.

### Features

- Only backs up servers that have been changed since the last backup
- Labels backups by the time the files were last modified, not when the backup was made

### Expected file structure

This utility expects to find all of your Minecraft server folders in the same parent directory:

```
MinecraftServers
├───Vanilla-1.11.2
├───Vanilla-1.9.4
├───FTB-Beyond-1.7.0
├───FTB-Beyond-1.5.0
└───FTB-Skyfactory-3.0.10
```

The backup folder follows the same structure as the servers, however the individual server directories contain zipped backups:

```
MinecraftServers
├───Vanilla-1.11.2
│   ├───Vanilla-1.11.2_2017-05-21_23-00-00.zip
│   ├───Vanilla-1.11.2_2017-05-20_23-00-00.zip
│   ├───Vanilla-1.11.2_2017-05-20_24-00-00.zip
├───Vanilla-1.9.4
│   ├───Vanilla-1.9.4_2017-05-21_23-00-00.zip
│   ├───Vanilla-1.9.4_2017-05-20_23-00-00.zip
│   ├───Vanilla-1.9.4_2017-05-20_24-00-00.zip
...
```

### Usage & Installation

#### Graphically

1. Double click on the jar to generate a `config.ini` in that folder
2. Fill out the `config.ini` values for `serversDirectory` and `backupsDirectory` as specified in [Configuration options](#configuration-options)
3. With `config.ini` filled out, double click the jar again and a backup directory should now appear in the specified location
4. Confirm the servers were backed up as expected into the backups directory.

#### From a console

1. Navigate to the directory that you want `config.ini` to be saved (usually the same directory that the jar is saved in)
2. Run `java -jar /path/to/mc-server-backup-util-X.X.X.jar` to generate a `config.ini` in that folder
3. Fill out the `config.ini` values for `serversDirectory` and `backupsDirectory` as specified in [Configuration options](#configuration-options)
4. With `config.ini` filled out, run `java -jar /path/to/mc-server-backup-util-X.X.X.jar` again and a backup directory should now appear in the specified location
5. Confirm the servers were backed up as expected into the backups directory.

#### Using Windows Task Scheduler

1. Configure and test the utility using one the graphical or console installation steps above
2. Open Task Scheduler by opening the run prompt or start menu and typing `taskschd.msc` and press [Enter]
3. Under the "Action" menu, choose "Create Basic Task..."
4. Enter a name, then press "Next"
5. Choose "Daily" (it can be made more frequent after creating the task), then press "Next"
6. Choose the time you want to back up, then press "Next"
7. Select "Start a program", then press "Next"
8. In "Program/script", enter the path to `java.exe`. Instructions on finding this path can be found in [How to locate your Java executable](#how-to-locate-your-java-executable)
9. In "Add arguments", enter `-jar "/path/to/mc-server-backup-util-X.X.X.jar"`
10. In "Start in", enter the path to the directory containing `mc-server-backup-util-X.X.X.jar`, *without* quotes, then press "Next"
11. Press "Finish"
12. On the left column, click the folder "Task Scheduler Library", locate the name of the task that was just created, right click it, and choose "Run"
13. After the program has finished, press F5 to refresh the list and check the "Last Run Result" of the task, it should be "The operation completed successfully. (0x0)", if it doesn't it didn't work correctly.

**NOTE**: If you prefer the script to run without a console window popping up, use `javaw.exe` instead of `java.exe` in step 8.

### Configuration options

| Option name      | Default   | Description                                                      | Examples |
|:-----------------|:---------:|------------------------------------------------------------------|----------|
| serversDirectory | -         | The location of the folder containing the server folders.        | `../servers`, `C://servers`, `/home/user/servers` |
| backupsDirectory | -         | The location of the folder containing the backup folders. This directory will be created if it does not exist. |  `../backups`, `J://backups`, `/home/user/backups` |

### Appendix I

#### How to locate your Java executable

##### Windows

1. Open the run prompt or start menu and type `cmd` and press [Enter]
2. In the console that opens, type `where java`, this will display all paths to your installed Java directories (there may be more than one). The Java executable should be in the `bin` folder in the listed directories.

**NOTE**: If multiple directories are listed from `where java` you're not sure which one to use. Try typing `echo %JAVA_HOME%` and using that directory, though that environment variable may not exist, depending on how Java was installed.

### Building and Versioning

This project includes preconfigured ANT versioning and packaging options.

**To display the current version used by ANT:**
```
ant
```

**To package the current version into a jar:**
```
ant dist
```

**To change the version and package the current version into a jar:**
```
ant revision (or ant major or ant minor)
ant dist
```
Note that changing the version and packaging the jar must occur as two separate ANT operations.
