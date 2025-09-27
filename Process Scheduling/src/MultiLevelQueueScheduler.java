import java.util.*;

// 5. Priority Scheduling - Multiple Queues
class MultiLevelQueueScheduler implements Scheduler {
    private final int quantumHighPriority = 2; // Quantum para fila de alta prioridade

    @Override
    public SchedulingResult schedule(List<Process> processes) {
        // Prioridade 1: Round Robin, Prioridade > 1: FCFS
        Queue<Process> highPriorityQueue = new LinkedList<>(); // Prioridade == 1
        Queue<Process> lowPriorityQueue = new LinkedList<>(); // Prioridade > 1

        List<Process> finishedProcesses = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();
        int currentTime = 0;

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        List<Process> remainingProcesses = new ArrayList<>(processes);

        while (finishedProcesses.size() < processes.size()) {
            // Move processos que chegaram para as filas apropriadas
            Iterator<Process> iterator = remainingProcesses.iterator();
            while(iterator.hasNext()) {
                Process p = iterator.next();
                if (p.arrivalTime <= currentTime) {
                    if (p.priority == 1) {
                        highPriorityQueue.add(p);
                    } else {
                        lowPriorityQueue.add(p);
                    }
                    iterator.remove();
                }
            }

            Process currentProcess = null;
            if (!highPriorityQueue.isEmpty()) {
                // Executa da fila de alta prioridade (Round Robin)
                currentProcess = highPriorityQueue.poll();
                executionOrder.add(currentProcess.id);
                int timeToRun = Math.min(quantumHighPriority, currentProcess.remainingTime);
                currentTime += timeToRun;
                currentProcess.remainingTime -= timeToRun;

                // Readiciona se não terminou
                if (currentProcess.remainingTime > 0) {
                    highPriorityQueue.add(currentProcess);
                } else {
                    finishProcess(currentProcess, currentTime, finishedProcesses);
                }
            } else if (!lowPriorityQueue.isEmpty()) {
                // Executa da fila de baixa prioridade (FCFS)
                currentProcess = lowPriorityQueue.poll();
                executionOrder.add(currentProcess.id);
                currentTime += currentProcess.remainingTime;
                currentProcess.remainingTime = 0;
                finishProcess(currentProcess, currentTime, finishedProcesses);
            } else {
                // CPU ociosa
                currentTime++;
            }
        }

        finishedProcesses.sort(Comparator.comparing(p -> p.id));
        return new SchedulingResult("Priority Scheduling - Multiple Queues",
                String.join(" -> ", executionOrder),
                finishedProcesses);
    }

    private void finishProcess(Process p, int completionTime, List<Process> finishedList) {
        p.completionTime = completionTime;
        p.turnaroundTime = p.completionTime - p.arrivalTime;
        p.waitingTime = p.turnaroundTime - p.burstTime;
        finishedList.add(p);
    }
}