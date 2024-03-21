import javax.swing.*;
import javax.swing.text.*;

/**
 * 输入过滤器类
 *
 * @author wzy
 * @date 2024-02-18 15:53:29
 */
public class InputFilter {
    /**
     * 为 JTextField 设置通用过滤器
     *
     * @param textField JTextField 对象
     * @param pattern   正则式
     */
    public static void setFilter(JTextField textField, String pattern) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches(pattern)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches(pattern)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    } // end setFilter()
} // end class InputFilter
