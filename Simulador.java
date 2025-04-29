// Simulador.java
import java.util.Random;

/**
 * Contém a lógica principal de atualização da simulação.
 * Opera sobre um objeto Mundo para modificar o estado das células.
 */
public class Simulador {

    private final Mundo mundo; // Referência ao modelo de dados
    private final Random random = new Random(); // Gerador de números aleatórios

    public Simulador(Mundo mundo) {
        this.mundo = mundo;
    }

    /**
     * Executa um passo da simulação, atualizando o estado de todas as células.
     */
    public void atualizarSimulacao() {
        for (int linha = mundo.getAltura() - 1; linha >= 0; linha--) {
             boolean varrerEsquerdaDireita = random.nextBoolean();
            for (int col = 0; col < mundo.getLargura(); col++) {
                int coluna = varrerEsquerdaDireita ? col : mundo.getLargura() - 1 - col;
                Elemento tipoAtual = mundo.getTipo(linha, coluna);
                if (tipoAtual != Elemento.VAZIO) {
                    switch (tipoAtual) {
                        case AREIA:    processarAreia(linha, coluna); break;
                        case AGUA:     processarAgua(linha, coluna); break;
                        case PETROLEO: processarPetroleo(linha, coluna); break;
                        case FOGO:     processarFogo(linha, coluna); break; // Modificado
                        case FUMACA:   processarGas(linha, coluna, Elemento.FUMACA); break;
                        case VAPOR:    processarGas(linha, coluna, Elemento.VAPOR); break;
                        default: break;
                    }
                }
            }
        }
    }

    // --- Processamento do Fogo (COM LÓGICA DE VIDA BAIXA) ---
    private void processarFogo(int linha, int coluna) {
        int vidaAtual = mundo.getVida(linha, coluna);
        vidaAtual--; // Sempre decrementa a vida

        // --- Lógica de Extinção e Vida Baixa ---
        if (vidaAtual <= 0) { // Extinção final
            int li = linha - 1;
            // Tenta gerar última fumaça acima
            if (mundo.isValido(li, coluna) && mundo.getTipo(li, coluna) == Elemento.VAZIO) {
                mundo.setElemento(li, coluna, Elemento.FUMACA, Constantes.MAX_VIDA_FUMACA / 3); // Vida curta
            }
            mundo.limparCelula(linha, coluna); // Fogo vira VAZIO
            return; // Termina aqui
        }

        mundo.setVida(linha, coluna, vidaAtual); // Atualiza a vida na grade

        // --- Verifica estado de vida baixa (brasas/faíscas) ---
        boolean vidaBaixa = vidaAtual <= Constantes.VIDA_BAIXA_FOGO_THRESHOLD;

        if (vidaBaixa) {
            // --- Comportamento em Vida Baixa ---

            // 1. Chance de apagar probabilisticamente (maior chance quanto menor a vida)
            int chanceApagar = Constantes.CHANCE_FOGO_APAGAR_VIDA_BAIXA + (Constantes.VIDA_BAIXA_FOGO_THRESHOLD - vidaAtual);
            if (random.nextInt(100) < chanceApagar) {
                 int li = linha - 1;
                 // Tenta gerar fumaça ao apagar
                 if (mundo.isValido(li, coluna) && mundo.getTipo(li, coluna) == Elemento.VAZIO) {
                    mundo.setElemento(li, coluna, Elemento.FUMACA, Constantes.MAX_VIDA_FUMACA / 4); // Mais curta ainda?
                 }
                 mundo.limparCelula(linha, coluna); // Apaga
                 return; // Termina
            }

            // 2. Gera fumaça de "brasa" (chance normal, sem bônus de óleo)
            int li = linha - 1;
            if (random.nextInt(100) < Constantes.CHANCE_GERAR_FUMACA && mundo.isValido(li, coluna) && mundo.getTipo(li, coluna) == Elemento.VAZIO) {
                 mundo.setElemento(li, coluna, Elemento.FUMACA, Constantes.MAX_VIDA_FUMACA); // Vida normal de fumaça
            }

            // 3. Tenta subir (chance normal, mas sem incendiar)
            if (random.nextInt(100) < Constantes.CHANCE_SUBIR_FOGO) {
                 // Chama tentarSubir, mas o retorno 2 (incendiar) não ocorrerá pois não há consumo/propagação
                 int subiu = tentarSubirElemento(linha, coluna, li, true); // Passa true, mas não vai incendiar
                 if (subiu == 1) return; // Termina se apenas subiu/trocou
            }

            // 4. NÃO consome petróleo e NÃO propaga em vida baixa

        } else {
            // --- Comportamento Normal (Vida Alta) ---

            // 1. Verifica se está sobre petróleo
            int la = linha + 1;
            boolean sobrePetroleo = mundo.isValido(la, coluna) && mundo.getTipo(la, coluna) == Elemento.PETROLEO;

            // 2. Consome petróleo abaixo (se houver e vida não for baixa)
             if (sobrePetroleo) {
                 if (random.nextInt(100) < Constantes.CHANCE_CONSUMIR_PETROLEO) {
                     int vidaFumacaOleo = Constantes.MAX_VIDA_FUMACA + Constantes.VIDA_BONUS_FUMACA_OLEO;
                     mundo.setElemento(la, coluna, Elemento.FUMACA, vidaFumacaOleo);
                     // Adiciona bônus de vida ao fogo
                     vidaAtual = Math.min(Constantes.MAX_VIDA_FOGO, mundo.getVida(linha, coluna) + Constantes.BONUS_VIDA_FOGO_CONSUMO);
                     mundo.setVida(linha, coluna, vidaAtual); // Atualiza a vida aumentada
                 }
             }

            // 3. Gera fumaça acima (chance depende se está sobre óleo)
            int li = linha - 1;
            int chanceGerar = sobrePetroleo ? Constantes.CHANCE_GERAR_FUMACA_OLEO : Constantes.CHANCE_GERAR_FUMACA;
            if (random.nextInt(100) < chanceGerar && mundo.isValido(li, coluna) && mundo.getTipo(li, coluna) == Elemento.VAZIO) {
                 int vidaFumaca = Constantes.MAX_VIDA_FUMACA;
                 if (sobrePetroleo) {
                     vidaFumaca += Constantes.VIDA_BONUS_FUMACA_OLEO;
                 }
                 mundo.setElemento(li, coluna, Elemento.FUMACA, vidaFumaca);
            }

            // 4. Tenta subir
            if (random.nextInt(100) < Constantes.CHANCE_SUBIR_FOGO) {
                 int subiu = tentarSubirElemento(linha, coluna, li, true); // true = é fogo
                 if (subiu > 0) return; // Termina se subiu ou incendiou
            }

            // 5. Propagação (apenas se vida não for baixa)
            int vidaAposEventos = mundo.getVida(linha, coluna); // Pega a vida mais recente
            int[][] vizinhos = {{0, 1}, {0, -1}, {1, 0}};
            for (int[] v : vizinhos) {
                int nl = linha + v[0], nc = coluna + v[1];
                if (mundo.isValido(nl, nc)) {
                    Elemento tv = mundo.getTipo(nl, nc);
                    int chance = 0;
                    if (tv == Elemento.PETROLEO) chance = Constantes.CHANCE_PROPAGACAO_FOGO_PETROLEO;
                    else if (tv == Elemento.VAZIO && v[0] == 0) chance = Constantes.CHANCE_PROPAGACAO_FOGO_VAZIO;

                    if (random.nextInt(100) < chance) {
                         int lnf = nl, cnf = nc;
                         if(tv == Elemento.PETROLEO) { int lav = nl - 1; if(mundo.isValido(lav, nc) && mundo.getTipo(lav, nc) == Elemento.VAZIO) { lnf = lav; } }
                        if (mundo.getTipo(lnf, cnf) != Elemento.FOGO) {
                             mundo.colocarElementoPadrao(lnf, cnf, Elemento.FOGO);
                             mundo.setVida(linha, coluna, Math.max(1, vidaAposEventos - 5)); // Custo por propagar
                        }
                    }
                }
            }
        } // Fim do else (comportamento normal)
    } // Fim processarFogo

    // --- Restante dos métodos de processamento (Areia, Agua, Petroleo, Gas, etc.) ---
    // MANTIDOS IGUAIS À VERSÃO ANTERIOR
    private void processarAreia(int linha, int coluna) { int la = linha + 1; if (!mundo.isValido(la, coluna)) return; Elemento ta = mundo.getTipo(la, coluna); if (ta == Elemento.VAZIO || ta == Elemento.FUMACA || ta == Elemento.VAPOR) { mundo.moverDados(linha, coluna, la, coluna); } else if (ta == Elemento.AGUA || ta == Elemento.PETROLEO) { mundo.trocarDados(linha, coluna, la, coluna); } else { int dir = random.nextBoolean() ? 1 : -1; processarMovimentoDiagonalSolido(linha, coluna, la, coluna + dir); if (mundo.getTipo(linha, coluna) == Elemento.AREIA) { processarMovimentoDiagonalSolido(linha, coluna, la, coluna - dir); } } }
    private void processarMovimentoDiagonalSolido(int lo, int co, int ld, int cd) { if (mundo.isValido(ld, cd)) { Elemento tipoDiag = mundo.getTipo(ld, cd); if (tipoDiag == Elemento.VAZIO || tipoDiag == Elemento.FUMACA || tipoDiag == Elemento.VAPOR) { mundo.moverDados(lo, co, ld, cd); } else if (tipoDiag == Elemento.AGUA || tipoDiag == Elemento.PETROLEO) { mundo.trocarDados(lo, co, ld, cd); } } }
    private void processarAgua(int linha, int coluna) { if (mundo.checarAdjacente(linha, coluna, Elemento.FOGO) && random.nextInt(100) < Constantes.CHANCE_GERAR_VAPOR) { mundo.colocarElementoPadrao(linha, coluna, Elemento.VAPOR); return; } int la = linha + 1; boolean caiu = false; if (mundo.isValido(la, coluna)) { Elemento tipoAbaixo = mundo.getTipo(la, coluna); if (tipoAbaixo == Elemento.VAZIO || tipoAbaixo == Elemento.FUMACA || tipoAbaixo == Elemento.VAPOR) { mundo.moverDados(linha, coluna, la, coluna); caiu = true; } else if (tipoAbaixo == Elemento.FOGO) { mundo.limparCelula(la, coluna); mundo.moverDados(linha, coluna, la, coluna); caiu = true; } } else { return; } if(caiu) return; int dir = random.nextInt(3) - 1; Elemento tipoAbaixo = mundo.getTipo(la, coluna); if (dir == 0 || !mundo.isValido(la, coluna) || (tipoAbaixo != Elemento.VAZIO && tipoAbaixo != Elemento.FUMACA && tipoAbaixo != Elemento.VAPOR && tipoAbaixo != Elemento.FOGO)) { int ddir = random.nextBoolean() ? 1 : -1; if (tentarMoverLiquido(linha, coluna, la, coluna + ddir)) return; if (tentarMoverLiquido(linha, coluna, la, coluna - ddir)) return; } int hdir = random.nextBoolean() ? 1 : -1; if(tentarFluirHorizontal(linha, coluna, hdir, Constantes.DISPERSAO_AGUA)) return; if(tentarFluirHorizontal(linha, coluna, -hdir, Constantes.DISPERSAO_AGUA)) return; if (dir != 0) { int ddir = random.nextBoolean() ? 1 : -1; if (tentarMoverLiquido(linha, coluna, la, coluna + ddir)) return; if (tentarMoverLiquido(linha, coluna, la, coluna - ddir)) return; } }
    private boolean tentarMoverLiquido(int lo, int co, int ld, int cd) { if (mundo.isValido(ld, cd)) { Elemento tipoDestino = mundo.getTipo(ld, cd); Elemento tipoOrigem = mundo.getTipo(lo, co); if (tipoDestino == Elemento.VAZIO || tipoDestino == Elemento.FUMACA || tipoDestino == Elemento.VAPOR) { mundo.moverDados(lo, co, ld, cd); return true; } else if (tipoOrigem == Elemento.AGUA && tipoDestino == Elemento.FOGO) { mundo.limparCelula(ld, cd); mundo.moverDados(lo, co, ld, cd); return true; } else if (tipoOrigem == Elemento.PETROLEO && tipoDestino == Elemento.AGUA && ld >= lo) { mundo.trocarDados(lo, co, ld, cd); return true; } } return false; }
    private boolean tentarFluirHorizontal(int l, int c, int dir, int disp) { Elemento tipoOrigem = mundo.getTipo(l, c); for (int i = 1; i <= disp; i++) { int nc = c + dir * i; if (!mundo.isValido(l, nc)) return false; Elemento tipoAlvo = mundo.getTipo(l, nc); if (tipoAlvo == Elemento.VAZIO || tipoAlvo == Elemento.FUMACA || tipoAlvo == Elemento.VAPOR || (tipoOrigem == Elemento.AGUA && tipoAlvo == Elemento.FOGO)) { if (tentarMoverLiquido(l, c, l, nc)) return true; } else if (tipoAlvo == Elemento.AREIA) { return false; } } return false; }
    private void processarPetroleo(int linha, int coluna) { int la = linha + 1; boolean caiu = false; if (mundo.isValido(la, coluna)) { Elemento tipoAbaixo = mundo.getTipo(la, coluna); if (tipoAbaixo == Elemento.VAZIO || tipoAbaixo == Elemento.FUMACA || tipoAbaixo == Elemento.VAPOR) { mundo.moverDados(linha, coluna, la, coluna); caiu = true; } else if (tipoAbaixo == Elemento.AGUA) { mundo.trocarDados(linha, coluna, la, coluna); caiu = true; } } else { return; } if(caiu) return; int dir = random.nextInt(3) - 1; Elemento tipoAbaixo = mundo.getTipo(la, coluna); if (dir == 0 || !mundo.isValido(la, coluna) || (tipoAbaixo != Elemento.VAZIO && tipoAbaixo != Elemento.FUMACA && tipoAbaixo != Elemento.VAPOR && tipoAbaixo != Elemento.AGUA)) { int ddir = random.nextBoolean() ? 1 : -1; if (tentarMoverLiquido(linha, coluna, la, coluna + ddir)) return; if (tentarMoverLiquido(linha, coluna, la, coluna - ddir)) return; } int hdir = random.nextBoolean() ? 1 : -1; if(tentarFluirHorizontal(linha, coluna, hdir, Constantes.DISPERSAO_PETROLEO)) return; if(tentarFluirHorizontal(linha, coluna, -hdir, Constantes.DISPERSAO_PETROLEO)) return; if (dir != 0) { int ddir = random.nextBoolean() ? 1 : -1; if (tentarMoverLiquido(linha, coluna, la, coluna + ddir)) return; if (tentarMoverLiquido(linha, coluna, la, coluna - ddir)) return; } }
    private void processarGas(int linha, int coluna, Elemento tipoGas) { int vidaAtual = mundo.getVida(linha, coluna); vidaAtual--; if (vidaAtual <= 0) { mundo.limparCelula(linha, coluna); return; } mundo.setVida(linha, coluna, vidaAtual); int chanceSubir = (tipoGas == Elemento.VAPOR) ? 90 : 55; int li = linha - 1; if (random.nextInt(100) < chanceSubir) { int subiu = tentarSubirElemento(linha, coluna, li, false); if (subiu > 0) return; } int chanceDispersar = (tipoGas == Elemento.VAPOR) ? 25 : 15; if (random.nextInt(100) < chanceDispersar) { int hDir = random.nextBoolean() ? 1 : -1; int nc = coluna + hDir; if (mundo.isValido(linha, nc)) { Elemento tipoLateral = mundo.getTipo(linha, nc); if (tipoLateral == Elemento.VAZIO) { mundo.moverDados(linha, coluna, linha, nc); return;} if (tipoGas == Elemento.VAPOR && tipoLateral == Elemento.FUMACA) { mundo.trocarDados(linha, coluna, linha, nc); return;} } } }
    private int tentarSubirElemento(int lo, int co, int ld, boolean isFire) { if (mundo.isValido(ld, co)) { Elemento ta = mundo.getTipo(ld, co); if (ta == Elemento.VAZIO || ta == Elemento.FUMACA || ta == Elemento.VAPOR) { boolean podeTrocar = true; if (ta != Elemento.VAZIO) { Elemento to = mundo.getTipo(lo, co); if (!((to == Elemento.VAPOR && ta == Elemento.FUMACA) || (to == Elemento.VAPOR && ta == Elemento.FOGO) || (to == Elemento.FUMACA && ta == Elemento.FOGO))) { podeTrocar = false; } } if(podeTrocar) { mundo.trocarDados(lo, co, ld, co); return 1; } } if (isFire && ta == Elemento.PETROLEO) { mundo.colocarElementoPadrao(ld, co, Elemento.FOGO); mundo.setVida(lo, co, Math.max(1, mundo.getVida(lo, co) - 10)); return 2; } } int ddir = random.nextBoolean() ? 1 : -1; if (tentarSubirDiagonal(lo, co, ld, co + ddir, isFire)) { return (isFire && mundo.isValido(ld,co+ddir) && mundo.getTipo(ld,co+ddir) == Elemento.FOGO) ? 2 : 1; } if (tentarSubirDiagonal(lo, co, ld, co - ddir, isFire)) { return (isFire && mundo.isValido(ld,co-ddir) && mundo.getTipo(ld,co-ddir) == Elemento.FOGO) ? 2 : 1; } return 0; }
    private boolean tentarSubirDiagonal(int lo, int co, int ld, int cd, boolean isFire) { if (mundo.isValido(ld, cd)) { Elemento td = mundo.getTipo(ld, cd); if (td == Elemento.VAZIO || td == Elemento.FUMACA || td == Elemento.VAPOR) { boolean podeTrocar = true; if (td != Elemento.VAZIO) { Elemento to = mundo.getTipo(lo, co); if (!((to == Elemento.VAPOR && td == Elemento.FUMACA) || (to == Elemento.VAPOR && td == Elemento.FOGO) || (to == Elemento.FUMACA && td == Elemento.FOGO))) { podeTrocar = false; } } if(podeTrocar) { mundo.trocarDados(lo, co, ld, cd); return true; } } if (isFire && td == Elemento.PETROLEO) { mundo.colocarElementoPadrao(ld, cd, Elemento.FOGO); mundo.setVida(lo, co, Math.max(1, mundo.getVida(lo, co) - 10)); return true; } } return false; }

} // Fim da classe Simulador