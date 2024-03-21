import javax.swing.*;
import java.awt.*;

/**
 * 创建进程对话框类
 *
 * @author wzy
 * @date 2024-02-17 18:39:11
 */
public class CreateProcessDialog extends JDialog {
    private JTextField processNameField, priorityField, arrivalTimeField, requiredRuntimeField; // 进程信息
    private JButton createButton, cancelButton; // 功能按钮
    private PCB process; // 进程
    private Scheduler scheduler; // 进程调度器
    private ControlGUI controlGUI; // UI

    public CreateProcessDialog(Frame owner, Scheduler scheduler) {
        super(owner, "创建进程（输入框有悬浮提示）", true);
        this.controlGUI = (ControlGUI) owner;
        this.scheduler = scheduler;
        setSize(330, 190);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2));

        add(new JLabel("进程名："));
        processNameField = new JTextField();
        processNameField.setToolTipText("例如：P1,进程1");
        add(processNameField);

        add(new JLabel("优先数（整数>=0）："));
        priorityField = new JTextField();
        priorityField.setToolTipText("例如：0,1");
        InputFilter.setFilter(priorityField, "[0-9]\\d*"); // 只允许输入>=0的整数
        add(priorityField);

        add(new JLabel("到达时间（整数>=0）："));
        arrivalTimeField = new JTextField();
        arrivalTimeField.setToolTipText("例如：0,1");
        InputFilter.setFilter(arrivalTimeField, "[0-9]\\d*"); // 只允许输入>=0的整数
        add(arrivalTimeField);

        add(new JLabel("需要运行时间（整数>=0）："));
        requiredRuntimeField = new JTextField();
        requiredRuntimeField.setToolTipText("例如：0,1");
        InputFilter.setFilter(requiredRuntimeField, "[0-9]\\d*"); // 只允许输入>=0的整数
        add(requiredRuntimeField);

        createButton = new JButton("创建");
        createButton.addActionListener(e -> createProcess());
        add(createButton);

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        setLocationRelativeTo(owner);
    } // end AddProcessDialog()

    /**
     * 创建进程
     */
    private void createProcess() {
        /* 获取输入 */
        String name = processNameField.getText();
        String priorityStr = priorityField.getText();
        String arrivalTimeStr = arrivalTimeField.getText();
        String requiredRuntimeStr = requiredRuntimeField.getText();

        /* 检验输入 */
        if (name.isEmpty() || priorityStr.isEmpty() || arrivalTimeStr.isEmpty() || requiredRuntimeStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "进程信息未输入完整，请输入！！！", "输入错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        /* 解析输入 */
        int priority = Integer.parseInt(priorityStr);
        int arrivalTime = Integer.parseInt(arrivalTimeStr);
        int requiredRuntime = Integer.parseInt(requiredRuntimeStr);

        /* 创建 PCB 实例，并添加到调度器 */
        process = new PCB(name, priority, arrivalTime, requiredRuntime);
        scheduler.createProcess(process);

        /* 清空输入字段以便下一次输入 */
        processNameField.setText("");
        priorityField.setText("");
        arrivalTimeField.setText("");
        requiredRuntimeField.setText("");

        dispose(); // 关闭对话框
        controlGUI.createProcess(process); // 创建的后端进程数据同步到前端
    } // end createProcess()
} // end class CreateProcessDialog
