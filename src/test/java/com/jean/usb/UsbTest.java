package com.jean.usb;

import com.jean.adb.*;
import org.junit.Test;

import javax.usb.*;
import java.util.ArrayList;
import java.util.List;


public class UsbTest {

    @Test
    public void adbTest() throws Exception {
        List<AdbDevice> devices = Adb.findDevices();
        if (devices.isEmpty()) {
            System.err.println("No ADB devices found");
            return;
        }
        AdbDevice device = devices.get(0);
        try {
            device.open();
            Message message = new ConnectMessage(ConnectMessage.SYSTEM_TYPE_HOST, "12345678", "ADB Demo");
            System.out.println("Sending: " + message);
            device.sendMessage(message);
            boolean triedAuthentication = false;
            boolean sentPublicKey = false;
            boolean connected = false;
            while (!connected) {
                message = device.receiveMessage();
                System.out.println("Received: " + message);
                if (message instanceof ConnectMessage) {
                    connected = true;
                } else if (message instanceof AuthMessage) {
                    AuthMessage authMessage = (AuthMessage) message;
                    if (!triedAuthentication) {
                        byte[] signature = Adb.signToken(authMessage.getData());
                        message = new AuthMessage(AuthMessage.TYPE_SIGNATURE, signature);
                        System.out.println("Sending: " + message);
                        device.sendMessage(message);
                        triedAuthentication = true;
                    } else if (!sentPublicKey) {
                        byte[] publicKey = Adb.getPublicKey();
                        message = new AuthMessage(AuthMessage.TYPE_RSAPUBLICKEY, publicKey);
                        System.out.println("Sending: " + message);
                        device.sendMessage(message);
                        triedAuthentication = false;
                        sentPublicKey = true;
                    } else {
                        System.err.println("Couldn't authenticate");
                        System.exit(1);
                    }
                } else {
                    System.err.println("Received unexpected message: " + message);
                    System.exit(1);
                }
            }

            // Open "sync:"
            message = new OpenMessage(1, "sync:");
            System.out.println("Sending: " + message);
            device.sendMessage(message);
            message = device.receiveMessage();
            System.out.println("Received: " + message);
            if (!(message instanceof OkayMessage)) {
                System.err.println("Open failed");
                System.exit(1);
            }
            int remoteId = ((OkayMessage) message).getRemoteId();
            message = new CloseMessage(1, remoteId);
            System.out.println("Sending: " + message);
            device.sendMessage(message);
            message = device.receiveMessage();
            System.out.println("Received: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            device.close();
        }
    }

    @Test
    public void usbTest() throws UsbException {
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub root = services.getRootUsbHub();
        List<UsbDevice> devices = new ArrayList<>();
        listDevices(root, devices);
        for (UsbDevice device : devices) {
            UsbDeviceDescriptor descriptor = device.getUsbDeviceDescriptor();
            if (Vendors.getInstance().include(descriptor.idVendor())) {
                System.out.println(descriptor);
            }

        }
    }


    @SuppressWarnings("ALL")
    public void listDevices(UsbHub hub, List<UsbDevice> devices) {
        List<UsbDevice> attachedUsbDevices = hub.getAttachedUsbDevices();
        for (UsbDevice device : attachedUsbDevices) {
            if (device.isUsbHub()) {
                listDevices((UsbHub) device, devices);
            } else {
                devices.add(device);
            }
        }
    }
}
