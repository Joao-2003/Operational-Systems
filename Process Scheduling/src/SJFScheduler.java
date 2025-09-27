import java.util.*;

// 2. SJF (Shortest Job First) - Não-Preemptivo
class SJFScheduler implements Scheduler {
    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> readyQueue = new ArrayList<>();
        List<Process> finishedProcesses = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();
        int currentTime = 0;
        int n = processes.size();

        while (finishedProcesses.size() < n) {
            // Adiciona à fila de prontos todos os processos que já chegaram
            for (Process p : processes) {
                if (p.arrivalTime <= currentTime && !readyQueue.contains(p) && !finishedProcesses.contains(p)) {
                    readyQueue.add(p);
                }
            }

            if (readyQueue.isEmpty()) {
                // Nenhum processo pronto, CPU ociosa
                currentTime++;
                continue;
            }

            // Ordena a fila de prontos pelo menor tempo de execução (burst time)
            readyQueue.sort(Comparator.comparingInt(p -> p.burstTime));

            Process currentProcess = readyQueue.get(0);
            readyQueue.remove(0);

            currentProcess.waitingTime = currentTime - currentProcess.arrivalTime;
            currentProcess.completionTime = currentTime + currentProcess.burstTime;
            currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;

            currentTime = currentProcess.completionTime;
            finishedProcesses.add(currentProcess);
            executionOrder.add(currentProcess.id);
        }

        return new SchedulingResult("SJF (Shortest Job First)",
                String.join(" -> ", executionOrder),
                finishedProcesses);
    }
}