import javax.swing.*;
import java.awt.*;

/**
 * 请求资源对话框类
 *
 * @author wzy
 * @date 2024-02-21 21:21:15
 */
public class RequestResourceDialog extends JDialog {
    private JTextField pidField, requestField; // 请求资源信息
    private JButton requestButton, cancelButton; // 功能按钮
    private BankerAlgorithm banker; // 银行家算法器
    private ControlGUI controlGUI; // UI

    public RequestResourceDialog(Frame owner, BankerAlgorithm banker) {
        super(owner, "请求资源（输入框有悬浮提示）", true);
        this.controlGUI = (ControlGUI) owner;
        this.banker = banker;
        setSize(380, 150);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2));

        add(new JLabel("PID（整数>=0）："));
        pidField = new JTextField();
        pidField.setToolTipText("例如：0");
        InputFilter.setFilter(pidField, "[0-9]\\d*"); // 只允许输入>=0的整数
        add(pidField);

        add(new JLabel("请求向量 request（整数>=0）："));
        requestField = new JTextField();
        requestField.setToolTipText("例如：[1,0,1]");
        add(requestField);

        requestButton = new JButton("请求");
        requestButton.addActionListener(e -> onRequest());
        add(requestButton);

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        setLocationRelativeTo(owner);
    } // end RequestResourceDialog()

    /**
     * 请求按钮功能：为进程请求资源
     */
    private void onRequest() {
        try {
            String pidStr = pidField.getText();
            String requestStr = requestField.getText();

            if (pidStr.isEmpty() || requestStr.isEmpty()) {
                JOptionPane.showMessageDialog(null, "信息未输入完整！！！", "输入错误",
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else if (!requestStr.matches("^\\[\\d+(,\\s*\\d+)*\\]$")) {
                JOptionPane.showMessageDialog(null, "Allocation 应为>=0的整数数组！\n（表格单元格中，逗号后必须有空格）",
                        "格式错误", JOptionPane.ERROR_MESSAGE);
                return; // 如果验证失败，则直接返回并不继续执行后续逻辑
            }

            int pid = Integer.parseInt(pidStr);
            int[] request = ResourceParser.parseResourceArray(requestStr);

            dispose(); // 关闭对话框
            controlGUI.appendBankerRunningResult(banker.tryAllocate(pid, request));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "输入不规范，请注意整数>=0、符号、空格等问题！！！",
                    "格式错误", JOptionPane.ERROR_MESSAGE);
        }
    } // end onRequest()
} // end class RequestResourceDialog
