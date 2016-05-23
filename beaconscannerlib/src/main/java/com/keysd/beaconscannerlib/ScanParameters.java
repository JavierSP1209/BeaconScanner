package com.keysd.beaconscannerlib;

import java.util.Arrays;

/**
 * Class containing the scan parameters to use, this class uses the builder pattern to set only the
 * desired parameters Created by javier.silva on 5/22/16.
 */
public class ScanParameters {
    private long scanPeriod;
    private long scanInterval;
    private byte[] filterUUIDData;

    private ScanParameters(long scanPeriod, long scanInterval, byte[] filterUUIDData) {
        this.scanPeriod = scanPeriod;
        this.scanInterval = scanInterval;
        this.filterUUIDData = filterUUIDData;
    }

    public long getScanPeriod() {
        return scanPeriod;
    }

    public long getScanInterval() {
        return scanInterval;
    }

    public byte[] getFilterUUIDData() {
        return filterUUIDData;
    }

    @Override
    public String toString() {
        return "ScanParameters{" +
                "scanPeriod=" + scanPeriod +
                ", scanInterval=" + scanInterval +
                ", filterUUIDData=" + Arrays.toString(filterUUIDData) +
                '}';
    }

    public static class Builder {
        private long nestedScanPeriod;
        private long nestedScanInterval;
        private byte[] nestedFilterUUIDData;

        public Builder setScanPeriod(long scanPeriod) {
            this.nestedScanPeriod = scanPeriod;
            return this;
        }

        public Builder setScanInterval(long scanInterval) {
            this.nestedScanInterval = scanInterval;
            return this;
        }

        public Builder setFilterUUIDData(byte[] filterUUIDData) {
            this.nestedFilterUUIDData = filterUUIDData;
            return this;
        }

        public ScanParameters build() {
            return new ScanParameters(nestedScanPeriod, nestedScanInterval, nestedFilterUUIDData);
        }

    }
}
