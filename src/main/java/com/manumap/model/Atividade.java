package com.manumap.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Atividade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String area;
	private String descricao;
	private TipoManutencao tipo;
	private StatusAtividade status;
	private boolean emergenical;
	
	public Atividade() {
	}

	public Atividade(Long id, String area, String descricao, TipoManutencao tipo, StatusAtividade status,
			boolean emergenical) {
		super();
		this.id = id;
		this.area = area;
		this.descricao = descricao;
		this.tipo = tipo;
		this.status = status;
		this.emergenical = emergenical;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public TipoManutencao getTipo() {
		return tipo;
	}

	public void setTipo(TipoManutencao tipo) {
		this.tipo = tipo;
	}

	public StatusAtividade getStatus() {
		return status;
	}

	public void setStatus(StatusAtividade status) {
		this.status = status;
	}

	public boolean isEmergenical() {
		return emergenical;
	}

	public void setEmergenical(boolean emergenical) {
		this.emergenical = emergenical;
	}
	
	
}
