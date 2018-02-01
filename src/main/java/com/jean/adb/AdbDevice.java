/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package com.jean.adb;

import javax.usb.*;
import javax.usb.event.UsbDeviceListener;
import java.io.Closeable;
import java.util.List;


public class AdbDevice implements Closeable {

    private final UsbDevice usbDevice;

    private UsbEndpoint inEndpoint;
    private UsbEndpoint outEndpoint;
    private UsbEndpoint interruptEndpoint;
    private UsbInterface usbInterface;

    public AdbDevice(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
        UsbConfiguration config = usbDevice.getActiveUsbConfiguration();
        List<UsbInterface> interfaces = config.getUsbInterfaces();
        for (UsbInterface usbInterface : interfaces) {
            List<UsbEndpoint> endpoints = usbInterface.getUsbEndpoints();
            if (endpoints.size() >= 2) {
                for (UsbEndpoint endpoint : endpoints) {
                    if (endpoint.getType() == UsbConst.ENDPOINT_TYPE_BULK) {
                        if (endpoint.getDirection() == UsbConst.ENDPOINT_DIRECTION_IN) {
                            //输入端点
                            this.inEndpoint = endpoint;
                        } else if (endpoint.getDirection() == UsbConst.ENDPOINT_DIRECTION_OUT) {
                            //输出端点
                            this.outEndpoint = endpoint;
                        }
                    } else if (endpoint.getType() == UsbConst.ENDPOINT_TYPE_INTERRUPT) {
                        this.interruptEndpoint = endpoint;
                    }
                }
                if (inEndpoint != null && outEndpoint != null) {
                    this.usbInterface = usbInterface;
                }
            }
        }
    }


    public void open() throws UsbException {
        this.usbInterface.claim(usbInterface -> true);
    }

    public void close() {
        try {
            if (this.usbInterface.isClaimed()) {
                this.usbInterface.release();
            }
        } catch (UsbException e) {
            e.printStackTrace();
        }
    }


    public void sendMessage(Message message) throws UsbException {
        UsbPipe outPipe = this.outEndpoint.getUsbPipe();
        MessageHeader header = message.getHeader();
        outPipe.open();
        try {
            int sent = outPipe.syncSubmit(header.getBytes());
            if (sent != MessageHeader.SIZE)
                throw new InvalidMessageException("Invalid ADB message header size sent: " + sent);
            sent = outPipe.syncSubmit(message.getData());
            if (sent != header.getDataLength())
                throw new InvalidMessageException("Data size mismatch in sent ADB message. Should be " + header.getDataLength() + " but is " + sent);
        } finally {
            outPipe.close();
        }
    }


    public Message receiveMessage() throws UsbException {
        UsbPipe inPipe = this.inEndpoint.getUsbPipe();
        inPipe.open();
        try {
            byte[] headerBytes = new byte[MessageHeader.SIZE];
            int received = inPipe.syncSubmit(headerBytes);
            if (received != MessageHeader.SIZE)
                throw new InvalidMessageException("Invalid ADB message header size: " + received);
            MessageHeader header = new MessageHeader(headerBytes);
            if (!header.isValid())
                throw new InvalidMessageException("ADB message header checksum failure");
            byte[] data = new byte[header.getDataLength()];
            received = inPipe.syncSubmit(data);
            if (received != header.getDataLength())
                throw new InvalidMessageException("ADB message data size mismatch. Should be " + header.getDataLength() + " but is " + received);
            Message message = Message.create(header, data);
            if (!message.isValid())
                throw new InvalidMessageException("ADB message data checksum failure");
            return message;
        } finally {
            inPipe.close();
        }
    }

    public void addUsbDeviceListener(UsbDeviceListener listener) {
        this.usbDevice.addUsbDeviceListener(listener);
    }

    public void removeUsbDeviceListener(UsbDeviceListener listener) {
        this.usbDevice.removeUsbDeviceListener(listener);
    }
}
