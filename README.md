SDRecord
====

Tool for audio recording with SDR, tested to work with Gqrx: https://github.com/csete/gqrx

It can also be used with others SDR software that send packets with streaming audio over UDP.

Indeed this software does not record directly, but acts like a filter to use in couple with nc (netcat) or similar.
This filter drops UDP packets coming from Gqrx that do not contain any relevant data ( data payload is all 0) and forwards to a specific host, choosen by the user, only packets containing info.

Thus this filter is able to reduce network traffic and the disk space needed for records.

Preamble
--------

Gqrx has an internal function for audio recording but it records even if there is no signal in input, with the effect of consuming a lot of disk space for junk data. 

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

4) In another terminal window (in case of using localhost), launch netcat for recording: ```nc -lup DESTPORT > audiofile``` 

5) You can listen the recorded conversations with a line of code like this: ``` aplay -r 48k -f S16_LE -t raw -c 1 < audiofile```

## Example

```bash
andrea@Workstation:~/Desktop$ java SDRecord 0 7355 localhost 7356
888.09 KB transferred
```

## Tested Environments

* GNU/Linux

If you have successfully tested this software on others systems or platforms please let me know.

License and Donations
-------

Written by Andrea 'cybernova' Dari and licensed under GNU GPL v2.0

If you have found this tool useful I gladly accept donations, also symbolic through Paypal:

<a href="https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=andreadari91%40gmail%2ecom&lc=IT&item_name=Andrea%20Dari%20IT%20independent%20researcher&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest"><img src="https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif" alt="[paypal]" /></a> or Bitcoin: 1B2KqKm4CgzRfSsXv7VmbmXD9XNQzzLaTW
