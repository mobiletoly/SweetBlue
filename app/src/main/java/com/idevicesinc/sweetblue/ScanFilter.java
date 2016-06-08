package com.idevicesinc.sweetblue;


import android.bluetooth.BluetoothDevice;
import com.idevicesinc.sweetblue.utils.BleScanInfo;
import com.idevicesinc.sweetblue.utils.Utils_String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public interface ScanFilter
{

    Please onEvent(ScanEvent e);

    class ScanEvent
    {
        private final BleScanInfo mScanInfo;
        private final String mName_native;
        private final String mName_device;
        private final BluetoothDevice mDevice_native;
        private final int mRssi;

        ScanEvent(BluetoothDevice device, BleScanInfo scanInfo, String native_name, String device_name, int rssi)
        {
            mScanInfo = scanInfo;
            mDevice_native = device;
            mName_native = native_name;
            mName_device = device_name;
            mRssi = rssi;
        }

        public BluetoothDevice nativeInstance()
        {
            return mDevice_native;
        }

        public List<UUID> advertisedServices()
        {
            if (!mScanInfo.isNull())
            {
                return mScanInfo.getServiceUUIDS();
            }
            else
            {
                return new ArrayList<>(0);
            }
        }

        public String name_native()
        {
            return mName_native;
        }

        public String name_device()
        {
            return mName_device;
        }

        public byte[] scanRecord()
        {
            if (!mScanInfo.isNull())
            {
                return mScanInfo.getRawScanRecord();
            }
            else
            {
                return new byte[0];
            }
        }

        public int rssi()
        {
            return mRssi;
        }

        public int txPower()
        {
            if (!mScanInfo.isNull())
            {
                return mScanInfo.getTxPower().value;
            }
            else
            {
                return 0;
            }
        }

        public String macAddress()
        {
            return mDevice_native.getAddress();
        }

        public int advertisingFlags()
        {
            if (!mScanInfo.isNull())
            {
                return mScanInfo.getAdvFlags().value;
            }
            else
            {
                return 0;
            }
        }

        public int manufacturerId()
        {
            if (!mScanInfo.isNull())
            {
                return mScanInfo.getManufacturerId();
            }
            else
            {
                return 0;
            }
        }

        public byte[] manufacturerData()
        {
            if (!mScanInfo.isNull())
            {
                return mScanInfo.getManufacturerData();
            }
            else
            {
                return new byte[0];
            }
        }

        public Map<UUID, byte[]> serviceData()
        {
            if (!mScanInfo.isNull())
            {
                return mScanInfo.getServiceData();
            }
            else
            {
                return new HashMap<>(0);
            }
        }

        @Override public String toString()
        {
            return Utils_String.toString(
                    this.getClass(),
                    "macAddress",       macAddress(),
                    "name",             name_device(),
                    "services",         advertisedServices()
            );
        }
    }

    class Please
    {
        private final boolean mAck;
        private boolean mStopScan;
        private final BleDeviceConfig mConfig;

        private Please(boolean ack, BleDeviceConfig config)
        {
            mAck = ack;
            mConfig = config;
        }

        boolean ack()
        {
            return mAck;
        }

        BleDeviceConfig config()
        {
            return mConfig;
        }

        public Please thenStopScan()
        {
            mStopScan = true;
            return this;
        }

        public static Please acknowledge()
        {
            return new Please(true, null);
        }

        public static Please acknowledge(BleDeviceConfig config)
        {
            return new Please(true, config);
        }

        public static Please acknowledgeIf(boolean condition)
        {
            return condition ? acknowledge() : ignore();
        }

        public static Please acknowledgeIf(boolean condition, BleDeviceConfig config)
        {
            return condition ? acknowledge(config) : ignore();
        }

        public static Please ignore()
        {
            return new Please(false, null);
        }

        public static Please ignoreIf(boolean condition)
        {
            return condition ? ignore() : acknowledge();
        }
    }

}