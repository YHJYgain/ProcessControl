import java.util.*;

/**
 * 进程调度器抽象类
 *
 * @author wzy
 * @date 2024-02-03 16:04:18
 */
public abstract class Scheduler {
    Queue<PCB> readyQueue = new LinkedList<>(); // 就绪队列
    Queue<PCB> waitQueue = new LinkedList<>(); // 等待队列
    List<PCB> finishedProcesses = new ArrayList<>(); // 完成进程

    int time = 0; // 运行时间
    PCB currentProcess; // 当前运行进程
    Queue<Integer> priorityBackup = new LinkedList<>(); // 优先数备份（用于 HPF 运行之后重置优先数状态）

    abstract void createProcess(PCB process); // 创建进程

    abstract void killProcess(PCB process); // 撤销进程

    abstract String schedule(); // 进程调度

    /**
     * 显示各进程的运行情况
     *
     * @param priorityQueue 优先队列
     */
    String displaySchedulingStatus(PriorityQueue<PCB> priorityQueue) {
        StringBuilder status = new StringBuilder();
        /* 显示就绪队列中的 PCB */
        if (!priorityQueue.isEmpty()) {
            status.append("就绪队列：");
            for (PCB process : priorityQueue) {
                if (process.getState() == 'R') {
                    status.append(process.getName()).append(" ");
                }
            }
            status.append("\n");
        } else status.append("就绪队列：\n");

        /* 显示等待队列中的 PCB */
        if (!waitQueue.isEmpty()) {
            status.append("等待队列：");
            for (PCB process : waitQueue) {
                status.append(process.getName()).append(" ");
            }
            status.append("\n");
        } else status.append("等待队列：\n");

        /* 显示已完成进程的 PCB */
        if (!finishedProcesses.isEmpty()) {
            status.append("完成进程：");
            for (PCB process : finishedProcesses) {
                status.append(process.getName()).append(" ");
            }
            status.append("\n");
        } else status.append("完成进程：\n");

        status.append("-------------------------------".repeat(5));

        return status.toString();
    } // end displaySchedulingStatus()

    /**
     * 重置调度状态（以便下次调度）
     *
     * @param priorityQueue 优先队列
     */
    void resetSchedulingStatus(Queue<PCB> priorityQueue) {
        time = 0;
        currentProcess = null;
        Queue<Integer> temp = priorityBackup;
        for (PCB process : readyQueue) {
            if (!temp.isEmpty()) {
                process.setPriority(temp.poll());
            }
            process.setUsedCPUTime(0);
            process.setState('R');
            if (priorityQueue != null) {
                priorityQueue.offer(process);
            }
        }
        waitQueue.clear();
        finishedProcesses.clear();
    } // end resetSchedulingStatus()
} // end abstract class Scheduler
