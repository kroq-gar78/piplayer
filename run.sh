#!/bin/sh -eu

# Runs PiPlayer by Aditya Vaidya <kroq.gar78@gmail.com>
# Watch out! It can generate a LOT of output (1 million digits of Pi)
# if left unchecked!

if [ ! -f ./PiPlayer.class ] ; then
	javac PiPlayer.java
fi

java PiPlayer

echo # add blank line at the end
