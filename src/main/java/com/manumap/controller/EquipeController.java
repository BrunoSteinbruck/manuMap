package com.manumap.controller;

import com.manumap.model.Equipe;
import com.manumap.repository.EquipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipes")
@CrossOrigin(origins = "http://localhost:5173")
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

    @PutMapping("/{id}/atividade")
    public ResponseEntity<Equipe> atualizarAtividade(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Equipe equipe = repo.findById(id).orElseThrow();
        equipe.setAtividadeAtual(body.get("atividadeAtual"));
        return ResponseEntity.ok(repo.save(equipe));
    }
}