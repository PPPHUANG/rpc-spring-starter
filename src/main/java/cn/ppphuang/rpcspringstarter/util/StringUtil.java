package cn.ppphuang.rpcspringstarter.util;

/**
 * 字符串工具类
 *
 * @Author: ppphuang
 * @Create: 2022/4/20
 */
public class StringUtil {
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
}
