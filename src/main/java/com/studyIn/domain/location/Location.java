package com.studyIn.domain.location;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "location", uniqueConstraints = @UniqueConstraint(columnNames = {"city", "province"}))
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String localNameOfCity;

    @Column
    private String province;

    //== 생성 메서드 ==//
    public static Location createLocation(String city, String localNameOfCity, String province) {
        Location location = new Location();
        location.city = city;
        location.localNameOfCity = localNameOfCity;
        location.province = province;
        return location;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)/%s", city, localNameOfCity, province);
    }
}