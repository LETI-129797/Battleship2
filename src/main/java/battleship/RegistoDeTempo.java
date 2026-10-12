package battleship;

import java.time.Duration;

public class RegistoDeTempo {
    private final Duration duracao;
    private final int contadorJogadas;

    public RegistoDeTempo(int contadorJogadas, Duration duracao){
        this.duracao = duracao;
        this.contadorJogadas = contadorJogadas;
    }

    public int getContadorJogadas(){
        return contadorJogadas;
    }

    public Duration getDuracao(){
        return duracao;
    }

    @Override
    public String toString(){
        return "Jogada " + contadorJogadas +": " + duracao.toSeconds() + "s (" + duracao.toMillis() + "ms)";
    }
}
