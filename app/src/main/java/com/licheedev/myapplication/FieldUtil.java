package com.licheedev.myapplication;

import java.lang.reflect.Field;

public class FieldUtil {

    /**
     * 通过属性值获取对应的属性名
     *
     * @param value 属性值
     * @param classes 需要搜索的类型
     * @return
     */
    public static String getFieldName(Object value, Class... classes) {

        if (value == null) {
            return null;
        }

        for (Class cls : classes) {
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                try {
                    if (value.equals(f.get(null))) {
                        return f.getName();
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                }
            }
        }
        
        return null;
    }
}
