package com.studyIn;

import com.studyIn.domain.location.Location;
import com.studyIn.domain.location.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Profile({"local", "dev"})
@Component
@RequiredArgsConstructor
public class InitLocation {

    private final InitLocationService initLocationService;

    @PostConstruct
    public void initLocationData() throws IOException {
//        initLocationService.init();
    }

    @Component
    @RequiredArgsConstructor
    static class InitLocationService {

        private final LocationRepository locationRepository;

        @Transactional
        public void init() throws IOException {
            if (locationRepository.count() == 0) {
                Resource resource = new ClassPathResource("location_kr.csv");
                List<Location> locations = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
                        .map(line -> {
                            String[] split = line.split(",");
                            return Location.createLocation(split[0], split[1], split[2]);})
                        .collect(Collectors.toList());
                locationRepository.saveAll(locations);
            }
        }
    }
}
