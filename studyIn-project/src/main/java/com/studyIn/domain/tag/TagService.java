package com.studyIn.domain.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    /**
     * 주제 태그가 "DB"에 이미 저장되어 있다면 저장된 태그를 반환
     * "DB"에 존재하지 않을 경우 새로 만들어서 저장 후, 반환
     */
    public Tag findExistingTagOrElseCreateTag(TagForm tagForm) {
        return tagRepository.findByTitle(tagForm.getTitle())
                .orElseGet(() -> tagRepository.save(Tag.createTag(tagForm.getTitle())));
    }
}
