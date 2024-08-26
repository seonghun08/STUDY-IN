package com.studyIn.modules.tag;

import com.studyIn.modules.tag.form.TagForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public Tag findTag(TagForm tagForm) {
        return tagRepository.findByTitle(tagForm.getTagTitle())
                .orElseGet(() -> tagRepository
                        .save(Tag.builder()
                        .title(tagForm.getTagTitle())
                        .build())
                );
    }

    @Transactional(readOnly = true)
    public List<String> findAllTags() {
        return tagRepository.findAll().stream().map(Tag :: getTitle).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> collectList(Set<Tag> tags) {
        return tags.stream().map(Tag :: getTitle).collect(Collectors.toList());
    }

    public void deleteTag(Tag tag) {
        tagRepository.delete(tag);
    }
}