package com.studyIn.modules.account;

import com.querydsl.core.types.Predicate;
import com.studyIn.modules.location.Location;
import com.studyIn.modules.tag.Tag;

import java.util.Set;

public class AccountPredicates {

    public static Predicate findByTagsAndLocations(Set<Tag> tags, Set<Location> locations) {
        QAccount account = QAccount.account;
        return account.locations.any().in(locations).and(account.tags.any().in(tags));
    }
}
