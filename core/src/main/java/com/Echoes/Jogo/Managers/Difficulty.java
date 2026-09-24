package com.Echoes.Jogo.Managers;
public enum Difficulty {
 FACIL("FÁCIL",0.70f,0.70f,0.80f), NORMAL("NORMAL",1f,1f,1f), DIFICIL("DIFÍCIL",1.50f,1.40f,1.25f);
 public final String nome; public final float multiplicadorDanoRecebido,multiplicadorO2,multiplicadorVelocidadeInimigo;
 Difficulty(String n,float d,float o,float v){nome=n;multiplicadorDanoRecebido=d;multiplicadorO2=o;multiplicadorVelocidadeInimigo=v;}
}
