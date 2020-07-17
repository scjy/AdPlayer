package com.licheedev.adplayer;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

    private static final char[] DIGITS_LOWER =
        { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static final MessageDigest MESSAGE_DIGEST = getMd5();

    private static MessageDigest getMd5() {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            //e.printStackTrace();
            // 空实现
        }
        return md5;
    }

    public static String md5(String src) {
        try {
            return byteArrayToHexString(MESSAGE_DIGEST.digest(src.getBytes("UTF-8")));
        } catch (Exception e) {
            return "";
        }
    }

    private static String byteArrayToHexString(byte[] data) {
        char[] out = new char[data.length << 1];

        for (int i = 0, j = 0; i < data.length; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }
    

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
