import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class DiningPhilosophers {

    // Configurações da simulação
    private static final int NUM_PHILOSOPHERS = 5;
    private static final int SIMULATION_TIME_MS = 10000; // 15 segundos

    public static void main(String[] args) {
        System.out.println("=== Início do Jantar dos Filósofos ===");
        System.out.println("Estratégia: Hierarquia de Recursos + Fair Locks");

        // 1. Inicializa os garfos (Recursos)
        Lock[] forks = initializeForks(NUM_PHILOSOPHERS);

        // 2. Inicia a sessão de jantar
        runDiningSession(forks);
    }

    /**
     * Cria e inicializa o array de garfos.
     * @param count Quantidade de garfos.
     * @return Um array de Locks configurados com justiça (fairness).
     */
    private static Lock[] initializeForks(int count) {
        Lock[] forks = new ReentrantLock[count];
        for (int i = 0; i < count; i++) {
            // 'true' ativa a política de justiça (Fairness) para evitar Starvation.
            // Garante que a thread esperando há mais tempo seja a próxima a pegar o lock.
            forks[i] = new ReentrantLock(true);
        }
        return forks;
    }

    /**
     * Gerencia a criação dos filósofos, execução das threads e encerramento.
     * @param forks O array de garfos já inicializado.
     */
    private static void runDiningSession(Lock[] forks) {
        // Executor usando Virtual Threads (Ideal para tarefas IO-bound ou com muito bloqueio)
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            List<Philosopher> philosophers = new ArrayList<>();

            for (int i = 0; i < NUM_PHILOSOPHERS; i++) {

                // Identifica garfos à esquerda e à direita baseados no índice
                Lock leftFork = forks[i];
                Lock rightFork = forks[(i + 1) % NUM_PHILOSOPHERS];

                // === PREVENÇÃO DE DEADLOCK (Hierarquia de Recursos) ===
                // Regra: Sempre pegar o garfo com o MENOR ID primeiro.
                // Isso quebra a espera circular (circular wait).

                Lock firstFork, secondFork;

                // O ID do garfo esquerdo (i) é menor que o direito (i+1), exceto para o último filósofo.
                if (i < (i + 1) % NUM_PHILOSOPHERS) {
                    firstFork = leftFork;   // Pega esquerda primeiro
                    secondFork = rightFork; // Pega direita depois
                } else {
                    // Caso especial: Último filósofo (ex: índice 4 e 0)
                    // Ele deve pegar o garfo 0 (direita) primeiro, pois 0 < 4.
                    firstFork = rightFork;
                    secondFork = leftFork;
                }

                Philosopher p = new Philosopher(i, firstFork, secondFork);
                philosophers.add(p);
                executor.submit(p);
            }

            // A thread principal dorme enquanto a simulação acontece
            try {
                Thread.sleep(SIMULATION_TIME_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("=== Fim do Tempo de Simulação. Encerrando... ===");

            // Sinaliza para os filósofos pararem
            for (Philosopher p : philosophers) {
                p.stop();
            }

        } // O try-with-resources fecha o executor automaticamente aqui
    }

    /**
     * Classe interna representando o Filósofo (Tarefa).
     */
    static class Philosopher implements Runnable {
        private final int id;
        private final Lock lowerLock;  // Primeiro lock a adquirir (ID menor)
        private final Lock higherLock; // Segundo lock a adquirir (ID maior)
        private final Random random = new Random();

        private volatile boolean running = true;
        private int mealsEaten = 0;

        public Philosopher(int id, Lock lowerLock, Lock higherLock) {
            this.id = id;
            this.lowerLock = lowerLock;
            this.higherLock = higherLock;
        }

        public void stop() {
            this.running = false;
        }

        private void performAction(String actionName) throws InterruptedException {
            // Tempo aleatório entre 50ms e 150ms
            int duration = random.nextInt(100) + 50;
            Thread.sleep(duration);
        }

        @Override
        public void run() {
            try {
                while (running) {
                    // 1. PENSAR
                    System.out.printf("Filósofo %d está pensando...\n", id);
                    performAction("Pensando");

                    // 2. TENTAR COMER (Adquirir recursos na ordem hierárquica)
                    lowerLock.lock();
                    try {
                        System.out.printf("Filósofo %d pegou o 1º garfo.\n", id);

                        higherLock.lock();
                        try {
                            System.out.printf("Filósofo %d pegou o 2º garfo e está COMENDO.\n", id);
                            mealsEaten++;
                            performAction("Comendo");
                        } finally {
                            System.out.printf("Filósofo %d soltou o 2º garfo.\n", id);
                            higherLock.unlock();
                        }
                    } finally {
                        System.out.printf("Filósofo %d soltou o 1º garfo.\n", id);
                        lowerLock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                System.out.printf("Filósofo %d foi interrompido.\n", id);
                Thread.currentThread().interrupt();
            }
            System.out.printf(">>> Filósofo %d foi embora. Total de refeições: %d\n", id, mealsEaten);
        }
    }
}