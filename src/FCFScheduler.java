import java.util.*;

/**
 * 先来先服务（FCFS）调度器类
 *
 * @author wzy
 * @date 2024-02-03 16:52:35
 */
public class FCFScheduler extends Scheduler {
    private PriorityQueue<PCB> arrivalQueue = new PriorityQueue<>(
            Comparator.comparingInt(PCB::getArrivalTime)); // 到达队列（按到达时间顺序排列）

    @Override
    public void createProcess(PCB process) {
        readyQueue.offer(process);
        arrivalQueue.offer(process);
    } // end createProcess()

    @Override
    public void killProcess(PCB process) {
        readyQueue.remove(process);
        arrivalQueue.remove(process);
    } // end killProcess()

    @Override
    public String schedule() {
        StringBuilder result = new StringBuilder();

        if (!arrivalQueue.isEmpty()) {
            currentProcess = arrivalQueue.poll();
            if (currentProcess.getRequiredRuntime() <= 0) { // 处理进程一开始的需要运行时间为0
                currentProcess.setState('F');
                finishedProcesses.add(currentProcess);
            } else {
                currentProcess.setState('E'); // 设为执行状态
                result.append("运行进程：").append(currentProcess.getName()).append("\n");
                result.append(displaySchedulingStatus(arrivalQueue)).append("\n");

                /* 模拟执行一个时间片 */
                currentProcess.setUsedCPUTime(currentProcess.getUsedCPUTime() + 1);
                if (currentProcess.getUsedCPUTime() >= currentProcess.getRequiredRuntime()) {
                    currentProcess.setState('F');
                    finishedProcesses.add(currentProcess);
                } else {
                    currentProcess.setState('R');
                    arrivalQueue.offer(currentProcess); // 优先数不变，再次加入就绪队列
                }
            }
        } else { // 进程调度结束，立即将数据重置，以便下一次重新调度
            result.append("运行进程：\n");
            result.append(displaySchedulingStatus(arrivalQueue)).append("\n");
            result.append("【进程调度结束】\n");
            resetSchedulingStatus(arrivalQueue);
        }

        return result.toString();
    } // end schedule()
} // end class FCFScheduler
