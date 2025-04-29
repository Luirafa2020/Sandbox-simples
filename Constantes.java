// Constantes.java
public class Constantes {
    // Simulação Geral
    public static final int LARGURA_GRADE = 160;
    public static final int ALTURA_GRADE = 120;
    public static final int TAMANHO_CELULA = 5;
    public static final int DELAY_MS = 35; // Delay do Timer

    // Comportamento Elementos
    public static final int MAX_VIDA_FOGO = 80;
    public static final int MAX_VIDA_FUMACA = 100;
    public static final int MAX_VIDA_VAPOR = 60;

    // --- Comportamento Fogo Específico ---
    public static final int VIDA_BAIXA_FOGO_THRESHOLD = (int) (MAX_VIDA_FOGO * 0.15); // Limiar para "brasas" (ex: 12)
    public static final int CHANCE_FOGO_APAGAR_VIDA_BAIXA = 5; // Chance % base de apagar a cada passo em vida baixa (aumenta com vida menor)
    public static final int CHANCE_PROPAGACAO_FOGO_VAZIO = 5;
    public static final int CHANCE_PROPAGACAO_FOGO_PETROLEO = 85;
    public static final int CHANCE_SUBIR_FOGO = 70;
    public static final int CHANCE_CONSUMIR_PETROLEO = 20;
    public static final int BONUS_VIDA_FOGO_CONSUMO = 40; // Vida ADICIONADA ao fogo ao consumir

    // --- Comportamento Fumaça ---
    public static final int CHANCE_GERAR_FUMACA = 25; // Chance normal (fogo normal, brasas)
    public static final int CHANCE_GERAR_FUMACA_OLEO = 65; // Chance maior sobre óleo
    public static final int VIDA_BONUS_FUMACA_OLEO = 50; // Vida ADICIONADA à MAX_VIDA_FUMACA

    // --- Outros ---
    public static final int CHANCE_GERAR_VAPOR = 25;
    public static final int DISPERSAO_AGUA = 5;
    public static final int DISPERSAO_PETROLEO = 2;

    // Renderização Gases
    public static final int CHANCE_DESENHAR_FUMACA = 75;
    public static final int CHANCE_DESENHAR_VAPOR = 60;
}