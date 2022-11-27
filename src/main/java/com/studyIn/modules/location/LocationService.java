package com.studyIn.modules.location;

import com.studyIn.modules.location.form.LocationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    @PostConstruct
    public void initLocationData() throws IOException {
        if (locationRepository.count() == 0) {
            Resource resource = new ClassPathResource("location_kr.csv");
            List<Location> locationList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
                    .map(line -> {
                        String[] split = line.split(",");
                        return Location.builder().city(split[0]).localNameOfCity(split[1]).province(split[2]).build();
                    }).collect(Collectors.toList());

            locationRepository.saveAll(locationList);
        }
    }

    public Location findLocation(LocationForm locationForm) {
        return locationRepository.findByCityAndProvince(locationForm.getCityName(), locationForm.getProvinceName());
    }

    @Transactional(readOnly = true)
    public List<String> findAllLocations() {
        return locationRepository.findAll().stream().map(Location :: toString).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> collectList(Set<Location> locations) {
        return locations.stream().map(Location :: toString).collect(Collectors.toList());
    }
}
