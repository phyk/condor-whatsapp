# WACE
## A condor whatsapp extractor

# Prerequisites
In order to run all those tools, a java development kit needs to be installed and setup to be on your path of your operating system.

# Setup
To get the tool running on any operating system, first clone the directory to a local directory. Then, get the latest release from 
https://github.com/MarcoG3/WhatsDump/releases and put it in a subdirectory ./dist/whatsdump/ and rename it to whatsdump.exe for windows and to whatsdump otherwise.

Next step is to open a power shell, terminal or another interaction window and run the whatsdump executable with the following command: ./dist/whatsdump/whatsdump.exe --install-sdk
This way, the android sdk is installed to the local folder android-sdk. This is needed to extract the encrypted database from android phones. Prior to running the tool, the operating system has to be set in the config file at ./config/dynamic_config.txt
After that, the tool can be run via starting the main method, located at ./src/main/java/application/Main.java

# Results
The intermediate results and the database are stored under the directory ./data and the generated export is copied to ./export.
