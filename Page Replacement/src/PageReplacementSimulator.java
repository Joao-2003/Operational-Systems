import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Classe principal que executa a simulação dos algoritmos de substituição de páginas.
 */
public class PageReplacementSimulator {

    // Constantes da simulação
    private static final int RAM_SIZE = 10;
    private static final int SWAP_SIZE = 100;
    private static final int NUM_INSTRUCTIONS = 1000;
    private static final int RESET_R_INTERVAL = 10;

    // Matrizes de memória
    private Page[] ram = new Page[RAM_SIZE];
    private Page[] swap = new Page[SWAP_SIZE];

    // Ferramentas e contadores
    private Random random = new Random();
    private int pageFaults;
    private int writeBacks; // Contagem de "salvamentos" em SWAP (Obs5)

    // Ponteiros para algoritmos
    private int fifoPointer = 0;
    private int clockPointer = 0;

    // Estrutura auxiliar para o LRU
    // Armazena o "tempo" (número da instrução) do último acesso
    private long[] lruTimestamps = new long[RAM_SIZE];

    /**
     * Ponto de entrada principal.
     */
    public static void main(String[] args) {
        PageReplacementSimulator simulator = new PageReplacementSimulator();

        String[] algorithms = {"NRU", "FIFO", "FIFO-SC", "CLOCK", "LRU", "WS-CLOCK"};

        for (String alg : algorithms) {
            simulator.runSimulation(alg);
        }
    }

    /**
     * Executa uma simulação completa (1000 instruções) para um determinado algoritmo.
     */
    public void runSimulation(String algorithmName) {
        System.out.println("\n=======================================================");
        System.out.println("Iniciando Simulação para o Algoritmo: " + algorithmName);
        System.out.println("=======================================================");

        // 1. Inicializa memórias e contadores
        initializeSwap();
        initializeRam();
        pageFaults = 0;
        writeBacks = 0;
        fifoPointer = 0;
        clockPointer = 0;
        Arrays.fill(lruTimestamps, 0);

        // 2. (Obs6) Imprime estado inicial
        System.out.println("--- ESTADO INICIAL ---");
        printMatrix(ram, "RAM (Memória Principal)");
        // printMatrix(swap, "SWAP (Memória Secundária)"); // Descomente para ver o SWAP inicial

        // 3. Loop principal de simulação (Obs1)
        for (int i = 1; i <= NUM_INSTRUCTIONS; i++) {

            // Sorteia a instrução (1 a 100)
            int instruction = random.nextInt(100) + 1;

            // Procura a página na RAM
            int ramIndex = findPageInRam(instruction);

            if (ramIndex != -1) {
                // --- PAGE HIT ---
                handlePageHit(ramIndex, i);
            } else {
                // --- PAGE FAULT ---
                handlePageFault(instruction, algorithmName, i);
            }

            // (Obs4) A cada 10 instruções, zera os bits R
            if (i % RESET_R_INTERVAL == 0) {
                resetRBits();
            }
        }

        // 4. (Obs6) Imprime estado final e estatísticas
        System.out.println("\n--- ESTADO FINAL (" + algorithmName + ") ---");
        printMatrix(ram, "RAM (Memória Principal)");
        // printMatrix(swap, "SWAP (Memória Secundária)"); // Descomente para ver o SWAP final
        System.out.println("\nEstatísticas (" + algorithmName + "):");
        System.out.println("Total de Instruções: " + NUM_INSTRUCTIONS);
        System.out.println("Total de Page Faults: " + pageFaults);
        System.out.println("Total de Write Backs (Escritas em SWAP): " + writeBacks);
    }

    /**
     * Preenche a matriz SWAP conforme as regras.
     */
    private void initializeSwap() {
        for (int i = 0; i < SWAP_SIZE; i++) {
            int N = i;
            int I = i + 1;
            int D = random.nextInt(50) + 1;  // 1 a 50
            int R = 0;
            int M = 0;
            int T = random.nextInt(9900) + 100; // 100 a 9999
            swap[i] = new Page(N, I, D, R, M, T);
        }
    }

    /**
     * Preenche a matriz RAM com 10 páginas aleatórias e únicas do SWAP.
     */
    private void initializeRam() {
        Set<Integer> selectedPages = new HashSet<>();
        for (int i = 0; i < RAM_SIZE; i++) {
            int pageN;
            // Garante que a página sorteada ainda não está na RAM
            do {
                pageN = random.nextInt(SWAP_SIZE); // Sorteia N (0 a 99)
            } while (selectedPages.contains(pageN));

            selectedPages.add(pageN);

            // Copia a página do SWAP para a RAM (usando construtor de cópia)
            ram[i] = new Page(swap[pageN]);
            lruTimestamps[i] = 0; // Inicializa o timestamp LRU
        }
    }

    /**
     * Procura uma página na RAM baseada no número da instrução (I).
     * @return O índice na RAM (0-9) se encontrada, ou -1 se for page fault.
     */
    private int findPageInRam(int instruction) {
        for (int i = 0; i < RAM_SIZE; i++) {
            if (ram[i].I == instruction) {
                return i; // Page Hit
            }
        }
        return -1; // Page Fault
    }

    /**
     * Ações a serem tomadas em um Page Hit.
     * @param ramIndex O índice da página na RAM.
     * @param instructionTime O número da instrução atual (usado pelo LRU).
     */
    private void handlePageHit(int ramIndex, int instructionTime) {
        // 1) Bit de acesso R = 1
        ram[ramIndex].R = 1;

        // Atualiza o timestamp do LRU
        lruTimestamps[ramIndex] = instructionTime;

        // 2) 50% de chance de modificação
        if (random.nextDouble() < 0.5) {
            // 2.1) D = D + 1
            ram[ramIndex].D++;
            // 2.2) M = 1
            ram[ramIndex].M = 1;
        }
    }

    /**
     * Ações a serem tomadas em um Page Fault.
     * @param instruction O número da instrução (I) que causou a falta.
     * @param algorithmName O algoritmo de substituição a ser usado.
     * @param instructionTime O número da instrução atual (usado pelo LRU).
     */
    private void handlePageFault(int instruction, String algorithmName, int instructionTime) {
        pageFaults++;

        // 1. Encontra a página a ser removida (vítima)
        int victimIndex = findVictim(algorithmName);
        Page victimPage = ram[victimIndex];

        // 2. (Obs5) Verifica se a página vítima está "suja" (M=1)
        if (victimPage.M == 1) {
            writeBacks++;
            writePageToSwap(victimPage);
        }

        // 3. Busca a nova página no SWAP
        // (Assumindo que I = N + 1, então N = I - 1)
        Page newPage = findPageInSwap(instruction);

        // 4. Coloca a nova página na RAM (usando construtor de cópia)
        ram[victimIndex] = new Page(newPage);

        // 5. Reseta o timestamp LRU para a nova página
        lruTimestamps[victimIndex] = instructionTime;
    }

    /**
     * Salva os dados de uma página da RAM de volta para o SWAP (Obs5).
     */
    private void writePageToSwap(Page victimPage) {
        int swapIndex = victimPage.N; // O índice no SWAP é o número (N) da página
        swap[swapIndex] = new Page(victimPage); // Copia os dados (N, I, D, R, M, T)
        swap[swapIndex].M = 0; // Bit M é zerado no SWAP
    }

    /**
     * Encontra uma página no SWAP pelo seu número de instrução (I).
     */
    private Page findPageInSwap(int instruction) {
        // Mapeamento direto: Instrução 1 é N=0, Instrução 100 é N=99
        int swapIndex = instruction - 1;
        return swap[swapIndex];
    }

    /**
     * (Obs4) Zera todos os bits R na RAM.
     */
    private void resetRBits() {
        for (int i = 0; i < RAM_SIZE; i++) {
            ram[i].R = 0;
        }
    }

    /**
     * Imprime o conteúdo de uma matriz (RAM ou SWAP) de forma formatada.
     */
    private void printMatrix(Page[] matrix, String name) {
        System.out.println("--- " + name + " ---");
        System.out.println("-------------------------------------------------------------");
        System.out.println("| Frame | N:    | I:     | D:    | R:   | M:   | T:      |");
        System.out.println("-------------------------------------------------------------");
        for (int i = 0; i < matrix.length; i++) {
            // No caso da RAM (matrix.length == 10)
            if (matrix.length == RAM_SIZE) {
                System.out.printf("| %-5d %s\n", i, matrix[i].toString());
            }
            // (Opcional) Para imprimir o SWAP
            // else {
            //     System.out.printf("| %-5d %s\n", i, matrix[i].toString());
            // }
        }
        System.out.println("-------------------------------------------------------------");
    }

    // --- SEÇÃO DOS ALGORITMOS DE SUBSTITUIÇÃO ---

    /**
     * Chama o algoritmo de seleção de vítima apropriado.
     */
    private int findVictim(String algorithm) {
        switch (algorithm) {
            case "NRU":
                return findVictimNRU();
            case "FIFO":
                return findVictimFIFO();
            case "FIFO-SC":
                return findVictimFIFOSC();
            case "CLOCK":
                return findVictimClock();
            case "LRU":
                return findVictimLRU();
            case "WS-CLOCK":
                return findVictimWSClock();
            default:
                // Caso padrão: usa FIFO se o nome for inválido
                return findVictimFIFO();
        }
    }

    /**
     * Algoritmo NRU (Not Recently Used).
     * Procura a primeira página da classe mais baixa (R, M):
     * Classe 0: (0, 0)
     * Classe 1: (0, 1)
     * Classe 2: (1, 0)
     * Classe 3: (1, 1)
     */
    private int findVictimNRU() {
        int class0 = -1, class1 = -1, class2 = -1;

        // Procura pela primeira ocorrência de cada classe
        for (int i = 0; i < RAM_SIZE; i++) {
            Page p = ram[i];
            if (p.R == 0 && p.M == 0) {
                return i; // Classe 0 (ideal)
            }
            if (p.R == 0 && p.M == 1 && class1 == -1) {
                class1 = i; // Classe 1
            }
            if (p.R == 1 && p.M == 0 && class2 == -1) {
                class2 = i; // Classe 2
            }
        }

        // Retorna a melhor classe encontrada (na ordem 1, 2, 3)
        if (class1 != -1) return class1;
        if (class2 != -1) return class2;

        // Se só restam páginas (1, 1) ou não achou as outras,
        // retorna a primeira (1, 1) ou a primeira página (fallback)
        return 0;
    }

    /**
     * Algoritmo FIFO (First-In, First-Out).
     */
    private int findVictimFIFO() {
        int victimIndex = fifoPointer;
        fifoPointer = (fifoPointer + 1) % RAM_SIZE; // Avança o ponteiro
        return victimIndex;
    }

    /**
     * Algoritmo FIFO-SC (Second Chance).
     */
    private int findVictimFIFOSC() {
        while (true) {
            Page p = ram[fifoPointer];
            if (p.R == 0) {
                // Vítima encontrada (R=0)
                int victimIndex = fifoPointer;
                fifoPointer = (fifoPointer + 1) % RAM_SIZE;
                return victimIndex;
            } else {
                // Segunda chance (R=1)
                p.R = 0; // Zera o bit R
                fifoPointer = (fifoPointer + 1) % RAM_SIZE; // Avança o ponteiro
            }
        }
    }

    /**
     * Algoritmo CLOCK (Relógio). (Funcionalmente idêntico ao FIFO-SC).
     */
    private int findVictimClock() {
        while (true) {
            Page p = ram[clockPointer];
            if (p.R == 0) {
                // Vítima encontrada (R=0)
                int victimIndex = clockPointer;
                clockPointer = (clockPointer + 1) % RAM_SIZE;
                return victimIndex;
            } else {
                // Segunda chance (R=1)
                p.R = 0; // Zera o bit R
                clockPointer = (clockPointer + 1) % RAM_SIZE; // Avança o ponteiro
            }
        }
    }

    /**
     * Algoritmo LRU (Least Recently Used).
     * Usa o array auxiliar lruTimestamps.
     */
    private int findVictimLRU() {
        long minTime = Long.MAX_VALUE;
        int victimIndex = 0;

        for (int i = 0; i < RAM_SIZE; i++) {
            if (lruTimestamps[i] < minTime) {
                minTime = lruTimestamps[i];
                victimIndex = i;
            }
        }
        return victimIndex;
    }

    /**
     * Algoritmo WS-Clock (Working Set Clock) (Obs3).
     */
    private int findVictimWSClock() {
        while (true) {
            Page p = ram[clockPointer];

            if (p.R == 0) {
                // R=0. Verifica o conjunto de trabalho (T)

                // (Obs3) Sorteia EP (Envelhecimento da Página)
                int EP = random.nextInt(9900) + 100; // 100 a 9999

                if (EP > p.T) {
                    // (Obs3) EP > T -> Página não está no conjunto de trabalho.
                    // Esta é a vítima.
                    int victimIndex = clockPointer;
                    clockPointer = (clockPointer + 1) % RAM_SIZE;
                    return victimIndex;
                }
                // else (EP <= p.T): R=0, mas ainda está no WS. Não substituir.

            } else {
                // R=1. Página foi usada recentemente.
                p.R = 0; // Zera o bit R e continua
            }

            // Avança o ponteiro do relógio
            clockPointer = (clockPointer + 1) % RAM_SIZE;
        }
    }
}
