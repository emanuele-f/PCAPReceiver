package com.emanuelef.pcap_receiver;

import android.util.Log;

import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.IpV4Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

public class CaptureThread extends Thread {
    static final String TAG = "CaptureThread";
    static final ByteBuffer PCAP_HDR_BYTES = ByteBuffer.wrap(hex2bytes("d4c3b2a1020004000000000000000000ffff000065000000"));
    final MainActivity mActivity;
    private DatagramSocket mSocket;

    public CaptureThread(MainActivity activity) {
        mActivity = activity;
    }

    public static byte[] hex2bytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    @Override
    public void run() {
        try {
            // Important: requires "android.permission.INTERNET"
            mSocket = new DatagramSocket(5123);
            byte[] buf = new byte[65535];
            DatagramPacket datagram = new DatagramPacket(buf, buf.length);
            Log.d(TAG, "running");

            while(true) {
                mSocket.receive(datagram);
                int len = datagram.getLength();
                ByteBuffer data = ByteBuffer.wrap(buf, 0, len);

                if(data.equals(PCAP_HDR_BYTES)) {
                    Log.d(TAG, "Detected PCAP header, skipping");
                    continue;
                }

                // struct pcaprec_hdr_s
                if(len < 16) {
                    Log.w(TAG, "Invalid PCAP record: " + len);
                    continue;
                }

                // Skip the pcaprec_hdr_s record to get the IPv4 packet
                IpV4Packet pkt = IpV4Packet.newPacket(buf, 16, len - 16);

                mActivity.runOnUiThread(() -> mActivity.onPacketReceived(pkt));
            }
        } catch (IOException | IllegalRawDataException e) {
            e.printStackTrace();
        }
    }

    public void stopCapture() {
        if(mSocket != null)
            mSocket.close();
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
