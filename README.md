# PCAPReceiver
A sample app to show how to receive packets via [PCAPdroid](https://github.com/emanuele-f/PCAPdroid).

<p align="center">
<img src="https://raw.githubusercontent.com/emanuele-f/PCAPReceiver/master/screenshots/app.png" width="190" />
</p>

*NOTE*: if you are using a debug build of PCAPdroid, ensure to add `.debug` to the `PCAPDROID_PACKAGE` constraint, otherwise the PCAPdroid app won't be found.

Relevant sources:
 - [MainActivity.java](https://github.com/emanuele-f/PCAPReceiver/blob/master/app/src/main/java/com/emanuelef/pcap_receiver/MainActivity.java) - code to start/stop the capture
 - [CaptureThread.java](https://github.com/emanuele-f/PCAPReceiver/blob/master/app/src/main/java/com/emanuelef/pcap_receiver/CaptureThread.java) - packets receiver
 
 Check out the [PCAPdroid API](https://github.com/emanuele-f/PCAPdroid/blob/master/docs/app_api.md) for more details.
