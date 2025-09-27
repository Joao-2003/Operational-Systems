import java.util.*;
import java.util.stream.Collectors;

// 1. FCFS (First-Come, First-Served)
class FCFSScheduler implements Scheduler {
    @Override
    public SchedulingResult schedule(List<Process> processes) {
        // Ordena os processos por tempo de chegada
        List<Process> sortedProcesses = processes.stream()
                .sorted(Comparator.comparingInt(p -> p.arrivalTime))
                .collect(Collectors.toList());

        List<String> executionOrder = new ArrayList<>();
        int currentTime = 0;

        for (Process p : sortedProcesses) {
            // Se a CPU estiver ociosa, avança o tempo para a chegada do processo
            if (currentTime < p.arrivalTime) {
                currentTime = p.arrivalTime;
            }

            p.waitingTime = currentTime - p.arrivalTime;
            p.completionTime = currentTime + p.burstTime;
            p.turnaroundTime = p.completionTime - p.arrivalTime;

            currentTime = p.completionTime;
            executionOrder.add(p.id);
        }

        return new SchedulingResult("FCFS (First-Come, First-Served)",
                String.join(" -> ", executionOrder),
                sortedProcesses);
    }
}