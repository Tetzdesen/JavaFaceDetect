package br.com.javafacedetect.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pessoa")
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RostoRegistro> rostos = new ArrayList<>();

    protected Pessoa() {
    }

    public Pessoa(String nome) {
        this.nome = nome;
        this.criadoEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public List<RostoRegistro> getRostos() {
        return rostos;
    }

    public void adicionarRosto(RostoRegistro rosto) {
        rosto.setPessoa(this);
        this.rostos.add(rosto);
    }
}
