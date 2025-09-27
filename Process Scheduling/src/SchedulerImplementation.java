import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
// --- Classe Principal para Execução ---
public class SchedulerImplementation {

    public static void main(String[] args) {
        // Lista inicial de processos
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 5, 2),
                new Process("P2", 2, 3, 1),
                new Process("P3", 4, 8, 3),
                new Process("P4", 5, 6, 2),
                new Process("P5", 11, 8, 1)
        );

        // Lista de algoritmos a serem executados
        List<Scheduler> schedulers = Arrays.asList(
                new FCFSScheduler(),
                new SJFScheduler(),
                new RoundRobinScheduler(2), // Quantum = 2
                new PriorityScheduler(),
                new MultiLevelQueueScheduler(),
                new LotteryScheduler()
        );

        StringBuilder fileContent = new StringBuilder();

        // Executa cada algoritmo e armazena os resultados
        for (Scheduler scheduler : schedulers) {
            // Cria uma cópia da lista de processos para cada algoritmo
            List<Process> processesCopy = processes.stream()
                    .map(Process::new)
                    .collect(Collectors.toList());

            SchedulingResult result = scheduler.schedule(processesCopy);
            String formattedResult = result.getFormattedResult();

            System.out.println(formattedResult);
            fileContent.append(formattedResult).append("\n");
        }

        // Grava os resultados em um arquivo de texto
        writeResultsToFile("resultados_escalonamento.txt", fileContent.toString());
    }

    private static void writeResultsToFile(String fileName, String content) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("### RESULTADOS DA SIMULAÇÃO DE ESCALONAMENTO DE PROCESSOS ###");
            writer.println();
            writer.print(content);
            System.out.println("Resultados gravados com sucesso no arquivo '" + fileName + "'");
        } catch (IOException e) {
            System.err.println("Erro ao gravar resultados no arquivo: " + e.getMessage());
        }
    }
}