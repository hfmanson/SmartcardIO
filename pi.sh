#/bin/sh

java -Dsun.security.smartcardio.library=/usr/lib/arm-linux-gnueabihf/libpcsclite.so.1 -cp dist/SmartcardIO.jar:lib/pi4j-1.2-SNAPSHOT/lib/pi4j-core.jar smartcardio.LoyalityCard slot1.p12 slot1 'ACS ACR122U PICC Interface 00 00'
