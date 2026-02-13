package com.manumap.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.manumap.model.Area;
import com.manumap.model.Equipe;
import com.manumap.model.ProgramacaoSemanalItem;
import com.manumap.repository.AreaRepository;
import com.manumap.repository.EquipeRepository;
import com.manumap.service.ProgramacaoSemanalService;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class ProgramacaoSemanalController {

    private final ProgramacaoSemanalService service;
    private final AreaRepository areaRepo;
    private final EquipeRepository equipeRepo;

    public ProgramacaoSemanalController(ProgramacaoSemanalService service, AreaRepository areaRepo, EquipeRepository equipeRepo) {
        this.service = service;
        this.areaRepo = areaRepo;
        this.equipeRepo = equipeRepo;
    }

    @GetMapping
    public List<ProgramacaoSemanalItem> listar() {
        return service.listar();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        try {
            List<String> areas = areaRepo.findAll().stream().map(Area::getCodigo).toList();
            List<ProgramacaoSemanalItem> itens = service.processarUpload(file, areas);
            long comArea = itens.stream().filter(i -> i.getAreaSugerida() != null).count();

            Map<String, Object> body = new HashMap<>();
            body.put("total", itens.size());
            body.put("comArea", comArea);
            body.put("itens", itens);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falha ao ler arquivo Excel.", e);
        }
    }

    @PostMapping("/{id}/auto-assign")
    public ResponseEntity<Equipe> alocarAutomaticamente(@PathVariable long id) {
        ProgramacaoSemanalItem item = service.buscar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linha não encontrada na programação."));

        if (item.getAreaSugerida() == null || item.getTipoSugerido() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível inferir área/tipo dessa linha. Faça alocação manual.");
        }

        List<Equipe> candidatas = equipeRepo.findByTipoOrderByNomeAsc(item.getTipoSugerido());
        if (candidatas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nenhuma equipe cadastrada para o tipo " + item.getTipoSugerido() + ".");
        }

        Equipe equipe = candidatas.stream().filter(e -> e.getAreaAtual() == null || e.getAreaAtual().isBlank()).findFirst()
                .orElse(candidatas.get(0));

        equipe.setAreaAtual(item.getAreaSugerida());
        if (item.getAtividade() != null && !item.getAtividade().isBlank()) {
            equipe.setAtividadeAtual(item.getAtividade());
        }
        return ResponseEntity.ok(equipeRepo.save(equipe));
    }

    @PostMapping("/{id}/assign/{equipeId}")
    public ResponseEntity<Equipe> alocarEquipe(@PathVariable long id, @PathVariable long equipeId) {
        ProgramacaoSemanalItem item = service.buscar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linha não encontrada na programação."));
        Equipe equipe = equipeRepo.findById(equipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipe não encontrada."));

        equipe.setAreaAtual(item.getAreaSugerida());
        if (item.getAtividade() != null && !item.getAtividade().isBlank()) {
            equipe.setAtividadeAtual(item.getAtividade());
        }
        return ResponseEntity.ok(equipeRepo.save(equipe));
    }
}
