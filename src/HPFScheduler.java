import java.util.*;

/**
 * 最高优先数（HPF）调度器类
 *
 * @author wzy
 * @date 2024-02-03 16:52:40
 */
public class HPFScheduler extends Scheduler {
    private PriorityQueue<PCB> priorityQueue = new PriorityQueue<>(
            Comparator.comparingInt(p -> -p.getPriority())); // 优先数队列（按优先数倒序排列）

    @Override
    public void createProcess(PCB process) {
        priorityBackup.offer(process.getPriority());
        readyQueue.offer(process);
        priorityQueue.offer(process);
    } // end createProcess()

    @Override
    public void killProcess(PCB process) {
        readyQueue.remove(process);
        priorityQueue.remove(process);
    } // end killProcess()

    @Override
    public String schedule() {
        StringBuilder result = new StringBuilder();

        /* 每次调度都备份一次优先数 */
        for (PCB process : readyQueue) {
            priorityBackup.offer(process.getPriority());
        }

        if (!priorityQueue.isEmpty()) {
            /* HPF 调度也要考虑到达时间 */
            for (PCB process : priorityQueue) {
                if (process.getArrivalTime() <= time) {
                    currentProcess = process; // 只有当进程到达且优先数最高时，才可为进程分配 CPU
                    break;
                }
            }

            if (currentProcess != null) {
                priorityQueue.remove(currentProcess);
                if (currentProcess.getRequiredRuntime() <= 0) { // 处理进程一开始的需要运行时间为0
                    currentProcess.setState('F');
                    finishedProcesses.add(currentProcess);
                } else {
                    currentProcess.setState('E');
                    result.append("运行进程：").append(currentProcess.getName()).append("\n");
                    result.append(displaySchedulingStatus(priorityQueue)).append("\n");

                    /* 模拟执行一个时间片 */
                    currentProcess.setUsedCPUTime(currentProcess.getUsedCPUTime() + 1);
                    if (currentProcess.getUsedCPUTime() >= currentProcess.getRequiredRuntime()) {
                        currentProcess.setState('F');
                        finishedProcesses.add(currentProcess);
                    } else {
                        currentProcess.setPriority(currentProcess.getPriority() - 1); // 优先级降低
                        currentProcess.setState('R'); // 设为就绪状态
                        priorityQueue.offer(currentProcess); // 再次加入优先数队列
                    }
                }
            }
            /*
             * 对于 HPF，若未找到当前能够运行进程，则默认为优先数队列队首元素
             * （因为本系统是根据当前是否有能够运行进程来判断进程调度是否结束）
             */
            else currentProcess = priorityQueue.peek();

            time++;
        } else { // 进程调度结束，立即将数据重置，以便下一次重新调度
            result.append("运行进程：\n");
            result.append(displaySchedulingStatus(priorityQueue)).append("\n");
            result.append("【进程调度结束】\n");
            resetSchedulingStatus(priorityQueue);
        }

        return result.toString();
    } // end schedule()
} // end class HPFScheduler
