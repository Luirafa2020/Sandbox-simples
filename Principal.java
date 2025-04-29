import javax.swing.*;

public class Principal {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. Cria o Mundo (modelo de dados)
            Mundo mundo = new Mundo(Constantes.LARGURA_GRADE, Constantes.ALTURA_GRADE);

            // 2. Cria o Simulador (lógica)
            Simulador simulador = new Simulador(mundo);

            // 3. Cria o Visor (visualização e interação)
            VisorSimulacao visor = new VisorSimulacao(mundo, Constantes.TAMANHO_CELULA);

            // 4. Configura a Janela Principal (JFrame)
            JFrame frame = new JFrame("Simulador de Elementos");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(visor); // Adiciona o painel de visualização
            frame.pack();     // Ajusta o tamanho da janela
            frame.setLocationRelativeTo(null); // Centraliza
            frame.setVisible(true); // Torna visível
            visor.requestFocusInWindow(); // Pede foco para o painel (para teclado)

            // 5. Cria e inicia o Timer para o loop da simulação
            // Usa javax.swing.Timer para garantir atualizações na EDT
            Timer timer = new Timer(Constantes.DELAY_MS, e -> {
                simulador.atualizarSimulacao(); // Atualiza a lógica
                visor.repaint();             // Pede para redesenhar
            });
            timer.start(); // Inicia o timer
        });
    }
}