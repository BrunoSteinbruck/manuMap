package com.manumap.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manumap.model.Equipe;
import com.manumap.repository.EquipeRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/equipes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // para o Vite
public class EquipeController {

	@Autowired
    private EquipeRepository repo;

    @GetMapping
    public List<Equipe> listar() {
        return repo.findAll();
    }

    @PatchMapping("/{id}/area")
    public Equipe mover(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Equipe equipe = repo.findById(id).orElseThrow();
        equipe.setAreaAtual(body.get("area"));
        return repo.save(equipe);
    }
}