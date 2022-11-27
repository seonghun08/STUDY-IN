package com.studyIn.modules.location.form;

import com.studyIn.modules.location.Location;
import lombok.Data;

@Data
public class LocationForm {

    private String locationName;

    public String getCityName() {
        return this.locationName.substring(0, locationName.indexOf("("));
    }

    public String getLocalNameOfCity() {
        return this.locationName.substring(locationName.indexOf("(") + 1, locationName.indexOf(")"));
    }

    public String getProvinceName() {
        return this.locationName.substring(locationName.indexOf("/") + 1);
    }

    public Location getLocation() {
        return Location.builder()
                .city(this.getCityName())
                .localNameOfCity(this.getLocalNameOfCity())
                .province(this.getProvinceName())
                .build();
    }
}
