package com.studyIn.domain.location;

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
}
