#!/bin/sh

java -cp dist/SmartcardIO.jar:lib/pi4j-1.2-SNAPSHOT/lib/pi4j-core.jar smartcardio.LoyalityCard slot1.p12 slot1
