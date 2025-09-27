import java.util.*;

// 4. Priority Scheduling (Não-Preemptivo)
class PriorityScheduler implements Scheduler {
    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> readyQueue = new ArrayList<>();
        List<Process> finishedProcesses = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();
        int currentTime = 0;
        int n = processes.size();

        while (finishedProcesses.size() < n) {
            // Adiciona processos que já chegaram à fila de prontos
            for (Process p : processes) {
                if (p.arrivalTime <= currentTime && !readyQueue.contains(p) && !finishedProcesses.contains(p)) {
                    readyQueue.add(p);
                }
            }

            if (readyQueue.isEmpty()) {
                currentTime++;
                continue;
            }

            // Ordena por prioridade (menor número = maior prioridade)
            readyQueue.sort(Comparator.comparingInt(p -> p.priority));

            Process currentProcess = readyQueue.get(0);
            readyQueue.clear(); // Limpa a fila pois a escolha pode mudar no próximo instante de tempo

            currentProcess.waitingTime = currentTime - currentProcess.arrivalTime;
            currentProcess.completionTime = currentTime + currentProcess.burstTime;
            currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;

            currentTime = currentProcess.completionTime;
            finishedProcesses.add(currentProcess);
            executionOrder.add(currentProcess.id);
        }

        return new SchedulingResult("Priority Scheduling (Não-Preemptivo)",
                String.join(" -> ", executionOrder),
                finishedProcesses);
    }
}
