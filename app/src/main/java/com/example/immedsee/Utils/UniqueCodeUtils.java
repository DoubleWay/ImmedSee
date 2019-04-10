package com.example.immedsee.Utils;

import java.util.Random;

/**
 * DoubleWay on 2019/4/10:10:20
 * 邮箱：13558965844@163.com
 * 工具类 产生各种随机数
 *
 */
public class UniqueCodeUtils {
    private static final int NUMBER = 0;

    private static final int UPPER = 1;

    private static final int LOWER = 2;

    private static final int SPECIAL = 3;

    private static final int UNDERLINE = 4;

    private static final int SPACE = 5;

    private static final String[] seedTypes = new String[]{
            "NUMBER", "UPPER", "LOWER", "SPECIAL", "UNDERLINE", "SPACE"
    };

    private static final Random random = new Random();

    private static int SEED_TYPES_LENGTH = seedTypes.length;

    private static char[][] rootSeeds = new char[SEED_TYPES_LENGTH][];

    static {
        StringBuffer sb = new StringBuffer(95);
        for (int i = 48; i < 58; i ++) {
            sb.append((char)i);
        }
        for (int i = 65; i < 91; i ++) {
            sb.append((char)i);
        }
        for (int i = 97; i < 123; i ++) {
            sb.append((char)i);
        }
        for (int i = 33; i < 48; i ++) {
            sb.append((char)i);
        }
        for (int i = 58; i < 65; i ++) {
            sb.append((char)i);
        }
        for (int i = 91; i < 95; i ++) {
            sb.append((char)i);
        }
        for (int i = 96; i < 97; i ++) {
            sb.append((char)i);
        }
        for (int i = 123; i < 127; i ++) {
            sb.append((char)i);
        }
        for (int i = 95; i < 96; i ++) {
            sb.append((char)i);
        }
        for (int i = 32; i < 33; i ++) {
            sb.append((char)i);
        }
        String seedStr = sb.toString();

        rootSeeds[NUMBER] = seedStr.substring(0, 10).toCharArray();

        rootSeeds[UPPER] = seedStr.substring(10, 36).toCharArray();

        rootSeeds[LOWER] = seedStr.substring(36, 62).toCharArray();

        rootSeeds[SPECIAL] = seedStr.substring(62, 93).toCharArray();

        rootSeeds[UNDERLINE] = new char[]{seedStr.charAt(93)};

        rootSeeds[SPACE] = new char[]{seedStr.charAt(94)};

    }

    /**
     * 交换数组中两个字符位置
     * @param chars
     * @param a
     * @param b
     */
    private static void swap(char[] chars, int a, int b) {
        char t;
        t = chars[a];
        chars[a] = chars[b];
        chars[b] = t;
    }

    /**
     * 验证字符数组中是否包含指定字符
     * @param chars
     * @param a
     * @return
     */
    private static boolean hasChar(char[] chars, char a) {
        for (int i = 0; i < chars.length; i ++) {
            if (chars[i] == a) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据指定的每种字符类型数量生成表达式
     * @param typesNum
     * @return
     */
    private static int[][] createRules(int... typesNum) {
        return new int[][]{
                {NUMBER, typesNum[NUMBER]},
                {UPPER, typesNum[UPPER]},
                {LOWER, typesNum[LOWER]},
                {SPECIAL, typesNum[SPECIAL]},
                {UNDERLINE, typesNum[UNDERLINE]},
                {SPACE, typesNum[SPACE]}
        };
    }

    /**
     * 根据数量规则生成字符种子
     * @param rules {{3, 5}, {4, 8}} ==> {{"类型LOWER", "5个"}, {"类型UNDERLINE", "8个"}}
     * @return
     */
    private static char[] generateSeeds(int[][] rules) {
        StringBuffer sb = new StringBuffer();
        for (int[] rule : rules) {
            for (int i = 0; i < rule[1]; i ++) {
                sb.append(rootSeeds[rule[0]][random.nextInt(rootSeeds[rule[0]].length)]);
            }
        }
        return sb.toString().toCharArray();
    }

    /**
     * 根据指定条件生成随机字符串通用方法一
     * @param needBeginWithLetter 指定是否需要以字母开头
     * @param typesNum 按顺序指定所有字符类型个数
     * @return
     */
    private static String generate(boolean needBeginWithLetter, int... typesNum){

        char[] seeds = generateSeeds(createRules(typesNum));
        for (int i = 0; i < seeds.length; i ++) {
            int randomIndex = random.nextInt(seeds.length);
            if (randomIndex != i) {
                swap(seeds, i, randomIndex);
            }
        }
        if (needBeginWithLetter && !hasChar(rootSeeds[UPPER], seeds[0]) && !hasChar(rootSeeds[LOWER], seeds[0])) {

            for (int i = 1; i < seeds.length; i ++) {
                if (hasChar(rootSeeds[UPPER], seeds[i]) || hasChar(rootSeeds[LOWER], seeds[i])) {
                    swap(seeds, 0, i);
                    break;
                }
            }
        }
        return String.valueOf(seeds);
    }

    /**
     * 根据指定条件生成随机字符串通用方法二
     * @param length 指定字符个数
     * @param needBeginWithLetter 指定是否需要以字母开头
     * @param hasTypes 按顺序指定所有字符类型是否需要
     * @return
     */
    private static String generate(int length, boolean needBeginWithLetter, boolean... hasTypes){
        int[] typesNum = new int[SEED_TYPES_LENGTH];
        if (needBeginWithLetter && hasTypes[UPPER]) {
            typesNum[UPPER] ++;
            length --;
        }else if (needBeginWithLetter && hasTypes[LOWER]) {
            typesNum[LOWER] ++;
            length --;
        }
        int checkedNum = 0;
        for (int i = 0; i < hasTypes.length; i ++) {
            if (hasTypes[i]) {
                checkedNum ++;
            }
        }
        for (int i = 0; i < length; i ++) {
            int k = 0, r = random.nextInt(checkedNum);
            for (int j = 0; j < hasTypes.length; j ++) {
                if (hasTypes[j]) {
                    if (r == k) {
                        typesNum[j] ++;
                        break;
                    }
                    k ++;
                }
            }
        }
        return generate(needBeginWithLetter, typesNum);
    }

    /**
     * 生成一个由32个字符长度的随机字符串，
     * 该字符串由数字和小写字母构成
     * @return
     */
    public static String genUniqueID() {
        boolean[] hasTypes = new boolean[SEED_TYPES_LENGTH];
        hasTypes[NUMBER] = true;
        hasTypes[LOWER] = true;
        return generate(32, false, hasTypes);
    }

    /**
     * 生成指定长度的随机字符串，
     * 该字符串由数字和小写字母构成。
     * @param length
     * @return
     */
    public static String genUniqueID(int length) {
        boolean[] hasTypes = new boolean[SEED_TYPES_LENGTH];
        hasTypes[NUMBER] = true;
        hasTypes[LOWER] = true;
        return generate(32, false, hasTypes);
    }

    /**
     * 生成一个长度为8个字符的字符串，
     * 该字符串仅有数字和大写字母构成，
     * 且该字符串由大写字母开头。
     * @return
     */
    public static String genShortUID() {
        boolean[] hasTypes = new boolean[SEED_TYPES_LENGTH];
        hasTypes[NUMBER] = true;
        hasTypes[UPPER] = true;
        return generate(8, true, hasTypes);
    }

    /**
     * 生成一个长度为8个字符的随机密码，
     * 该密码由数字和大小写字母构成。
     * @return
     */
    public static String genPWD() {
        boolean[] hasTypes = new boolean[SEED_TYPES_LENGTH];
        hasTypes[NUMBER] = true;
        hasTypes[UPPER] = true;
        hasTypes[LOWER] = true;
        return generate(8, false, hasTypes);
    }

    /**
     * 生成一个长度为8个字符的随机密码，
     * 该密码由数字、大写字母、小写字母和特殊字符构成。
     * @return
     */
    public static String genDifficultPWD() {
        boolean[] hasTypes = new boolean[SEED_TYPES_LENGTH];
        hasTypes[NUMBER] = true;
        hasTypes[UPPER] = true;
        hasTypes[LOWER] = true;
        hasTypes[SPECIAL] = true;
        return generate(10, false, hasTypes);
    }

    /**
     * 生成一个简单的6位数字组成的密码
     * @return
     */
    public static String genSimplePWD() {
        boolean[] hasTypes = new boolean[SEED_TYPES_LENGTH];
        hasTypes[NUMBER] = true;
        return generate(6, false, hasTypes);
    }

    public static void main(String[] args) {
        System.out.println("------------genUniqueID--------------");
        for (int i = 0; i < 20; i ++) {
            String randomStr = genUniqueID();
            System.out.println(randomStr);
        }
        System.out.println("------------genShortUID--------------");
        for (int i = 0; i < 20; i ++) {
            String randomStr = genShortUID();
            System.out.println(randomStr);
        }
        System.out.println("------------genDifficultPWD--------------");
        for (int i = 0; i < 20; i ++) {
            String randomStr = genDifficultPWD();
            System.out.println(randomStr);
        }
        System.out.println("------------genPWD--------------");
        for (int i = 0; i < 20; i ++) {
            String randomStr = genPWD();
            System.out.println(randomStr);
        }
        System.out.println("------------genSimplePWD--------------");
        for (int i = 0; i < 20; i ++) {
            String randomStr = genSimplePWD();
            System.out.println(randomStr);
        }

    }

}

