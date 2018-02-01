
package com.jean.adb;

import javax.crypto.Cipher;
import javax.usb.*;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("unchecked")
public class Adb {

    private static final byte ADB_CLASS = 6;


    private static final byte ADB_SUBCLASS = 1;


    private static final byte ADB_PROTOCOL = 1;


    private static byte[] headerOID = new byte[]{0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x05, 0x00, 0x04, 0x14};


    public static List<AdbDevice> findDevices() throws UsbException {
        UsbServices services = UsbHostManager.getUsbServices();
        List<AdbDevice> usbDevices = new ArrayList<>();
        findDevices(services.getRootUsbHub(), usbDevices);
        return usbDevices;
    }


    private static void findDevices(UsbHub hub, List<AdbDevice> devices) {
        List<UsbDevice> deviceList = (List<UsbDevice>) hub.getAttachedUsbDevices();
        for (UsbDevice usbDevice : deviceList) {
            if (usbDevice.isUsbHub()) {
                findDevices((UsbHub) usbDevice, devices);
            } else {
                if (checkDevice(usbDevice)) {
                    devices.add(new AdbDevice(usbDevice));
                }
            }
        }
    }


    private static boolean checkDevice(UsbDevice usbDevice) {
        UsbDeviceDescriptor deviceDesc = usbDevice.getUsbDeviceDescriptor();
        return Vendors.getInstance().include(deviceDesc.idVendor());

    }


    public static RSAPrivateKey getPrivateKey() throws IOException, GeneralSecurityException {
        File file = new File(System.getProperty("user.home"), ".android/adbkey");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith("----")) {
                    builder.append(line);
                }
                line = reader.readLine();
            }
            byte[] bytes = DatatypeConverter.parseBase64Binary(builder.toString());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
            return (RSAPrivateKey) keyFactory.generatePrivate(ks);
        }
    }


    public static byte[] getPublicKey() throws IOException {
        File file = new File(System.getProperty("user.home"), ".android/adbkey.pub");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.write(0);
        in.close();
        out.close();
        return out.toByteArray();
    }


    public static byte[] signToken(byte[] token) throws IOException, GeneralSecurityException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(headerOID);
        stream.write(token);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey());
        return cipher.doFinal(stream.toByteArray());
    }
}
