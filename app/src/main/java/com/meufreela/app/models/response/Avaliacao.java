package com.meufreela.app.models.response;

public class Avaliacao {
    private String id;
    private String autorId;
    private String autorNome;
    private String avaliadoId;
    private String avaliadoNome;
    private Integer nota;
    private String comentario;
    private String criadoEm;

    public String getId() { return id; }
    public String getAutorNome() { return autorNome; }
    public String getAvaliadoNome() { return avaliadoNome; }
    public Integer getNota() { return nota; }
    public String getComentario() { return comentario; }
    public String getCriadoEm() { return criadoEm; }
}