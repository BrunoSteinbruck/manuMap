package com.manumap.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manumap.model.Area;
import com.manumap.repository.AreaRepository;

@RestController
@RequestMapping("/api/areas")
@CrossOrigin(origins = "*")
public class AreaController {

	@Autowired
    private AreaRepository repo;

    @GetMapping
    public List<Area> listar() {
        return repo.findAll();
    }
}