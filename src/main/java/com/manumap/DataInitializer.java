package com.manumap;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.manumap.model.Area;
import com.manumap.model.Equipe;
import com.manumap.model.TipoManutencao;
import com.manumap.repository.AreaRepository;
import com.manumap.repository.EquipeRepository;

@Configuration
public class DataInitializer {
	
	@Autowired
    private AreaRepository areaRepo;

    @Autowired
    private EquipeRepository equipeRepo;

	@Bean
    CommandLineRunner init() {
        return args -> {
        	if (areaRepo.count() == 0) {
        		areaRepo.saveAll(Arrays.asList(
                        new Area("12"),
                        new Area("14"),
                        new Area("15"),
                        new Area("16"),
                        new Area("18"),
                        new Area("21"),
                        new Area("22"),
                        new Area("23"),
                        new Area("24"),
                        new Area("25A"),
                        new Area("25C")
                    ));
                    System.out.println("ÁREAS INSERIDAS COM SUCESSO!");
        		/*areaRepo.save(new Area("32"));
        		areaRepo.save(new Area("60"));
        		areaRepo.save(new Area("61"));
        		areaRepo.save(new Area("62"));
        		areaRepo.save(new Area("63"));
        		areaRepo.save(new Area("64"));
        		areaRepo.save(new Area("219"));*/
        	}
        	
        	if (equipeRepo.count() == 0) {
        		equipeRepo.saveAll(Arrays.asList(
        		new Equipe(null, "Equipe Alfa",TipoManutencao.MECANICA, "A11", "Alinhamento da B 11-02"),
        		new Equipe(null, "Equipe Bola",TipoManutencao.ELETRICA, null, null),
        		new Equipe(null, "Equipe Caju",TipoManutencao.CALDEIRARIA, null, null)
        	));
        	}
        };
	}
}
