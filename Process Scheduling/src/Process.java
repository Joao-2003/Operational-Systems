// --- Classe para representar um Processo ---
// Armazena todos os atributos e métricas de um processo.
class Process {
    String id;
    int arrivalTime;
    int burstTime;
    int priority;

    // Variáveis para cálculos do escalonador
    int remainingTime;
    int completionTime;
    int waitingTime;
    int turnaroundTime;

    public Process(String id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime; // Tempo restante é inicialmente o tempo de execução
    }

    // Construtor de cópia para evitar que um algoritmo modifique os dados do outro
    public Process(Process other) {
        this.id = other.id;
        this.arrivalTime = other.arrivalTime;
        this.burstTime = other.burstTime;
        this.priority = other.priority;
        this.remainingTime = other.burstTime;
    }

    @Override
    public String toString() {
        return String.format("  %-10s %-15d %-15d", id, waitingTime, turnaroundTime);
    }
}