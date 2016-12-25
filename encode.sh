#!/bin/bash
mkdir -p mp3

for RAW in *.raw; do
    lame --quiet -r -s 48 -m m "$RAW" "mp3/$RAW".mp3 2>/dev/null 
    rm $RAW
done    
