package com.studyIn.domain.account.entity;

import com.studyIn.domain.location.Location;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "account_location")
@Getter @Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountLocation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_location_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    //== 생성 메서드 ==//
    public static AccountLocation createAccountLocation(Location location) {
        AccountLocation accountLocation = new AccountLocation();
        accountLocation.setLocation(location);
        return accountLocation;
    }
}
