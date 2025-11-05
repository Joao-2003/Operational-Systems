import java.util.Random;

/**
 * Representa uma única página, seja na RAM ou no SWAP.
 * Contém os 6 campos especificados: N, I, D, R, M, T.
 */
public class Page {
    int N; // Número da Página (0-99)
    int I; // Instrução (1-100)
    int D; // Dado (1-50)
    int R; // Bit de Acesso (Referência) (0 ou 1)
    int M; // Bit de Modificação (Dirty) (0 ou 1)
    int T; // Tempo de Envelhecimento (100-9999)

    /**
     * Construtor para criar uma nova página (usado na inicialização do SWAP).
     */
    public Page(int n, int i, int d, int r, int m, int t) {
        this.N = n;
        this.I = i;
        this.D = d;
        this.R = r;
        this.M = m;
        this.T = t;
    }

    /**
     * Construtor de cópia.
     * Usado para carregar uma página do SWAP para a RAM sem compartilhar a referência.
     */
    public Page(Page other) {
        this.N = other.N;
        this.I = other.I;
        this.D = other.D;
        this.R = other.R;
        this.M = other.M;
        this.T = other.T;
    }

    /**
     * Retorna uma representação em String da página para impressão.
     */
    @Override
    public String toString() {
        // Formata a string para alinhamento em colunas
        return String.format("| N: %-2d | I: %-3d | D: %-2d | R: %-1d | M: %-1d | T: %-4d |",
                this.N, this.I, this.D, this.R, this.M, this.T);
    }
}
