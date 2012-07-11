#!/bin/sh

# Runs PiPlayer by Aditya Vaidya <kroq.gar78@gmail.com>
# Watch out! It can generate a LOT of output (1 million digits of Pi)
# if left unchecked!

javac PiPlayer.java && (java PiPlayer)

echo # add blank line at the end
