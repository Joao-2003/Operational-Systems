import java.util.*;

// Classe principal que executa a simulação
public class MemoryManagementSimulator {

    public static void main(String[] args) {
        final int MEMORY_SIZE = 32;
        final int SIMULATION_STEPS = 30;

        // Lista de processos disponíveis
        List<Process> processes = Arrays.asList(
                new Process("P1", 5), new Process("P2", 4), new Process("P3", 2),
                new Process("P4", 5), new Process("P5", 8), new Process("P6", 3),
                new Process("P7", 5), new Process("P8", 8), new Process("P9", 2),
                new Process("P10", 6)
        );

        // Algoritmos a serem simulados
        String[] algorithms = {"First Fit", "Next Fit", "Best Fit", "Worst Fit", "Quick Fit"};
        Random random = new Random();

        for (String algorithm : algorithms) {
            System.out.println("\n=========================================================");
            System.out.println("INICIANDO SIMULAÇÃO COM O ALGORITMO: " + algorithm);
            System.out.println("=========================================================");

            MemoryManager manager = new MemoryManager(MEMORY_SIZE);
            System.out.println("Estado inicial da memória:");
            manager.printMemoryMap();
            System.out.println("---------------------------------------------------------");

            for (int i = 1; i <= SIMULATION_STEPS; i++) {
                // Sorteia um processo aleatório da lista
                Process randomProcess = processes.get(random.nextInt(processes.size()));

                System.out.println("\nPasso " + i + ": Processo sorteado -> " + randomProcess);

                if (manager.isProcessAllocated(randomProcess)) {
                    System.out.println("Processo " + randomProcess.getId() + " já está na memória. Desalocando...");
                    manager.deallocate(randomProcess);
                } else {
                    System.out.println("Processo " + randomProcess.getId() + " não está na memória. Tentando alocar...");
                    manager.allocate(randomProcess, algorithm);
                }

                manager.printMemoryMap();
                manager.printFragmentationStats(processes);
                System.out.println("---------------------------------------------------------");
            }
        }
    }
}