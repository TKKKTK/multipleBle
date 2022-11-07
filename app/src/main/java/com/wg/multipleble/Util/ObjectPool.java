package com.wg.multipleble.Util;

import com.wg.multipleble.Manager.DecoratorBleManager;

import java.util.HashMap;
import java.util.Map;

public class ObjectPool {
    static Map<String, DecoratorBleManager> bleManagerMap = new HashMap<String,DecoratorBleManager>();

    public static void addBleManager(String key,DecoratorBleManager decoratorBleManager){
          bleManagerMap.put(key,decoratorBleManager);
    }

    public static DecoratorBleManager getBleManager(String key){
        return bleManagerMap.get(key);
    }

    public static void removeBleManager(String key){
        bleManagerMap.remove(key);
    }
}
