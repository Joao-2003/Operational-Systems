import java.util.List;

// --- Interface para os Algoritmos de Escalonamento ---
interface Scheduler {
    SchedulingResult schedule(List<Process> processes);
}
