package com.studyIn.domain.account.entity;

import com.studyIn.domain.tag.Tag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "account_tag")
@Getter @Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountTag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    //== 생성 메서드 ==//
    public static AccountTag createAccountTag(Tag tag) {
        AccountTag accountTag = new AccountTag();
        accountTag.setTag(tag);
        return accountTag;
    }
}
