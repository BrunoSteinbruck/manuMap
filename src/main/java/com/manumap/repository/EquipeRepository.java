package com.manumap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manumap.model.Equipe;
import com.manumap.model.TipoManutencao;

public interface EquipeRepository extends JpaRepository<Equipe, Long>{
	List<Equipe> findByTipoOrderByNomeAsc(TipoManutencao tipo);
}
