import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析银行家算法输入资源类
 *
 * @author wzy
 * @date 2024-02-22 21:31:53
 */
public class ResourceParser {
    /**
     * 解析输入的资源数组
     *
     * @param text 输入文本
     * @return 资源数组
     * @throws Exception 异常
     */
    public static int[] parseResourceArray(String text) throws Exception {
        String trimmedInput = text.substring(1, text.length() - 1); // 去除字符串的开头和结尾的方括号
        String[] stringNumbers = trimmedInput.split(","); // 使用逗号分隔符分割字符串

        int[] numbers = new int[stringNumbers.length];

        for (int i = 0; i < stringNumbers.length; i++) {
            numbers[i] = Integer.parseInt(stringNumbers[i].trim());
        }

        if (numbers.length == 0) {
            throw new Exception("解析错误：输入数据格式不正确");
        }

        return numbers;
    }

    /**
     * 解析输入的资源矩阵
     *
     * @param text 输入文本
     * @return 资源矩阵
     * @throws Exception 异常
     */
    public static int[][] parseResourceMatrix(String text) throws Exception {
        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(text);
        List<int[]> matrixList = new ArrayList<>(); // 使用 List 先暂存匹配到的数组

        while (matcher.find()) {
            String[] numbers = matcher.group(1).split(",");
            int[] row = new int[numbers.length];
            for (int i = 0; i < numbers.length; i++) {
                row[i] = Integer.parseInt(numbers[i].trim()); // 去除可能的空格并转换为整数
            }
            matrixList.add(row); // 将转换后的数组添加到列表中
        }

        int[][] matrix = new int[matrixList.size()][];
        for (int i = 0; i < matrixList.size(); i++) {
            matrix[i] = matrixList.get(i);
        }

        if (matrix.length == 0) {
            throw new Exception("解析错误：输入数据格式不正确");
        }

        return matrix;
    }
} // end class ResourceParser
