package com.studyIn.domain.study.controller;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.study.dto.StudyDto;
import com.studyIn.domain.study.dto.form.DescriptionForm;
import com.studyIn.domain.study.service.StudyService;
import com.studyIn.domain.study.repository.StudyRepository;
import com.studyIn.domain.study.service.StudySettingsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    private final StudySettingsService studySettingsService;
    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/description")
    public String descriptionSettingsForm(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, Model model) {
        StudyDto studyDto = studySettingsService.getStudyPermissionToUpdate(accountInfo, path);
        model.addAttribute(accountInfo);
        model.addAttribute("study", studyDto);
        model.addAttribute(modelMapper.map(studyDto, DescriptionForm.class));
        return "study/settings/description";
    }
}
