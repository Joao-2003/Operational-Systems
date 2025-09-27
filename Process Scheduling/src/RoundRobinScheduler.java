import java.util.*;

// 3. Round Robin (RR)
class RoundRobinScheduler implements Scheduler {
    private final int quantum;

    public RoundRobinScheduler(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public SchedulingResult schedule(List<Process> processes) {
        Queue<Process> readyQueue = new LinkedList<>();
        List<Process> finishedProcesses = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();
        int currentTime = 0;
        int nextProcessIndex = 0;

        // Ordena por chegada para facilitar a adição na fila
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        while (finishedProcesses.size() < processes.size()) {
            // Adiciona novos processos que chegaram na fila
            while (nextProcessIndex < processes.size() && processes.get(nextProcessIndex).arrivalTime <= currentTime) {
                readyQueue.add(processes.get(nextProcessIndex));
                nextProcessIndex++;
            }

            if (readyQueue.isEmpty()) {
                if (nextProcessIndex < processes.size()) {
                    currentTime = processes.get(nextProcessIndex).arrivalTime;
                } else {
                    break; // Todos os processos foram concluídos
                }
                continue;
            }

            Process currentProcess = readyQueue.poll();
            executionOrder.add(currentProcess.id);
            int timeToRun = Math.min(quantum, currentProcess.remainingTime);

            currentTime += timeToRun;
            currentProcess.remainingTime -= timeToRun;

            // Adiciona processos que chegaram enquanto o atual executava
            while (nextProcessIndex < processes.size() && processes.get(nextProcessIndex).arrivalTime <= currentTime) {
                readyQueue.add(processes.get(nextProcessIndex));
                nextProcessIndex++;
            }

            if (currentProcess.remainingTime > 0) {
                readyQueue.add(currentProcess); // Readiciona ao final da fila
            } else {
                currentProcess.completionTime = currentTime;
                currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                finishedProcesses.add(currentProcess);
            }
        }

        finishedProcesses.sort(Comparator.comparing(p -> p.id));
        return new SchedulingResult("Round Robin (RR) com Quantum=" + quantum,
                String.join(" -> ", executionOrder),
                finishedProcesses);
    }
}

