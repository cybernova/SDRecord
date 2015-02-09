SDRecord
====

Tool for audio recording with SDR, tested to work with Gqrx: https://github.com/csete/gqrx

It also can be used with others SDR software that send UDP packets with streaming audio over the internet.

Indeed this software does not record directly, but acts like a filter to use in couple with nc (netcat).
This filter blocks UDP packets coming from Gqrx that do not contain any relevant data and dispatches to a specific host, choosen by the user, only packets containing info. 

Preamble
--------

Gqrx has an internal function for audio recording but it records even if none is transmitting anything, with the effect of consuming a lot of disk space for junk data. 
Another bad effect is the difficulty to follow a conversation when you have junk data (silence segments) between talked segments in the record.

Usage and Options
-----------------

***Usage:***

```
java SDRecord minutes_to_record source_port dest_host dest_port [buff_size]
                       1              2         3         4          5
```

***Options:***

```
1 - An integer as number of minutes to record (ideal). 0 for no limit
2 - An integer between 1024-65535. This is the port used by Gqrx where to send UDP packets. Default 7355
3 - IP or hostname of the destination host. Ex: localhost
4 - An integer between 1024-65535. This is the port used by the destination host for receive filtered UDP packets
Optional:
5 - An integer as size in bytes of the buffer. Should be as big as the MAX size of UDP packets sended by Gqrx. With no value the default is 1500 bytes
```

Putting all together
--------------------

1) Download and compile the java code: ```javac SDRecord.java```

2) Launch Gqrx enabling stream audio over UDP (default port 7355)

3) In a terminal window, launch the filter: ```java SDRecord OPTIONS```

4) In another terminal window (in case of using localhost), launch netcat for recording: ```nc -l -u DESTPORT > audiofile``` 

5) You can listen the recorded conversations with a line of code like this: ``` aplay -r 48k -f S16_LE -t raw -c 1 < audiofile```

License
-------

Written by Andrea 'cybernova' Dari and licensed under GNU GPL v2.0

