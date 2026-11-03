package br.com.javafacedetect.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "rosto_registro")
public class RostoRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @Lob
    @Column(name = "imagem_recortada", nullable = false)
    private byte[] imagemRecortada;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected RostoRegistro() {
    }

    public RostoRegistro(byte[] imagemRecortada) {
        this.imagemRecortada = imagemRecortada;
        this.criadoEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public byte[] getImagemRecortada() {
        return imagemRecortada;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
