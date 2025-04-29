// VisorSimulacao.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * Painel responsável por desenhar o estado do Mundo e lidar com a
 * interação do usuário (mouse e teclado).
 */
public class VisorSimulacao extends JPanel {

    private final Mundo mundo;
    private final int tamanhoCelula;
    private Elemento elementoAtual = Elemento.AREIA; // Elemento a ser adicionado
    private boolean adicionarContinuo = false;
    private final Random random = new Random(); // Para renderização aleatória

    // Cores para renderização
    private static final Color COR_AGUA = new Color(60, 120, 220, 200);
    private static final Color COR_PETROLEO = new Color(30, 20, 10);
    private static final Color[] CORES_FUMACA = {
            new Color(50, 50, 50), new Color(65, 65, 65),
            new Color(40, 40, 40), new Color(55, 55, 55)
    };
    private static final Color[] CORES_VAPOR = {
            new Color(180, 180, 180), new Color(200, 200, 200),
            new Color(170, 170, 170), new Color(190, 190, 190)
    };

    public VisorSimulacao(Mundo mundo, int tamanhoCelula) {
        this.mundo = mundo;
        this.tamanhoCelula = tamanhoCelula;
        setPreferredSize(new Dimension(mundo.getLargura() * tamanhoCelula, mundo.getAltura() * tamanhoCelula));
        setBackground(Color.DARK_GRAY);
        setupMouseListener();
        setupKeyListener();
        setFocusable(true);
    }

     private void setupMouseListener() {
        // MANTIDO IGUAL À VERSÃO ANTERIOR
        MouseAdapter mouseHandler = new MouseAdapter() {
             private void adicionarElementosNoPonto(Point p) { if (p == null) return; int coluna = p.x / tamanhoCelula; int linha = p.y / tamanhoCelula; int raio = (elementoAtual == Elemento.FOGO || elementoAtual == Elemento.VAPOR || elementoAtual == Elemento.FUMACA) ? 2 : 4; for (int i = -raio; i <= raio; i++) { for (int j = -raio; j <= raio; j++) { if (Math.sqrt(i*i + j*j) <= raio && random.nextInt(3) != 0) { int nl = linha + i; int nc = coluna + j; if (mundo.isValido(nl, nc)) { if (mundo.getTipo(nl, nc) == Elemento.VAZIO) { mundo.colocarElementoPadrao(nl, nc, elementoAtual); } else if (elementoAtual == Elemento.FOGO && mundo.getTipo(nl, nc) == Elemento.PETROLEO) { int la = nl - 1; if(mundo.isValido(la, nc) && mundo.getTipo(la, nc) == Elemento.VAZIO) { mundo.colocarElementoPadrao(la, nc, Elemento.FOGO); } else { mundo.colocarElementoPadrao(nl, nc, Elemento.FOGO); } } } } } } }
            @Override public void mousePressed(MouseEvent e) { adicionarContinuo = true; adicionarElementosNoPonto(e.getPoint()); VisorSimulacao.this.requestFocusInWindow(); }
            @Override public void mouseReleased(MouseEvent e) { adicionarContinuo = false; }
             Timer addTimer = new Timer(20, _ -> { if (adicionarContinuo) { Point p = VisorSimulacao.this.getMousePosition(); adicionarElementosNoPonto(p); } });
             { addTimer.start(); }
        };
        addMouseListener(mouseHandler);
    }

     private void setupKeyListener() {
        // MANTIDO IGUAL À VERSÃO ANTERIOR
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char key = Character.toUpperCase(e.getKeyChar());
                switch (key) {
                    case 'S': elementoAtual = Elemento.AREIA; break; case 'W': elementoAtual = Elemento.AGUA; break; case 'O': elementoAtual = Elemento.PETROLEO; break; case 'F': elementoAtual = Elemento.FOGO; break; case 'M': elementoAtual = Elemento.FUMACA; break; case 'V': elementoAtual = Elemento.VAPOR; break; case 'C': mundo.limpar(); repaint(); break; // Limpa e redesenha
                }
                // Imprime o selecionado no console (opcional)
                System.out.println("Selecionado: " + elementoAtual.name());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        for (int linha = 0; linha < mundo.getAltura(); linha++) {
            for (int coluna = 0; coluna < mundo.getLargura(); coluna++) {
                Elemento tipo = mundo.getTipo(linha, coluna);
                Color cor = null;
                boolean desenhar = true;

                if (tipo != Elemento.VAZIO) {
                     int vida = mundo.getVida(linha, coluna);

                    switch (tipo) {
                        case AREIA: cor = new Color(mundo.getCorAreia(linha, coluna)); break;
                        case AGUA: cor = COR_AGUA; break;
                        case PETROLEO: cor = COR_PETROLEO; break;
                        case FOGO: cor = getCorFogo(vida, linha, coluna); break; // Modificado para usar nova getCorFogo
                        case FUMACA:
                            if (random.nextInt(100) < Constantes.CHANCE_DESENHAR_FUMACA) {
                                Color corBase = CORES_FUMACA[random.nextInt(CORES_FUMACA.length)];
                                float ratio = (float) Math.max(0, vida) / Constantes.MAX_VIDA_FUMACA;
                                int alpha = (int) (150 * ratio);
                                alpha = Math.max(10, Math.min(150, alpha + random.nextInt(20) - 10));
                                cor = new Color(corBase.getRed(), corBase.getGreen(), corBase.getBlue(), alpha);
                            } else { desenhar = false; }
                            break;
                        case VAPOR:
                             if (random.nextInt(100) < Constantes.CHANCE_DESENHAR_VAPOR) {
                                Color corBase = CORES_VAPOR[random.nextInt(CORES_VAPOR.length)];
                                float ratio = (float) Math.max(0, vida) / Constantes.MAX_VIDA_VAPOR;
                                int alpha = (int) (100 * ratio);
                                alpha = Math.max(5, Math.min(100, alpha + random.nextInt(15) - 7));
                                cor = new Color(corBase.getRed(), corBase.getGreen(), corBase.getBlue(), alpha);
                             } else { desenhar = false; }
                            break;
                        default: cor = Color.MAGENTA; break;
                    }

                    if (desenhar && cor != null) {
                        g2d.setColor(cor);
                        g2d.fillRect(coluna * tamanhoCelula, linha * tamanhoCelula, tamanhoCelula, tamanhoCelula);
                    }
                }
            }
        }
        desenharHUD(g2d);
    }

    private void desenharHUD(Graphics2D g2d) {
        // MANTIDO IGUAL À VERSÃO ANTERIOR
        g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String texto = "Selecionado: " + elementoAtual.name() + " (" + getTeclaElemento(elementoAtual) + ")";
        texto += " | Limpar (C)";
        g2d.drawString(texto, 10, getHeight() - 10);
    }

    private char getTeclaElemento(Elemento el) {
        // MANTIDO IGUAL À VERSÃO ANTERIOR
        switch (el) { case AREIA: return 'S'; case AGUA: return 'W'; case PETROLEO: return 'O'; case FOGO: return 'F'; case FUMACA: return 'M'; case VAPOR: return 'V'; default: return '?'; }
    }

    // --- Geração da Cor do Fogo (COM LÓGICA DE VIDA BAIXA) ---
    private Color getCorFogo(int vida, int linha, int coluna) {
        boolean sobrePetroleo = mundo.isValido(linha + 1, coluna) && mundo.getTipo(linha + 1, coluna) == Elemento.PETROLEO;
        boolean vidaBaixa = vida <= Constantes.VIDA_BAIXA_FOGO_THRESHOLD;

        if (sobrePetroleo && !vidaBaixa) { // Fogo intenso sobre petróleo (apenas se vida não for baixa)
            int g = 200 + random.nextInt(55);
            int b = 100 + random.nextInt(100);
            return new Color(255, Math.min(255, g), Math.min(255, b));
        } else if (vidaBaixa) { // Cor de "brasa" para vida baixa
             float ratio = (float) Math.max(0, vida) / Constantes.VIDA_BAIXA_FOGO_THRESHOLD; // Ratio dentro da faixa de vida baixa
             int r = 150 + (int)(105 * ratio); // De 150 a 255 (vermelho escuro a vermelho normal)
             int g = (int)(80 * ratio);       // De 0 a 80 (pouco verde, mais laranja/vermelho)
             int b = random.nextInt(20);      // Quase sem azul
             r = Math.max(100, Math.min(255, r + random.nextInt(20)-10)); // Garante min red e variação
             g = Math.max(0, Math.min(100, g));
             return new Color(r, g, b);
        } else { // Cor normal (amarelo -> vermelho)
            float ratio = (float) Math.max(0, vida - Constantes.VIDA_BAIXA_FOGO_THRESHOLD) / (Constantes.MAX_VIDA_FOGO - Constantes.VIDA_BAIXA_FOGO_THRESHOLD); // Ratio acima do threshold
            int r = 255;
            int g = (int) (255 * ratio);
            int b = (int) (50 * (1 - ratio));
            g = Math.max(0, Math.min(255, g + random.nextInt(30)-15));
            b = Math.max(0, Math.min(100, b));
            return new Color(r, g, b);
        }
    }
} // Fim da classe VisorSimulacao