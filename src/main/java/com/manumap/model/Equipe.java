package com.manumap.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Equipe {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String nome;
	private TipoManutencao tipo;
	private String areaAtual;
	private String atividadeAtual;
	
	public Equipe(){
	}

	public Equipe(Long id, String nome, TipoManutencao tipo, String areaAtual, String atividadeAtual) {
		super();
		this.id = id;
		this.nome = nome;
		this.tipo = tipo;
		this.areaAtual = areaAtual;
		this.atividadeAtual = atividadeAtual;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public TipoManutencao getTipo() {
		return tipo;
	}

	public void setTipo(TipoManutencao tipo) {
		this.tipo = tipo;
	}

	public String getAreaAtual() {
		return areaAtual;
	}

	public void setAreaAtual(String areaAtual) {
		this.areaAtual = areaAtual;
	}

	public String getAtividadeAtual() {
		return atividadeAtual;
	}

	public void setAtividadeAtual(String atividadeAtual) {
		this.atividadeAtual = atividadeAtual;
	}
	
	
}
