import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;


/**
 * 系统主界面
 *
 * @author wzy
 * @date 2024-02-17 18:42:50
 */
public class ControlGUI extends JFrame {
    private static final String FCFS = "先来先服务算法（FCFS）";
    private static final String HPF = "最高优先数算法（HPF）";
    private static final String SEPARATOR = "-------------------------------".repeat(5) + "\n"; // 分割符

    private JPanel mainPanel, schedulerPanel, bankerPanel; // 主面板、进程调度面板、银行家算法面板
    private JPanel schedulerTopPanel, schedulerCenterPanel; // 进程调度顶部、中心面板
    private JPanel bankerTopPanel, bankerCenterPanel; // 银行家算法顶部、中心面板
    private JComboBox<String> schedulerComboBox; // 进程调度算法选择器
    private JScrollPane processScrollPane, resourceScrollPane; // 进程、资源表格滚动面板
    private DefaultTableModel processTableModel, resourceTableModel; // 进程、资源表格模型
    private JTable processTable, resourceTable; // 进程信息、资源信息表格
    private TableCellEditor processTableCellEditor, resourceTableCellEditor; // 进程、资源表格单元格编辑器
    private JButton createProcessButton, killProcessButton, clearProcessButton, startSchedulingButton; // 进程调度面板功能按钮
    private JButton setResourceDialogButton, clearResourceButton, runBankerAlgorithmButton; // 银行家算法面板功能按钮
    private JTextArea schedulingOutputTextArea, bankerOutputTextArea; // 算法执行结果
    private CreateProcessDialog createProcessDialog; // “创建进程”功能对话框
    private SetResourceDialog setResourceDialog; // “设置系统资源”功能对话框
    private RequestResourceDialog requestResourceDialog; // “请求资源”功能对话框

    private int selectedRow, selectedColumn; // 选中单元格的行列索引
    private String selectedScheduler; // 选择的进程调度算法名
    private PCB currentProcess, selectedProcess; // 当前运行进程（用于判断调度是否结束）、被选择编辑进程
    private Scheduler scheduler; // 进程调度器
    private Timer schedulingTimer; // 进程调度定时器
    private BankerAlgorithm banker; // 银行家算法器

    public ControlGUI() {
        super("进程控制和银行家算法的模拟实现");
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null); // 窗口居中显示
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置"关闭"按钮操作
        mainPanel = new JPanel(new GridLayout(1, 2));
        initSchedulerComponents();
        initBankerComponents();
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    } // end ControlGUI()

    /**
     * 自定义表格样式
     */
    private void customizeTableStyle(JTable table) {
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Serif", Font.BOLD, 16)); // 设置表头字体大小
        table.setFont(new Font("Serif", Font.PLAIN, 17)); // 设置字体大小
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER); // 设置居中
        table.setDefaultRenderer(String.class, centerRenderer); // 对于 String 类型数据居中
        table.setDefaultRenderer(Integer.class, centerRenderer); // 对于 Integer 类型数据居中
        table.setDefaultRenderer(Character.class, centerRenderer); // 对于 Character 类型数据居中
    } // end customizeTableStyle()

    /**
     * 初始化进程调度组件
     */
    private void initSchedulerComponents() {
        schedulerPanel = new JPanel(new BorderLayout());
        startSchedulingButton = new JButton("开始进程调度");

        initSchedulerTop();
        iniSchedulerCenter();

        startSchedulingButton.addActionListener(e -> runSelectedScheduler());

        schedulerPanel.add(schedulerTopPanel, BorderLayout.NORTH);
        schedulerPanel.add(schedulerCenterPanel, BorderLayout.CENTER);
        schedulerPanel.add(startSchedulingButton, BorderLayout.SOUTH);
        mainPanel.add(schedulerPanel);
    } // end initSchedulerComponents()

    /**
     * 初始化进程调度顶部组件
     */
    private void initSchedulerTop() {
        schedulerTopPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        schedulerComboBox = new JComboBox<>(new String[]{FCFS, HPF});
        createProcessButton = new JButton("创建进程");
        killProcessButton = new JButton("撤销进程");
        clearProcessButton = new JButton("清空进程");

        /* 默认先来先服务（FIFO）算法 */
        schedulerComboBox.setSelectedIndex(0);
        scheduler = new FCFScheduler();
        selectedScheduler = (String) schedulerComboBox.getSelectedItem();

        schedulerComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) { // 只处理选中的状态
                selectedScheduler = (String) schedulerComboBox.getSelectedItem();
                initScheduler();
            }
        });

        createProcessButton.addActionListener(e -> {
            /* 多次添加进程时，只在进程队列为空时重置调度输出文本框 */
            if (scheduler.readyQueue.isEmpty() && scheduler.waitQueue.isEmpty()
                    && scheduler.finishedProcesses.isEmpty()) {
                schedulingOutputTextArea.setText("【进程信息】\n");
            }

            createProcessDialog = new CreateProcessDialog(ControlGUI.this, scheduler);
            createProcessDialog.setVisible(true);
        });

        killProcessButton.addActionListener(e -> {
            int confirmed = JOptionPane.showConfirmDialog(
                    null,
                    "您确定要撤销当前选择的进程吗？",
                    "撤销进程",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirmed == JOptionPane.YES_OPTION) {
                killProcess(); // 如果用户点击“是”，则执行撤销操作
            }
        });

        clearProcessButton.addActionListener(e -> {
            int confirmed = JOptionPane.showConfirmDialog(
                    null,
                    "您确定要清空当前进程吗？",
                    "清空进程",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirmed == JOptionPane.YES_OPTION) {
                if (!scheduler.readyQueue.isEmpty()) {
                    initScheduler();
                    processTableModel.setRowCount(0); // 清空表格数据
                    schedulingOutputTextArea.setText("【进程信息】\n");

                    JOptionPane.showMessageDialog(null, "已清空！");
                } else {
                    JOptionPane.showMessageDialog(null, "当前已无任何进程！");
                }
            }
        });

        schedulerTopPanel.add(schedulerComboBox);
        schedulerTopPanel.add(createProcessButton);
        schedulerTopPanel.add(killProcessButton);
        schedulerTopPanel.add(clearProcessButton);
    } // end initSchedulerTop()

    /**
     * 初始化进程调度面板中心组件
     */
    private void iniSchedulerCenter() {
        schedulerCenterPanel = new JPanel(new BorderLayout());
        schedulingOutputTextArea = new JTextArea();

        initProcessTable();

        schedulingOutputTextArea.setEditable(false);
        schedulingOutputTextArea.setFont(new Font("Serif", Font.PLAIN, 15));
        schedulingOutputTextArea.append("【进程信息】\n");

        schedulerCenterPanel.add(processScrollPane, BorderLayout.NORTH);
        schedulerCenterPanel.add(new JScrollPane(schedulingOutputTextArea), BorderLayout.CENTER);
    } // end iniSchedulerCenter()

    /**
     * 初始化进程表
     */
    private void initProcessTable() {
        String[] columnNames = {"进程名", "优先数", "到达时间", "需要运行时间", "已用 CPU 时间", "进程状态"};
        processTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 1: // 优先数
                    case 2: // 到达时间
                    case 3: // 需要运行时间
                    case 4: // 已用 CPU 时间
                        return Integer.class;
                    case 5: // 进程状态
                        return Character.class;
                    default:
                        return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column < 4; // 前四列可编辑
            }
        };
        processTable = new JTable(processTableModel);
        processTable.setRowHeight(30);
        processScrollPane = new JScrollPane(processTable);

        customizeTableStyle(processTable);

        processTableCellEditor = new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                selectedColumn = processTable.getSelectedColumn();
                String newValue = (String) getCellEditorValue();
                if (selectedColumn == 1) {
                    try {
                        Integer.parseInt(newValue); // 尝试将输入转换为整数以验证是否为整数
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "优先数必须是整数！",
                                "编辑警告", JOptionPane.WARNING_MESSAGE);
                        return false; // 不接受编辑
                    }
                } else if (selectedColumn == 2&& !newValue.matches("^[0-9]\\d*$")) {
                    JOptionPane.showMessageDialog(null, "到达时间必须是>=0的整数！",
                            "编辑警告", JOptionPane.WARNING_MESSAGE);
                    return false; // 不接受编辑
                } else if (selectedColumn == 3 && !newValue.matches("^[0-9]\\d*$")) {
                    JOptionPane.showMessageDialog(null, "需要运行时间必须是>=0的整数！",
                            "编辑警告", JOptionPane.WARNING_MESSAGE);
                    return false; // 不接受编辑
                }
                return super.stopCellEditing(); // 接受编辑
            } // end stopCellEditing()
        };
        processTable.getColumnModel().getColumn(1).setCellEditor(processTableCellEditor); // 优先数
        processTable.getColumnModel().getColumn(2).setCellEditor(processTableCellEditor); // 到达时间
        processTable.getColumnModel().getColumn(3).setCellEditor(processTableCellEditor); // 需要运行时间

        processTable.addPropertyChangeListener(e -> {
            if ("tableCellEditor".equals(e.getPropertyName()) && !processTable.isEditing()) {
                editProcess();
            }
        });
    } // end initProcessTable()

    /**
     * 初始化进程调度器
     */
    public void initScheduler() {
        if (FCFS.equals(selectedScheduler)) {
            scheduler = new FCFScheduler();
        } else if (HPF.equals(selectedScheduler)) {
            scheduler = new HPFScheduler();
        }

        /* 切换进程调度算法，则需重新创建进程 */
        resetProcessTable();
        printProcessInfo();
    } // end initScheduler()

    /**
     * 开始进程调度
     */
    private void runSelectedScheduler() {
        setButtonsEnabled(schedulerPanel, false); // 在进行进程调度时，禁止调度面板的所有操作

        printProcessInfo();
        if (scheduler.finishedProcesses.isEmpty()) {
            resetProcessTable(); // 多次点击“开始调度”，则重置进程表格状态，以便重新调度
        }

        String tipBegin = "【开始" + selectedScheduler + "调度】\n";
        schedulingOutputTextArea.append(tipBegin + SEPARATOR);

        /* 创建定时器，每1.2秒更新一次，动态显示进程调度过程 */
        schedulingTimer = new Timer(1200, e -> {
            String result = scheduler.schedule();
            schedulingOutputTextArea.append(result);

            /* 重绘组件以显示最新状态 */
            processTable.repaint();
            schedulingOutputTextArea.repaint();

            currentProcess = scheduler.currentProcess;
            if (currentProcess != null) {
                updateProcessTable(currentProcess); // 若当前还有运行进程，则实时更新进程信息表格
            } else {
                schedulingTimer.stop(); // 进程调度结束
                setButtonsEnabled(schedulerPanel, true); // 进程调度结束时，恢复调度面板的所有操作
            }
        });
        schedulingTimer.start(); // 启动定时器

        if (scheduler.readyQueue.isEmpty()) {
            JOptionPane.showMessageDialog(null, "还未创建任何进程！！",
                    "调度警告", JOptionPane.WARNING_MESSAGE);
        }
    } // end runSelectedScheduler()

    /**
     * 在界面显示添加成功的进程信息
     *
     * @param process 进程
     */
    public void createProcess(PCB process) {
        SwingUtilities.invokeLater(() -> {
            Object[] rowData = new Object[]{
                    process.getName(),
                    process.getPriority(),
                    process.getArrivalTime(),
                    process.getRequiredRuntime(),
                    process.getUsedCPUTime(),
                    process.getState()
            };
            processTableModel.addRow(rowData);

            /* 解决在进行进程调度结束之后直接点击“创建进程”按钮带来的文本框显示出错问题 */
            if (scheduler.readyQueue.isEmpty()) { // 首次创建进程
                schedulingOutputTextArea.append(process + "\n");
            } else { // 在之前创建的进程基础上，继续创建进程
                resetProcessTable();
                printProcessInfo();
            }
        });
    } // end createProcess()

    /**
     * 撤销进程
     */
    private void killProcess() {
        int selectedRow = processTable.getSelectedRow(); // 获取选中的行
        if (selectedRow >= 0) { // 确保选中了一行
            List<PCB> tempList = new ArrayList<>(scheduler.readyQueue);
            selectedProcess = tempList.get(selectedRow);

            /* 后端撤销 */
            scheduler.killProcess(selectedProcess);
            scheduler.resetSchedulingStatus(null);

            /* 同步到前端 */
            processTableModel.removeRow(selectedRow); // 从表格模型中删除行
            for (PCB process : scheduler.readyQueue) {
                updateProcessTable(process);
            }
            printProcessInfo();

            JOptionPane.showMessageDialog(null, "已撤销！");
        } else {
            JOptionPane.showMessageDialog(this, "请先选择一个进程！！！",
                    "撤销失败", JOptionPane.ERROR_MESSAGE);
        }
    } // end killProcess()

    /**
     * 编辑进程信息
     */
    private void editProcess() {
        int selectedRow = processTable.getSelectedRow();
        int selectedColumn = processTable.getSelectedColumn();
        Object newValue = processTableModel.getValueAt(selectedRow, selectedColumn);

        List<PCB> tempList = new ArrayList<>(scheduler.readyQueue);
        selectedProcess = tempList.get(selectedRow);

        /* 根据列索引更新 PCB 对象的相应属性 */
        switch (selectedColumn) {
            case 0: // 进程名
                selectedProcess.setName((String) newValue);
                printProcessInfo();
                break;
            case 1: // 优先数
                selectedProcess.setPriority(Integer.parseInt(newValue.toString()));
                printProcessInfo();
                break;
            case 2: // 到达时间
                selectedProcess.setArrivalTime(Integer.parseInt(newValue.toString()));
                printProcessInfo();
                break;
            case 3: // 需要运行时间
                selectedProcess.setRequiredRuntime(Integer.parseInt(newValue.toString()));
                printProcessInfo();
                break;
        }
    } // end editProcessInfo()

    /**
     * 实时更新进程信息表格
     *
     * @param updatedProcess 已更新进程
     */
    private void updateProcessTable(PCB updatedProcess) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < processTableModel.getRowCount(); ++i) {
                if (processTableModel.getValueAt(i, 0).equals(updatedProcess.getName())) {
                    processTableModel.setValueAt(updatedProcess.getPriority(), i, 1);
                    processTableModel.setValueAt(updatedProcess.getUsedCPUTime(), i, 4);
                    processTableModel.setValueAt(updatedProcess.getState(), i, 5);
                    break;
                }
            }
            processTable.repaint(); // 确保表格及时更新
        });
    } // end updateProcessTable()

    /**
     * 重置进程信息表格
     */
    private void resetProcessTable() {
        SwingUtilities.invokeLater(() -> {
            processTableModel.setRowCount(0); // 清空表格数据
            for (PCB process : scheduler.readyQueue) {
                Object[] rowData = new Object[]{
                        process.getName(),
                        process.getPriority(),
                        process.getArrivalTime(),
                        process.getRequiredRuntime(),
                        process.getUsedCPUTime(),
                        process.getState()
                };
                processTableModel.addRow(rowData);
            }
        });
    } // end resetProcessTable()

    /**
     * 打印进程信息
     */
    private void printProcessInfo() {
        schedulingOutputTextArea.setText("【进程信息】\n");
        for (PCB pcb : scheduler.readyQueue) {
            schedulingOutputTextArea.append(pcb + "\n");
        }
    } // end printProcessInfo()

    /**
     * 初始化银行家算法组件
     */
    private void initBankerComponents() {
        bankerPanel = new JPanel(new BorderLayout());
        runBankerAlgorithmButton = new JButton("运行银行家算法");

        intiBankerTop();
        initBankerCenter();

        runBankerAlgorithmButton.addActionListener(e -> runBankersAlgorithm());

        bankerPanel.add(bankerTopPanel, BorderLayout.NORTH);
        bankerPanel.add(bankerCenterPanel, BorderLayout.CENTER);
        bankerPanel.add(runBankerAlgorithmButton, BorderLayout.SOUTH);
        mainPanel.add(bankerPanel);
    } // end initBankerComponents()

    /**
     * 初始化银行家算法顶部组件
     */
    private void intiBankerTop() {
        bankerTopPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        setResourceDialogButton = new JButton("设置系统资源");
        clearResourceButton = new JButton("清空系统资源");

        setResourceDialogButton.addActionListener(e -> {
            setResourceDialog = new SetResourceDialog(ControlGUI.this, banker);
            setResourceDialog.setVisible(true);
        });

        clearResourceButton.addActionListener(e -> {
            int confirmed = JOptionPane.showConfirmDialog(
                    null,
                    "您确定要清空当前系统资源吗？",
                    "清空系统资源",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirmed == JOptionPane.YES_OPTION) {
                if (banker != null) { // 如果用户点击“是”，则执行清空操作
                    banker = null;
                    resourceTableModel.setRowCount(0); // 清空表格数据
                    bankerOutputTextArea.setText("【资源信息】\n");

                    JOptionPane.showMessageDialog(null, "已清空！");
                } else {
                    JOptionPane.showMessageDialog(null, "当前系统资源已空！");
                }
            }
        });

        bankerTopPanel.add(setResourceDialogButton);
        bankerTopPanel.add(clearResourceButton);
    } // end intiBankerTop()

    /**
     * 初始化银行家算法中心组件
     */
    private void initBankerCenter() {
        bankerCenterPanel = new JPanel(new BorderLayout());
        bankerOutputTextArea = new JTextArea();

        initResourceTable();

        bankerOutputTextArea.setEditable(false);
        bankerOutputTextArea.setFont(new Font("Serif", Font.PLAIN, 15));
        bankerOutputTextArea.setText("【资源信息】\n");

        bankerCenterPanel.add(resourceScrollPane, BorderLayout.NORTH);
        bankerCenterPanel.add(new JScrollPane(bankerOutputTextArea), BorderLayout.CENTER);
    } // end initBankerCenter()

    /**
     * 初始化资源表
     */
    private void initResourceTable() {
        String[] columnNames = {"PID", "Max", "Allocation", "Need", "Available"};
        resourceTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: // PID
                        return Integer.class;
                    case 1: // Max
                    case 2: // Allocation
                    case 3: // Need
                    case 4: // Available
                        return String.class;
                    default:
                        return Object.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 1 || column == 2) return true; // 第2、3列可以整列编辑
                else return column == 4 && row == 0; // 第5列只有第一行可以编辑
            }
        };
        resourceTable = new JTable(resourceTableModel);
        resourceTable.setRowHeight(30); // 设置表格每一行的高度为30像素
        resourceScrollPane = new JScrollPane(resourceTable);

        customizeTableStyle(resourceTable);

        resourceTableCellEditor = new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    selectedRow = resourceTable.getSelectedRow();
                    selectedColumn = resourceTable.getSelectedColumn();
                    String value = (String) getCellEditorValue();

                    int[] newValues = Arrays.stream(value.replaceAll("[\\[\\]]", "").split(", "))
                            .mapToInt(Integer::parseInt)
                            .toArray(); // 解析新值为整数数组

                    if (selectedColumn == 1) {
                        int[][] tempMax = banker.getMax();
                        tempMax[selectedRow] = newValues;
                        if (!value.matches("^\\[\\d+(,\\s*\\d+)*\\]$")) {
                            JOptionPane.showMessageDialog(null, "Max 应为整数序列！！！\n（表格单元格中，逗号后必须有空格）",
                                    "格式错误", JOptionPane.ERROR_MESSAGE);
                            return false;
                        } else if (setResourceDialog.isAllocationWithinMax(tempMax, banker.getAllocation())) {
                            JOptionPane.showMessageDialog(null, "进程已占用资源数超过了进程最大需求量！！！",
                                    "分配超限", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    } else if (selectedColumn == 2) {
                        int[][] tempAllocation = banker.getAllocation();
                        tempAllocation[selectedRow] = newValues;
                        if (!value.matches("^\\[\\d+(,\\s*\\d+)*\\]$")) {
                            JOptionPane.showMessageDialog(null, "Allocation 应为整数序列！！！\n"
                                    + "（表格单元格中，逗号后必须有空格）", "格式错误", JOptionPane.ERROR_MESSAGE);
                            return false;
                        } else if (setResourceDialog.isAllocationWithinMax(banker.getMax(), tempAllocation)) {
                            JOptionPane.showMessageDialog(null, "进程已占用资源数超过了进程最大需求量！！",
                                    "分配超限", JOptionPane.WARNING_MESSAGE);
                            return false;
                        }
                    }
                    return super.stopCellEditing(); // 接受编辑
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "输入不规范，请注意整数>=0、符号、空格等问题！！！\n"
                            + "（表格单元格中，逗号后必须有空格）", "格式错误", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } // end stopCellEditing()
        };
        resourceTable.getColumnModel().getColumn(1).setCellEditor(resourceTableCellEditor); // Max
        resourceTable.getColumnModel().getColumn(2).setCellEditor(resourceTableCellEditor); // Allocation
        resourceTable.getColumnModel().getColumn(4).setCellEditor(resourceTableCellEditor); // Available

        resourceTable.addPropertyChangeListener(e -> {
            if ("tableCellEditor".equals(e.getPropertyName()) && !processTable.isEditing()) {
                editResourceInfo();
            }
        });
    } // end initResourceTable()

    /**
     * 运行银行家算法
     */
    private void runBankersAlgorithm() {
        setButtonsEnabled(bankerPanel, false); // 在进行进程调度时，禁止银行家算法面板的所有操作

        bankerOutputTextArea.setText("【资源信息】\n");
        String tipBegin = "【运行银行家算法】\n";
        if (banker != null) {
            bankerOutputTextArea.append(banker + "\n");
            bankerOutputTextArea.append(tipBegin + SEPARATOR);

            requestResourceDialog = new RequestResourceDialog(ControlGUI.this, banker);
            requestResourceDialog.setVisible(true);

            updateResourceInfo(banker);
        } else {
            String tipEnd = "【银行家算法运行结束】\n";
            bankerOutputTextArea.append(tipBegin + SEPARATOR + tipEnd);

            JOptionPane.showMessageDialog(null, "还未设置系统资源！！",
                    "运行警告", JOptionPane.WARNING_MESSAGE);
        }

        setButtonsEnabled(bankerPanel, true); // 在进行进程调度时，恢复银行家算法面板的所有操作
    } // end runBankersAlgorithm()

    /**
     * 在文本框显示初始化完成的系统资源信息
     *
     * @param bankerAlgorithm 银行家算法器
     */
    public void updateResourceInfo(BankerAlgorithm bankerAlgorithm) {
        SwingUtilities.invokeLater(() -> {
            if (banker != null && banker == bankerAlgorithm) {
                bankerAlgorithm.setFinish(new boolean[bankerAlgorithm.getMax().length]);
                Arrays.fill(bankerAlgorithm.getFinish(), false);
            } else {
                banker = bankerAlgorithm;
                bankerOutputTextArea.setText("【资源信息】\n");
            }
            bankerOutputTextArea.append(bankerAlgorithm + "\n");

            /* 获取资源配置信息 */
            int[] available = bankerAlgorithm.getAvailable();
            int[][] max = bankerAlgorithm.getMax();
            int[][] allocation = bankerAlgorithm.getAllocation();
            int[][] need = bankerAlgorithm.getNeed();

            resourceTableModel.setRowCount(0);
            /* 构建并添加表格行 */
            for (int i = 0; i < max.length; i++) {
                Vector<Object> row = new Vector<>();
                row.add(i); // PID
                row.add(Arrays.toString(max[i])); // Max
                row.add(Arrays.toString(allocation[i])); // Allocation
                row.add(Arrays.toString(need[i])); // Need

                /* 对于 Available 列，由于它是全局一致的，只在第一行添加数据 */
                if (i == 0) {
                    row.add(Arrays.toString(available)); // Available
                } else {
                    row.add(""); // 后续行不重复显示 Available 数据
                }

                resourceTableModel.addRow(row);
            }
        });
    } // end updateResourceInfo()

    /**
     * 添加银行家算法运行结果
     */
    public void appendBankerRunningResult(String result) {
        SwingUtilities.invokeLater(() -> bankerOutputTextArea.append(result + "\n"));
    } // end appendBankerRunningResult()

    /**
     * 编辑资源信息
     */
    private void editResourceInfo() {
        int selectedRow = resourceTable.getSelectedRow();
        int selectedColumn = resourceTable.getSelectedColumn();
        String newValue = (String) resourceTableModel.getValueAt(selectedRow, selectedColumn);

        int[] newValues = Arrays.stream(newValue.replaceAll("[\\[\\]]", "").split(", "))
                .mapToInt(Integer::parseInt)
                .toArray(); // 解析新值为整数数组

        switch (selectedColumn) {
            case 1: // "Max"列
                banker.getMax()[selectedRow] = newValues;
                break;
            case 2: // "Allocation"列
                banker.getAllocation()[selectedRow] = newValues;
                break;
            case 4: // "Available"列
                banker.setAvailable(newValues);
                break;
        }
        banker.calculateNeed(); // 更新 need 数组

        bankerOutputTextArea.setText("【资源信息】\n");
        if (banker != null) bankerOutputTextArea.append(banker + "\n");
    } // end editResourceInfo()

    /**
     * 设置容器中所有操作组件的启用/禁用状态
     *
     * @param container 容器
     * @param enabled   是否启用
     */
    private void setButtonsEnabled(Container container, boolean enabled) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                comp.setEnabled(enabled);
            } else if (comp instanceof JComboBox<?>) {
                comp.setEnabled(enabled);
            } else if (comp instanceof JTable) {
                comp.setEnabled(enabled); // 禁用表格，但不阻止编辑
            } else if (comp instanceof Container) {
                setButtonsEnabled((Container) comp, enabled);
            }
        }
    } // end setButtonsEnabled()

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ControlGUI().setVisible(true));
    } // end main()
} // end class ControlGUI