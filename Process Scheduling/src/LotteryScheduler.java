import java.util.*;

// 6. Lottery Scheduling
class LotteryScheduler implements Scheduler {
    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> readyProcesses = new ArrayList<>();
        List<Process> finishedProcesses = new ArrayList<>();
        StringBuilder executionOrder = new StringBuilder();
        Random random = new Random();
        int currentTime = 0;
        int n = processes.size();

        while (finishedProcesses.size() < n) {
            // Adiciona processos que chegaram à lista de prontos
            for (Process p : processes) {
                if (p.arrivalTime <= currentTime && p.remainingTime > 0 && !readyProcesses.contains(p)) {
                    readyProcesses.add(p);
                }
            }

            if (readyProcesses.isEmpty()) {
                currentTime++;
                continue;
            }

            // Calcula o total de bilhetes (usando prioridade, maior prioridade = mais bilhetes)
            int totalTickets = readyProcesses.stream().mapToInt(p -> 11 - p.priority).sum(); // Prioridade 1 -> 10 tickets
            if (totalTickets == 0) {
                currentTime++;
                continue;
            }

            int winningTicket = random.nextInt(totalTickets);

            Process winner = null;
            int cumulativeTickets = 0;
            for (Process p : readyProcesses) {
                cumulativeTickets += (11 - p.priority);
                if (winningTicket < cumulativeTickets) {
                    winner = p;
                    break;
                }
            }

            if (winner != null) {
                if (executionOrder.length() == 0 || !executionOrder.substring(executionOrder.length() - 4).contains(winner.id)) {
                    executionOrder.append(winner.id).append(" -> ");
                }

                winner.remainingTime--;
                currentTime++;

                if (winner.remainingTime == 0) {
                    winner.completionTime = currentTime;
                    winner.turnaroundTime = winner.completionTime - winner.arrivalTime;
                    winner.waitingTime = winner.turnaroundTime - winner.burstTime;
                    finishedProcesses.add(winner);
                    readyProcesses.remove(winner);
                }
            } else {
                currentTime++; // Se nenhum vencedor for selecionado por alguma razão
            }
        }

        finishedProcesses.sort(Comparator.comparing(p -> p.id));
        String finalExecutionOrder = executionOrder.length() > 4 ? executionOrder.substring(0, executionOrder.length() - 4) : "";

        return new SchedulingResult("Lottery Scheduling", finalExecutionOrder, finishedProcesses);
    }
}
