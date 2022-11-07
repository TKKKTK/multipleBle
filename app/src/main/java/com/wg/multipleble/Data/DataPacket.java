package com.wg.multipleble.Data;

import no.nordicsemi.android.ble.data.Data;

public class DataPacket {
    private Data data;
    private String time;

    public DataPacket(Data data, String time) {
        this.data = data;
        this.time = time;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
