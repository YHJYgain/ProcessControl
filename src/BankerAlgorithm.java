import java.util.*;

/**
 * 银行家算法类
 *
 * @author wzy
 * @date 2024-02-03 16:58:04
 */
public class BankerAlgorithm {
    private int[] available; // 系统可用资源数（剩余资源量）
    private int[][] max; // 各进程最大需求量
    private int[][] allocation; // 各进程已占用资源数
    private int[][] need; // 各进程还需资源数

    private boolean[] finish; // 记录进程完成状态
    private StringBuilder result; // 算法运行结果
    private List<Integer> safeSequence; // 安全序列

    public BankerAlgorithm(int[] available, int[][] max, int[][] allocation) {
        this.available = available;
        this.max = max;
        this.allocation = allocation;
        this.need = new int[max.length][available.length];
        this.finish = new boolean[max.length];
        calculateNeed();
    } // end BankerAlgorithm()

    /**
     * 计算进程尚需量
     */
    public void calculateNeed() {
        for (int i = 0; i < max.length; i++) {
            for (int j = 0; j < available.length; j++) {
                need[i][j] = max[i][j] - allocation[i][j];
            }
        }
    } // end calculateNeed()

    /**
     * 尝试为进程分配资源
     *
     * @param processIndex 进程下标
     * @param request      进程请求向量
     * @return 能否进行试分配
     */
    public String tryAllocate(int processIndex, int[] request) {
        result = new StringBuilder();

        result.append("试分配：").append(processIndex).append(" ==> ").append(Arrays.toString(request)).append("\n");
        if (!checkRequestValid(processIndex, request)) {
            result.append("无法试分配，请求超出了系统可用资源数或进程的还需资源数。");
            return result.toString();
        }
        applyRequest(processIndex, request);

        /* 检查安全性 */
        if (checkSafety()) {
            result.append("资源试分配成功，系统处于安全状态。\n");
            result.append(this).append("\n");
            result.append("-------------------------------".repeat(5)).append("\n");
            safeSequence = findSafeSequence();
            result.append("安全序列：").append(safeSequence).append("\n");
        } else {
            result.append("资源试分配失败，不存在安全序列，若分配会导致死锁。\n");
            rollbackRequest(processIndex, request);
            result.append("系统已回退至未试分配状态。\n");
        }

        result.append("-------------------------------".repeat(5)).append("\n");
        result.append("【银行家算法运行结束】");

        return result.toString();
    } // end tryAllocate()

    /**
     * 执行资源分配
     *
     * @param processIndex 进程下标
     * @param request      进程请求向量
     */
    private void applyRequest(int processIndex, int[] request) {
        for (int i = 0; i < available.length; i++) {
            available[i] -= request[i];
            allocation[processIndex][i] += request[i];
            need[processIndex][i] -= request[i];
        }
    } // end applyRequest()

    /**
     * 回滚资源分配
     *
     * @param processIndex 进程下标
     * @param request      进程请求向量
     */
    private void rollbackRequest(int processIndex, int[] request) {
        for (int i = 0; i < available.length; i++) {
            available[i] += request[i];
            allocation[processIndex][i] -= request[i];
            need[processIndex][i] += request[i];
        }
    } // end rollbackRequest()

    /**
     * 检查请求是否有效
     *
     * @param processIndex 进程下标
     * @param request      进程请求向量
     * @return 请求是否有效
     */
    private boolean checkRequestValid(int processIndex, int[] request) {
        for (int i = 0; i < available.length; i++) {
            if (request[i] > need[processIndex][i] || request[i] > available[i]) {
                return false;
            }
        }
        return true;
    } // end checkRequestValid()

    /**
     * 检查系统是否处于安全状态
     *
     * @return 系统是否处于安全状态
     */
    public boolean checkSafety() {
        int[] work = Arrays.copyOf(available, available.length);
        finish = new boolean[max.length];
        Arrays.fill(finish, false);

        while (true) {
            boolean foundProcess = false;
            for (int i = 0; i < max.length; i++) {
                if (!finish[i] && checkProcessCanFinish(i, work)) {
                    for (int j = 0; j < available.length; j++) {
                        work[j] += allocation[i][j];
                    }
                    finish[i] = true;
                    foundProcess = true;
                }
            }
            if (!foundProcess) {
                break;
            }
        }

        for (boolean b : finish) {
            if (!b) {
                return false;
            }
        }
        return true;
    } // end checkSafety()

    /**
     * 检查一个进程是否可以运行完成
     *
     * @param processIndex 进程下标
     * @param work         资源工作向量
     * @return 进程是否可以运行完成
     */
    private boolean checkProcessCanFinish(int processIndex, int[] work) {
        for (int i = 0; i < available.length; i++) {
            if (need[processIndex][i] > work[i]) {
                return false;
            }
        }
        return true;
    } // end checkProcessCanFinish()

    /**
     * 求安全序列
     *
     * @return 安全序列
     */
    private List<Integer> findSafeSequence() {
        safeSequence = new ArrayList<>();
        int[] availableBackup = Arrays.copyOf(available, available.length);
        int[][] allocationBackup = Arrays.stream(allocation)
                .map(a -> Arrays.copyOf(a, a.length))
                .toArray(int[][]::new);
        finish = new boolean[max.length];
        Arrays.fill(finish, false);

        while (safeSequence.size() < max.length) {
            boolean foundProcess = false;
            for (int i = 0; i < max.length; i++) {
                if (!finish[i] && checkProcessCanFinish(i, available)) {
                    for (int j = 0; j < available.length; j++) {
                        available[j] += allocation[i][j];
                        allocation[i][j] = 0;
                        need[i][j] = 0;
                    }
                    finish[i] = true;
                    safeSequence.add(i);
                    foundProcess = true;

                    result.append("运行进程：").append(i).append(" \n");
                    result.append(this).append("\n");
                    result.append("-------------------------------".repeat(5)).append("\n");
                }
            }

            if (!foundProcess) {
                return new ArrayList<>(); // 如果没有找到可执行的进程，则返回空列表
            }
        }

        /* 回滚至求安全序列前（试分配后）系统状态 */
        this.setAvailable(availableBackup);
        this.setAllocation(allocationBackup);
        calculateNeed();

        return safeSequence;
    } // end findSafeSequence()

    public int[] getAvailable() {
        return available;
    }

    public void setAvailable(int[] available) {
        this.available = available;
    }

    public int[][] getMax() {
        return max;
    }

    public void setMax(int[][] max) {
        this.max = max;
    }

    public int[][] getAllocation() {
        return allocation;
    }

    public void setAllocation(int[][] allocation) {
        this.allocation = allocation;
    }

    public int[][] getNeed() {
        return need;
    }

    public void setNeed(int[][] need) {
        this.need = need;
    }

    public boolean[] getFinish() {
        return finish;
    }

    public void setFinish(boolean[] finish) {
        this.finish = finish;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Available = ").append(Arrays.toString(available)).append("\n");
        sb.append("PID\t\tMax\t\tAllocation\t\tNeed\n");
        for (int i = 0; i < max.length; i++) {
            sb.append(i).append("\t".repeat(2));
            sb.append(Arrays.toString(max[i])).append("\t".repeat(2));
            sb.append(Arrays.toString(allocation[i])).append("\t".repeat(2));

            /* 资源矩阵最后一行需要换行，以便后续信息显示 */
            if (i == max.length - 1) {
                sb.append(Arrays.toString(need[i]));
            } else sb.append(Arrays.toString(need[i])).append("\n");
        }

        return sb.toString();
    } // end toString()
} // end class BankerAlgorithm