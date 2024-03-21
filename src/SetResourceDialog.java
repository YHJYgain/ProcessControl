import javax.swing.*;
import java.awt.*;

/**
 * 设置系统可用资源对话框类
 *
 * @author wzy
 * @date 2024-02-18 16:42:22
 */
public class SetResourceDialog extends JDialog {
    private JTextField availableField, maxField, allocationField; // 资源信息
    private JButton confirmButton, cancelButton; // 功能按钮
    private BankerAlgorithm banker; // 银行家算法器
    private ControlGUI controlGUI; // UI

    public SetResourceDialog(Frame owner, BankerAlgorithm banker) {
        super(owner, "设置系统资源（输入框有悬浮提示）", true);
        this.controlGUI = (ControlGUI) owner;
        this.banker = banker;
        setSize(530, 190);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2));

        add(new JLabel("系统可用资源数 Available（整数>=0）："));
        availableField = new JTextField();
        availableField.setToolTipText("例如：[2,3,1]");
        add(availableField);

        add(new JLabel("各进程最大需求量 Max（整数>=0）："));
        maxField = new JTextField();
        maxField.setToolTipText("例如：[1,1,1],[1,1,0]");
        add(maxField);

        add(new JLabel("各进程已占用资源数 Allocation（整数>=0）："));
        allocationField = new JTextField();
        allocationField.setToolTipText("例如：[0,0,0],[0,1,0]");
        add(allocationField);

        confirmButton = new JButton("设置");
        confirmButton.addActionListener(e -> onConfirm());
        add(confirmButton);

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        setLocationRelativeTo(owner);
    } // end SetResourceDialog()

    /**
     * 确认按钮功能：确认设置资源
     */
    private void onConfirm() {
        try {
            String availableStr = availableField.getText();
            String maxStr = maxField.getText();
            String allocationStr = allocationField.getText();

            /* 验证输入的格式合法性 */
            if (availableStr.isEmpty() || maxStr.isEmpty() || allocationStr.isEmpty()) {
                JOptionPane.showMessageDialog(null, "资源信息未输入完整，请输入！！！", "输入错误",
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else if (validateInputFormats(availableStr, maxStr, allocationStr)) {
                return; // 如果验证失败，则直接返回并不继续执行后续逻辑
            }

            int[] available = ResourceParser.parseResourceArray(availableStr);
            int[][] max = ResourceParser.parseResourceMatrix(maxStr);
            int[][] allocation = ResourceParser.parseResourceMatrix(allocationStr);

            /* 验证输入的算法合法性 */
            if (isAllocationWithinMax(max, allocation)) {
                JOptionPane.showMessageDialog(this, "进程已占用资源数超过了进程最大需求量！！",
                        "分配超限", JOptionPane.WARNING_MESSAGE);
                return;
            }

            banker = new BankerAlgorithm(available, max, allocation);

            dispose(); // 关闭对话框
            controlGUI.updateResourceInfo(banker);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "输入不规范，请注意整数>=0、符号、空格等问题！！！",
                    "格式错误", JOptionPane.ERROR_MESSAGE);
        }
    } // end onConfirm()

    /**
     * 验证资源输入是否都是正整数
     *
     * @param availableStr  系统可用资源数文本
     * @param maxStr        各进程最大需求量文本
     * @param allocationStr 各进程已占用资源数文本
     * @return 验证结果
     */
    public boolean validateInputFormats(String availableStr, String maxStr, String allocationStr) {
        String regexForAvailable = "^\\[\\d+(,\\s*\\d+)*\\]$"; // >=0的整数数组
        String regexForMaxAndAllocation = "^\\[(\\d+(,\\d+)*)](,\\[(\\d+(,\\d+)*)])*"; // >=0的整数序列数组

        boolean isValidAvailable = availableStr.matches(regexForAvailable);
        boolean isValidMax = maxStr.matches(regexForMaxAndAllocation);
        boolean isValidAllocation = allocationStr.matches(regexForMaxAndAllocation);

        if (!isValidAvailable || !isValidMax || !isValidAllocation) {
            JOptionPane.showMessageDialog(this, "输入不规范，请注意整数>=0、符号、空格等问题！！！",
                    "格式错误", JOptionPane.ERROR_MESSAGE);
            return true;
        }

        return false;
    } // end validateInputFormats()

    /**
     * 验证输入的“进程已占用资源数”是否超过了“进程最大需求量”
     *
     * @param max        各进程最大需求量
     * @param allocation 各进程已占用资源数
     * @return 验证结果
     */
    public boolean isAllocationWithinMax(int[][] max, int[][] allocation) {
        for (int i = 0; i < allocation.length; i++) {
            for (int j = 0; j < allocation[i].length; j++) {
                if (allocation[i][j] > max[i][j]) return true;
            }
        }
        return false;
    } // end isAllocationWithinMax()
} // end class SetResourceDialog
