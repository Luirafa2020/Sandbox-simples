import java.awt.Color;
import java.util.Random;

public class Mundo {
    private final int largura;
    private final int altura;
    private Elemento[][] gradeTipos;
    private int[][] gradeCoresAreia; // Cor ARGB para AREIA
    private int[][] gradeVidaExtra;  // Vida para FOGO, FUMACA, VAPOR

    private static final Random random = new Random(); // Compartilhado para geração de cor
    private static final int VARIACAO_COR_AREIA = 15;

    public Mundo(int largura, int altura) {
        this.largura = largura;
        this.altura = altura;
        this.gradeTipos = new Elemento[altura][largura];
        this.gradeCoresAreia = new int[altura][largura];
        this.gradeVidaExtra = new int[altura][largura];
        limpar(); // Inicializa com VAZIO
    }

    // --- Getters ---
    public int getLargura() { return largura; }
    public int getAltura() { return altura; }
    public Elemento getTipo(int linha, int coluna) {
        return isValido(linha, coluna) ? gradeTipos[linha][coluna] : Elemento.VAZIO; // Retorna VAZIO se fora dos limites
    }
    public int getCorAreia(int linha, int coluna) {
        return isValido(linha, coluna) ? gradeCoresAreia[linha][coluna] : 0;
    }
    public int getVida(int linha, int coluna) {
        return isValido(linha, coluna) ? gradeVidaExtra[linha][coluna] : 0;
    }

    // --- Setters e Operações ---
    public void setElemento(int linha, int coluna, Elemento tipo, int vidaInicial) {
         if (!isValido(linha, coluna)) return;
         gradeTipos[linha][coluna] = tipo;
         gradeVidaExtra[linha][coluna] = vidaInicial;
         gradeCoresAreia[linha][coluna] = (tipo == Elemento.AREIA) ? gerarCorAmarelaAreia() : 0;
         // Se tipo for FUMACA ou VAPOR, a vida inicial já deve ser passada corretamente
    }

     // Coloca um elemento, calculando a vida inicial padrão para Fogo, Fumaça, Vapor
     public void colocarElementoPadrao(int linha, int coluna, Elemento tipo) {
         if (!isValido(linha, coluna)) return;

         int vida = 0;
          switch (tipo) {
            case FOGO:   vida = Constantes.MAX_VIDA_FOGO; break;
            case FUMACA: vida = Constantes.MAX_VIDA_FUMACA; break;
            case VAPOR:  vida = Constantes.MAX_VIDA_VAPOR; break;
            default: vida = 0; break;
        }
        setElemento(linha, coluna, tipo, vida);
    }

    public void setVida(int linha, int coluna, int vida) {
        if (isValido(linha, coluna)) {
            gradeVidaExtra[linha][coluna] = vida;
        }
    }

    // Move dados de uma célula para outra, limpando a origem
    public void moverDados(int lOrig, int cOrig, int lDest, int cDest) {
        if (!isValido(lOrig, cOrig) || !isValido(lDest, cDest)) return;
        gradeTipos[lDest][cDest] = gradeTipos[lOrig][cOrig];
        gradeCoresAreia[lDest][cDest] = gradeCoresAreia[lOrig][cOrig];
        gradeVidaExtra[lDest][cDest] = gradeVidaExtra[lOrig][cOrig];
        limparCelula(lOrig, cOrig);
    }

    // Troca os dados de duas células
    public void trocarDados(int l1, int c1, int l2, int c2) {
        if (!isValido(l1, c1) || !isValido(l2, c2)) return;
        Elemento tipoTmp = gradeTipos[l1][c1];
        int corTmp = gradeCoresAreia[l1][c1];
        int vidaTmp = gradeVidaExtra[l1][c1];

        gradeTipos[l1][c1] = gradeTipos[l2][c2];
        gradeCoresAreia[l1][c1] = gradeCoresAreia[l2][c2];
        gradeVidaExtra[l1][c1] = gradeVidaExtra[l2][c2];

        gradeTipos[l2][c2] = tipoTmp;
        gradeCoresAreia[l2][c2] = corTmp;
        gradeVidaExtra[l2][c2] = vidaTmp;
    }

    // Limpa uma única célula
    public void limparCelula(int linha, int coluna) {
        if(isValido(linha, coluna)) {
            gradeTipos[linha][coluna] = Elemento.VAZIO;
            gradeCoresAreia[linha][coluna] = 0;
            gradeVidaExtra[linha][coluna] = 0;
        }
    }

    // Limpa a grade inteira
    public final void limpar() {
        for (int i = 0; i < altura; i++) {
            for (int j = 0; j < largura; j++) {
                limparCelula(i,j);
            }
        }
    }

    // Verifica validade das coordenadas
    public boolean isValido(int linha, int coluna) {
        return linha >= 0 && linha < altura && coluna >= 0 && coluna < largura;
    }

    // Gera cor para areia (pode ser privado ou movido para Visor depois)
    private int gerarCorAmarelaAreia() {
        int r = 230 + random.nextInt(VARIACAO_COR_AREIA * 2) - VARIACAO_COR_AREIA;
        int g = 200 + random.nextInt(VARIACAO_COR_AREIA * 2) - VARIACAO_COR_AREIA;
        int b = 100 + random.nextInt(VARIACAO_COR_AREIA * 2) - VARIACAO_COR_AREIA;
        r = Math.max(180, Math.min(255, r));
        g = Math.max(150, Math.min(255, g));
        b = Math.max(50, Math.min(200, b));
        return new Color(r, g, b).getRGB();
    }

     // Checa adjacente (usado pelo Simulador)
     public boolean checarAdjacente(int linha, int coluna, Elemento tipoAlvo) {
         int[][] vizinhos = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
         for(int[] v : vizinhos) {
             int nl = linha + v[0];
             int nc = coluna + v[1];
             if(isValido(nl, nc) && getTipo(nl, nc) == tipoAlvo) {
                 return true;
             }
         }
         return false;
     }
}