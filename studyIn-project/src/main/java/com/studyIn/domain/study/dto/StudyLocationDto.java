package com.studyIn.domain.study.dto;

import lombok.Data;

@Data
public class StudyLocationDto {

    private Long locationId;
    private String city;
    private String localNameOfCity;
    private String province;

    public StudyLocationDto(Long locationId, String city, String localNameOfCity, String province) {
        this.locationId = locationId;
        this.city = city;
        this.localNameOfCity = localNameOfCity;
        this.province = province;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)/%s", city, localNameOfCity, province);
    }
}
