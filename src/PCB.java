/**
 * 进程控制块（PCB）类
 *
 * @author wzy
 * @date 2024-02-03 14:53:47
 */
public class PCB {
    private String name; // 进程名
    private int priority; // 优先数
    private int arrivalTime; // 到达时间
    private int requiredRuntime; // 需要运行时间
    private int usedCPUTime; // 已用 CPU 时间
    private char state; // 进程状态，E: 执行，R: 就绪，W: 等待，F: 完成

    public PCB(String name, int priority, int arrivalTime, int requiredRuntime) {
        this.name = name;
        this.priority = priority;
        this.arrivalTime = arrivalTime;
        this.requiredRuntime = requiredRuntime;
        this.usedCPUTime = 0;
        this.state = 'R'; // 初始状态设为就绪
    } // end PCB()

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getRequiredRuntime() {
        return requiredRuntime;
    }

    public void setRequiredRuntime(int requiredRuntime) {
        this.requiredRuntime = requiredRuntime;
    }

    public int getUsedCPUTime() {
        return usedCPUTime;
    }

    public void setUsedCPUTime(int usedCPUTime) {
        this.usedCPUTime = usedCPUTime;
    }

    public char getState() {
        return state;
    }

    public void setState(char state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return String.format("%s（优先数=%d, 到达时间=%d, 需要运行时间=%d, 已用 CPU 时间=%d, 进程状态=%c）", name, priority,
                arrivalTime, requiredRuntime, usedCPUTime, state);
    } // end toString()
} // end class PCB
