package com.wg.multipleble.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UUIDList {
    public List<Map<String,UUID>> mapList = new ArrayList<>();

    private static UUIDList _instance;
    private UUIDList(){
       addUUID();
    }

    public static UUIDList getInstance(){
        if (_instance == null){
            _instance = new UUIDList();
        }
        return _instance;
    }

    public void addUUID(){
        Map<String,UUID> map0 = new HashMap<>();
        map0.put("SERVICE_UUID",UUID.fromString("0000CCF0-67EE-4600-BCCC-FF98C2A749AB"));
        map0.put("READ_UUID",UUID.fromString("0000CCF1-67EE-4600-BCCC-FF98C2A749AB"));
        map0.put("WRITE_UUID",UUID.fromString("0000CCF2-67EE-4600-BCCC-FF98C2A749AB"));

        Map<String,UUID> map1 = new HashMap<>();
        map1.put("SERVICE_UUID",UUID.fromString("8653000a-43e6-47b7-9cb0-5fc21d4ae340"));
        map1.put("READ_UUID",UUID.fromString("8653000b-43e6-47b7-9cb0-5fc21d4ae340"));
        map1.put("WRITE_UUID",UUID.fromString("8653000c-43e6-47b7-9cb0-5fc21d4ae340"));

        Map<String,UUID> map2 = new HashMap<>();
        map2.put("SERVICE_UUID",UUID.fromString("0000000a-1da3-5366-b478-afc8bb2af312"));
        map2.put("READ_UUID",UUID.fromString("0000000b-1da3-5366-b478-afc8bb2af312"));
        map2.put("WRITE_UUID",UUID.fromString("0000000c-1da3-5366-b478-afc8bb2af312"));

        Map<String,UUID> map3 = new HashMap<>();
        map3.put("SERVICE_UUID",UUID.fromString("0000ffe0-3c17-d293-8e48-14fe2e4da211"));
        map3.put("READ_UUID",UUID.fromString("0000ffe2-3c17-d293-8e48-14fe2e4da211"));
        map3.put("WRITE_UUID",UUID.fromString("0000ffe3-3c17-d293-8e48-14fe2e4da211"));

        Map<String,UUID> map4 = new HashMap<>();
        map4.put("SERVICE_UUID",UUID.fromString("8653000a-7de6-99b7-9c6a-5fc2cf4aaa40"));
        map4.put("READ_UUID",UUID.fromString("8653000b-7de6-99b7-9c6a-5fc2cf4aaa40"));
        map4.put("WRITE_UUID",UUID.fromString("8653000c-7de6-99b7-9c6a-5fc2cf4aaa40"));

        Map<String,UUID> map5 = new HashMap<>();
        map5.put("SERVICE_UUID",UUID.fromString("0000CCF0-67EE-4600-BCCC-FF98C2A749AC"));
        map5.put("READ_UUID",UUID.fromString("0000CCF1-67EE-4600-BCCC-FF98C2A749AC"));
        map5.put("WRITE_UUID",UUID.fromString("0000CCF2-67EE-4600-BCCC-FF98C2A749AC"));

        mapList.add(map5);
        mapList.add(map4);
        mapList.add(map0);
        mapList.add(map1);
        mapList.add(map2);
        mapList.add(map3);
    }


}
