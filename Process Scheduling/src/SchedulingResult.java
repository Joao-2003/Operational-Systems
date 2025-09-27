import java.util.List;

// --- Classe para encapsular os resultados de um escalonamento ---
class SchedulingResult {
    String algorithmName;
    String executionOrder;
    List<Process> finishedProcesses;
    double averageWaitingTime;
    double averageTurnaroundTime;

    public SchedulingResult(String algorithmName, String executionOrder, List<Process> finishedProcesses) {
        this.algorithmName = algorithmName;
        this.executionOrder = executionOrder;
        this.finishedProcesses = finishedProcesses;
        calculateAverages();
    }

    private void calculateAverages() {
        if (finishedProcesses == null || finishedProcesses.isEmpty()) {
            this.averageWaitingTime = 0;
            this.averageTurnaroundTime = 0;
            return;
        }
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        for (Process p : finishedProcesses) {
            totalWaitingTime += p.waitingTime;
            totalTurnaroundTime += p.turnaroundTime;
        }
        this.averageWaitingTime = totalWaitingTime / finishedProcesses.size();
        this.averageTurnaroundTime = totalTurnaroundTime / finishedProcesses.size();
    }

    // Formata os resultados para exibição no console e gravação em arquivo
    public String getFormattedResult() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALGORITMO: ").append(algorithmName).append("\n");
        sb.append("Ordem de Execução: ").append(executionOrder).append("\n\n");
        sb.append(String.format("  %-10s %-15s %-15s\n", "Processo", "Tempo de Espera", "Tempo de Retorno"));
        sb.append("  ----------------------------------------\n");
        for (Process p : finishedProcesses) {
            sb.append(p.toString()).append("\n");
        }
        sb.append("\n");
        sb.append(String.format("Tempo Médio de Espera: %.2f\n", averageWaitingTime));
        sb.append(String.format("Tempo Médio de Retorno: %.2f\n", averageTurnaroundTime));
        sb.append("----------------------------------------\n");
        return sb.toString();
    }
}
