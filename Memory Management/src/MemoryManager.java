import java.util.*;

// Classe que gerencia a memória e os algoritmos de alocação
class MemoryManager {
    private final int[] memory;
    private final int memorySize;
    private int lastPlacementIndex = 0; // Para o Next Fit
    private final Map<String, Integer> allocatedProcesses; // Mapeia ID do processo para o bloco inicial
    private final Map<String, Integer> processOriginalSize; // Mapeia ID do processo para seu tamanho real

    // Estrutura de dados para o Quick Fit
    private final Map<Integer, List<Integer>> quickFitFreeLists;
    private static final int QUICK_FIT_BLOCK_SIZE = 8; // Tamanho da partição fixa

    public MemoryManager(int size) {
        this.memorySize = size;
        this.memory = new int[size];
        Arrays.fill(memory, 0); // 0 = livre, 1 = ocupado
        this.allocatedProcesses = new HashMap<>();
        this.processOriginalSize = new HashMap<>();

        // Inicialização do Quick Fit
        this.quickFitFreeLists = new HashMap<>();
        initializeQuickFitLists();
    }

    // Inicializa as listas de blocos livres para o Quick Fit
    private void initializeQuickFitLists() {
        List<Integer> freeBlocksOfSize = new ArrayList<>();
        // Divide a memória em partições de tamanho QUICK_FIT_BLOCK_SIZE
        for (int i = 0; i < memorySize; i += QUICK_FIT_BLOCK_SIZE) {
            freeBlocksOfSize.add(i);
        }
        quickFitFreeLists.put(QUICK_FIT_BLOCK_SIZE, freeBlocksOfSize);
    }


    // Método de alocação genérico que chama o algoritmo específico
    public void allocate(Process process, String algorithm) {
        if (allocatedProcesses.containsKey(process.getId())) {
            System.out.println("Processo " + process.getId() + " já está na memória. Impossível alocar novamente.");
            return;
        }

        int startBlock = -1;
        startBlock = switch (algorithm) {
            case "First Fit" -> firstFit(process);
            case "Next Fit" -> nextFit(process);
            case "Best Fit" -> bestFit(process);
            case "Worst Fit" -> worstFit(process);
            case "Quick Fit" -> quickFit(process);
            default -> startBlock;
        };

        if (startBlock != -1) {
            // Marca como ocupado apenas o tamanho real do processo
            for (int i = 0; i < process.getSize(); i++) {
                memory[startBlock + i] = 1;
            }
            allocatedProcesses.put(process.getId(), startBlock);
            processOriginalSize.put(process.getId(), process.getSize()); // Armazena o tamanho real
            System.out.println("Processo " + process.getId() + " alocado no bloco " + startBlock + ".");
        } else {
            System.out.println("Erro: Espaço insuficiente para alocar o Processo " + process.getId() + " (" + algorithm + ").");
        }
    }

    // Libera a memória ocupada por um processo
    public void deallocate(Process process) {
        if (!allocatedProcesses.containsKey(process.getId())) {
            return;
        }

        int startBlock = allocatedProcesses.get(process.getId());
        int originalSize = processOriginalSize.get(process.getId()); // Usa o tamanho real para desalocar

        for (int i = 0; i < originalSize; i++) {
            // Garante que não ultrapasse os limites da memória
            if (startBlock + i < memorySize) {
                memory[startBlock + i] = 0;
            }
        }

        // Lógica para Quick Fit: devolver a partição inteira à lista
        if (startBlock % QUICK_FIT_BLOCK_SIZE == 0) {
            List<Integer> freeList = quickFitFreeLists.get(QUICK_FIT_BLOCK_SIZE);
            if (freeList != null && !freeList.contains(startBlock)) {
                freeList.add(startBlock);
                Collections.sort(freeList); // Mantém a lista ordenada
            }
        }

        allocatedProcesses.remove(process.getId());
        processOriginalSize.remove(process.getId());
        System.out.println("Processo " + process.getId() + " desalocado da memória.");
    }

    // Algoritmo First Fit
    private int firstFit(Process process) {
        int processSize = process.getSize();
        for (int i = 0; i <= memorySize - processSize; i++) {
            boolean fit = true;
            for (int j = 0; j < processSize; j++) {
                if (memory[i + j] == 1) {
                    fit = false;
                    i += j;
                    break;
                }
            }
            if (fit) {
                return i;
            }
        }
        return -1;
    }

    // Algoritmo Next Fit
    private int nextFit(Process process) {
        int processSize = process.getSize();
        int initialIndex = lastPlacementIndex;
        int currentIndex = initialIndex;

        do {
            if (currentIndex + processSize <= memorySize) {
                boolean fit = true;
                for (int j = 0; j < processSize; j++) {
                    if (memory[currentIndex + j] == 1) {
                        fit = false;
                        currentIndex += j;
                        break;
                    }
                }
                if (fit) {
                    lastPlacementIndex = (currentIndex + processSize) % memorySize;
                    return currentIndex;
                }
            }
            currentIndex = (currentIndex + 1) % memorySize;
        } while (currentIndex != initialIndex);

        return -1;
    }

    // Algoritmo Best Fit
    private int bestFit(Process process) {
        int processSize = process.getSize();
        int bestFitIndex = -1;
        int minWaste = Integer.MAX_VALUE;

        for (int i = 0; i <= memorySize - processSize; i++) {
            if (memory[i] == 0) {
                int currentHoleSize = 0;
                while (i + currentHoleSize < memorySize && memory[i + currentHoleSize] == 0) {
                    currentHoleSize++;
                }

                if (currentHoleSize >= processSize) {
                    int waste = currentHoleSize - processSize;
                    if (waste < minWaste) {
                        minWaste = waste;
                        bestFitIndex = i;
                    }
                }
                i += currentHoleSize - 1;
            }
        }
        return bestFitIndex;
    }

    // Algoritmo Worst Fit
    private int worstFit(Process process) {
        int processSize = process.getSize();
        int worstFitIndex = -1;
        int maxWaste = -1;

        for (int i = 0; i <= memorySize - processSize; i++) {
            if (memory[i] == 0) {
                int currentHoleSize = 0;
                while (i + currentHoleSize < memorySize && memory[i + currentHoleSize] == 0) {
                    currentHoleSize++;
                }

                if (currentHoleSize >= processSize) {
                    int waste = currentHoleSize - processSize;
                    if (waste > maxWaste) {
                        maxWaste = waste;
                        worstFitIndex = i;
                    }
                }
                i += currentHoleSize - 1;
            }
        }
        return worstFitIndex;
    }

    // Algoritmo Quick Fit
    private int quickFit(Process process) {
        // 1. Verifica se o processo é maior que o bloco fixo. Se for, não pode ser alocado.
        if (process.getSize() > QUICK_FIT_BLOCK_SIZE) {
            System.out.println("Erro (Quick Fit): Processo " + process.getId() + " (tamanho " + process.getSize() + ") é maior que a partição fixa de " + QUICK_FIT_BLOCK_SIZE + ".");
            return -1;
        }

        // 2. Tenta obter um bloco da lista rápida de partições.
        List<Integer> freeList = quickFitFreeLists.get(QUICK_FIT_BLOCK_SIZE);
        if (freeList != null && !freeList.isEmpty()) {
            // Pega a primeira partição livre disponível.
            return freeList.remove(0);
        }

        // 3. Se não houver blocos rápidos, a alocação falha.
        return -1;
    }

    // Calcula e exibe estatísticas de fragmentação
    public void printFragmentationStats(List<Process> allProcesses) {
        List<Integer> holeSizes = new ArrayList<>();
        int i = 0;
        while (i < memorySize) {
            if (memory[i] == 0) {
                int holeSize = 0;
                while (i < memorySize && memory[i] == 0) {
                    holeSize++;
                    i++;
                }
                holeSizes.add(holeSize);
            } else {
                i++;
            }
        }

        int unusableBlocksCount = 0;
        int totalUnusableMemory = 0;

        int minPendingProcessSize = Integer.MAX_VALUE;
        for (Process p : allProcesses) {
            if (!allocatedProcesses.containsKey(p.getId())) {
                if (p.getSize() < minPendingProcessSize) {
                    minPendingProcessSize = p.getSize();
                }
            }
        }

        if (minPendingProcessSize == Integer.MAX_VALUE) {
            minPendingProcessSize = 0;
        }

        for (int hole : holeSizes) {
            if (hole < minPendingProcessSize) {
                unusableBlocksCount++;
                totalUnusableMemory += hole;
            }
        }

        System.out.println(
                "Estatísticas de Fragmentação: " + holeSizes.size() + " buracos livres. " +
                        unusableBlocksCount + " blocos inutilizáveis (total de " + totalUnusableMemory + " unidades de memória)."
        );
    }

    public boolean isProcessAllocated(Process process) {
        return allocatedProcesses.containsKey(process.getId());
    }

    public void printMemoryMap() {
        System.out.println("Mapa de bits da memória: " + Arrays.toString(memory));
    }

    public void reset() {
        Arrays.fill(memory, 0);
        allocatedProcesses.clear();
        processOriginalSize.clear();
        lastPlacementIndex = 0;
        quickFitFreeLists.clear();
        initializeQuickFitLists();
    }
}