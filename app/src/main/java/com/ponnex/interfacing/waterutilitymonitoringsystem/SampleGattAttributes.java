package com.ponnex.interfacing.waterutilitymonitoringsystem;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String HM_RX_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";

    static {
        //HM-10 serial service
        attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "HM 10 Serial");

        //HM-10 serial characteristic
        attributes.put(HM_RX_TX,"RX/TX data");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
