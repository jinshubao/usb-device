package com.jean.adb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * USB Vendors of ADB devices.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public class Vendors {

    private Vendors() {
    }

    private static class Holder {
        private static Vendors VENDORS = new Vendors();
    }

    public static Vendors getInstance() {
        return Holder.VENDORS;
    }

    /**
     * The fixed list of vendor ids.
     */
    private Short[] FIXED_VENDOR_IDS = new Short[]{
            // Acer's USB Vendor ID
            0x0502,
            // Allwinner's USB Vendor ID
            0x1F3A,
            // Amlogic's USB Vendor ID
            0x1b8e,
            // AnyDATA's USB Vendor ID
            0x16D5,
            // Archos's USB Vendor ID
            0x0E79,
            // Asus's USB Vendor ID
            0x0b05,
            // BYD's USB Vendor ID
            0x1D91,
            // Compal's USB Vendor ID
            0x1219,
            // Dell's USB Vendor ID
            0x413c,
            // ECS's USB Vendor ID
            0x03fc,
            // EMERGING_TECH's USB Vendor ID
            0x297F,
            // Emerson's USB Vendor ID
            0x2207,
            // Foxconn's USB Vendor ID
            0x0489,
            // Fujitsu's USB Vendor ID
            0x04C5,
            // Funai's USB Vendor ID
            0x0F1C,
            // Garmin-Asus's USB Vendor ID
            0x091E,
            // Gigabyte's USB Vendor ID
            0x0414,
            // Gigaset's USB Vendor ID
            0x1E85,
            // Google's USB Vendor ID
            0x18d1,
            // Haier's USB Vendor ID
            0x201E,
            // Harris's USB Vendor ID
            0x19A5,
            // Hisense's USB Vendor ID
            0x109b,
            // HP's USB Vendor ID
            0x03f0,
            // HTC's USB Vendor ID
            0x0bb4,
            // Huawei's USB Vendor ID
            0x12D1,
            // INQ Mobile's USB Vendor ID
            0x2314,
            // Intel's USB Vendor ID
            (short) 0x8087,
            // IRiver's USB Vendor ID
            0x2420,
            // K-Touch's USB Vendor ID
            0x24E3,
            // KT Tech's USB Vendor ID
            0x2116,
            // Kobo's USB Vendor ID
            0x2237,
            // Kyocera's USB Vendor ID
            0x0482,
            // Lab126's USB Vendor ID
            0x1949,
            // Lenovo's USB Vendor ID
            0x17EF,
            // LenovoMobile's USB Vendor ID
            0x2006,
            // LG's USB Vendor ID
            0x1004,
            // Lumigon's USB Vendor ID
            0x25E3,
            // Motorola's USB Vendor ID
            0x22b8,
            // MSI's USB Vendor ID
            0x0DB0,
            // MTK's USB Vendor ID
            0x0e8d,
            // NEC's USB Vendor ID
            0x0409,
            // B&N Nook's USB Vendor ID
            0x2080,
            // Nvidia's USB Vendor ID
            0x0955,
            // OPPO's USB Vendor ID
            0x22D9,
            // On-The-Go-Video's USB Vendor ID
            0x2257,
            // OUYA's USB Vendor ID
            0x2836,
            // Pantech's USB Vendor ID
            0x10A9,
            // Pegatron's USB Vendor ID
            0x1D4D,
            // Philips's USB Vendor ID
            0x0471,
            // Panasonic Mobile Communication's USB Vendor ID
            0x04DA,
            // Positivo's USB Vendor ID
            0x1662,
            // Qisda's USB Vendor ID
            0x1D45,
            // Qualcomm's USB Vendor ID
            0x05c6,
            // Quanta's USB Vendor ID
            0x0408,
            // Rockchip's USB Vendor ID
            0x2207,
            // Samsung's USB Vendor ID
            0x04e8,
            // Sharp's USB Vendor ID
            0x04dd,
            // SK Telesys's USB Vendor ID
            0x1F53,
            // Sony's USB Vendor ID
            0x054C,
            // Sony Ericsson's USB Vendor ID
            0x0FCE,
            // T & A Mobile Phones' USB Vendor ID
            0x1BBB,
            // TechFaith's USB Vendor ID
            0x1d09,
            // Teleepoch's USB Vendor ID
            0x2340,
            // Texas Instruments's USB Vendor ID
            0x0451,
            // Toshiba's USB Vendor ID
            0x0930,
            // Vizio's USB Vendor ID
            (short) 0xE040,
            // Wacom's USB Vendor ID
            0x0531,
            // Xiaomi's USB Vendor ID
            0x2717,
            // YotaDevices's USB Vendor ID
            0x2916,
            // Yulong Coolpad's USB Vendor ID
            0x1EBF,
            // ZTE's USB Vendor ID
            0x19D2
    };

    private Short[] VENDOR_IDS = createVendorIds();


    private Short[] createVendorIds() {
        Set<Short> vendorIds = new HashSet<>(Arrays.asList(FIXED_VENDOR_IDS));
        File android = new File(System.getProperty("user.home"), ".android");
        File ini = new File(android, "adb_usb.ini");
        if (ini.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(ini))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("0x")) {
                        vendorIds.add(Short.parseShort(line.substring(2), 16));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return vendorIds.toArray(new Short[0]);
    }

    public Short[] getVendorIds() {
        return VENDOR_IDS;
    }

    public boolean include(Short vendorId) {
        for (Short v : getVendorIds()) {
            if (v.equals(vendorId)) {
                return true;
            }
        }
        return false;
    }
}
