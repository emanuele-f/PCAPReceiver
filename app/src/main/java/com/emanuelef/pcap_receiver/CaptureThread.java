package com.emanuelef.pcap_receiver;

import android.util.Log;

import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.IpV4Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class CaptureThread extends Thread {
    static final String TAG = "CaptureThread";
    static final int PCAP_HDR_SIZE = 24;
    static final int PCAPREC_HDR_SIZE = 16;
    static final ByteBuffer PCAP_HDR_START_BYTES = ByteBuffer.wrap(hex2bytes("d4c3b2a1020004000000000000000000"));
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

                if((len == PCAP_HDR_SIZE) && (ByteBuffer.wrap(buf, 0, PCAP_HDR_START_BYTES.capacity()).equals(PCAP_HDR_START_BYTES))) {
                    Log.d(TAG, "Detected PCAP header, skipping");
                    continue;
                }

                // struct pcaprec_hdr_s
                if(len < PCAPREC_HDR_SIZE) {
                    Log.w(TAG, "Invalid PCAP record: " + len);
                    continue;
                }

                // Skip the pcaprec_hdr_s record to get the IPv4 packet, wrapped in an Ethernet frame
                // due to pcapdroid_trailer being set. If you don't set pcapdroid_trailer, you can
                // directly parse the packet with IpV4Packet, see below
                try {
                    EthernetPacket pkt = EthernetPacket.newPacket(buf, PCAPREC_HDR_SIZE, len - PCAPREC_HDR_SIZE);
                    // IpV4Packet pkt = IpV4Packet.newPacket(buf, PCAPREC_HDR_SIZE, len - PCAPREC_HDR_SIZE);
                    mActivity.runOnUiThread(() -> mActivity.onPacketReceived(pkt));
                } catch (IllegalRawDataException e) {
                    // Invalid packet
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            if(!(e instanceof SocketException)) // raised when capture is stopped
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
