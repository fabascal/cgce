package com.epson.epos2easyselect_parse.common;

import com.epson.easyselect.EasySelectDeviceType;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.printer.Printer;

public class Utility {

    // --------------------------------------------------------------------------------------------

    /**
     * Build "Target"Text
     *
     * @param deviceType DeviceType
     * @param macAddress MACAddress
     * @return String "Target"Text
     */

    public static String convertEasySelectInfoToTargetString(int deviceType, String macAddress) {

        StringBuilder sb = new StringBuilder();

        switch (deviceType) {
            case EasySelectDeviceType.TCP:    // TCP : 0
                sb.append("TCP:");
                break;
            case EasySelectDeviceType.BLUETOOTH:    // Bluetooth : 1
                sb.append("BT:");
                break;
        }
        sb.append(macAddress);
        return sb.toString();
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Convert PrinterName to PrinterSeries
     *
     * @param printerName Printer Name
     * @return int printerSeries
     */
    public static int convertPrinterNameToPrinterSeries(String printerName) {

        int printerSeries = Printer.TM_T88;

        if (printerName.equals("TM-T88V") || printerName.equals("TM-T88VI")) {
            printerSeries = Printer.TM_T88;
        } else if (printerName.equals("TM-m10")) {
            printerSeries = Printer.TM_M10;
        } else if (printerName.equals("TM-m30")) {
            printerSeries = Printer.TM_M30;
        } else if (printerName.equals("TM-P20")) {
            printerSeries = Printer.TM_P20;
        } else if (printerName.equals("TM-P60II")) {
            printerSeries = Printer.TM_P60II;
        } else if (printerName.equals("TM-P80")) {
            printerSeries = Printer.TM_P80;
        }else if (printerName.equals("TM-H6000V")) {
            printerSeries = Printer.TM_H6000;
        } else {
            // if you use other printer , add convert printer id
        }

        return printerSeries;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Convert To DeviceInfo to EposEasySelectDeviceType
     *
     * @param deviceInfo DeviceInfo Class Object
     * @return int EasySelectDeviceType
     */

    public static int convertEpos2DeviceInfoToEposEasySelectDeviceType(DeviceInfo deviceInfo) {

        int deviceType = EasySelectDeviceType.TCP;

        if (isDeviceNetwork(deviceInfo)) {
            deviceType = EasySelectDeviceType.TCP;
        } else if (isDeviceBluetooth(deviceInfo)) {
            deviceType = EasySelectDeviceType.BLUETOOTH;
        }

        return deviceType;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Check DeviceType(Network)
     *
     * @param deviceInfo DeviceInfo Class Object
     * @return boolean "Is Device Type Network?"
     */

    public static boolean isDeviceNetwork(DeviceInfo deviceInfo) {

        String macAddress = deviceInfo.getMacAddress();

        if (macAddress == null) {
            return false;
        }
        if (macAddress.equals("")) {
            return false;
        }
        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Check DeviceType(Bluetooth)
     *
     * @param deviceInfo DeviceInfo Class Object
     * @return boolean "Is Device Type Bluetooth?"
     */

    public static boolean isDeviceBluetooth(DeviceInfo deviceInfo) {

        String bdAddress = deviceInfo.getBdAddress();

        if (bdAddress == null) {
            return false;
        }
        if (bdAddress.equals("")) {
            return false;
        }
        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Get Interface string from EposConnectionType
     *
     * @param deviceType deviceType
     * @return String InterfaceString
     */

    public static String getInterfaceStringFromEposConnectionType(int deviceType) {

        switch (deviceType) {
            case EasySelectDeviceType.BLUETOOTH:
                return "Bluetooth";
            case EasySelectDeviceType.TCP:
                return "Network";
            default:
                return "";
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Get Address from DeviceInfo
     *
     * @param deviceInfo DeviceInfo Class Object
     * @return String Address
     */

    public static String getAddressFromEpos2DeviceInfo(DeviceInfo deviceInfo) {

        String address = "";

        if (isDeviceNetwork(deviceInfo)) {
            address = deviceInfo.getMacAddress();
        } else if (isDeviceBluetooth(deviceInfo)) {
            address = deviceInfo.getBdAddress();
        }

        return address;
    }
}
