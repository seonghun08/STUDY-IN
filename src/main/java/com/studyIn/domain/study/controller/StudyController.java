package com.studyIn.domain.study.controller;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.study.controller.validator.StudyFormValidator;
import com.studyIn.domain.study.dto.form.StudyForm;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.repository.query.StudyQueryDto;
import com.studyIn.domain.study.repository.query.StudyQueryRepository;
import com.studyIn.domain.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final StudyQueryRepository studyQueryRepository;
    private final StudyFormValidator studyFormValidator;

    @InitBinder("studyForm")
    private void StudyFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(studyFormValidator);
    }

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentAccount AccountInfo accountInfo, Model model) {
        model.addAttribute(accountInfo);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudy(@CurrentAccount AccountInfo accountInfo, @Valid StudyForm studyForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(accountInfo);
            return "study/form";
        }

        Study study = studyService.createStudy(accountInfo, studyForm);
        return "redirect:/study/" + study.getPath();
    }

    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, Model model) {
        StudyQueryDto study = studyQueryRepository.findStudyQueryDtoByPath(path);
        model.addAttribute(accountInfo);
        model.addAttribute("study", study);
        return "study/view";
    }

    @GetMapping("study/{path}/members")
    public String viewStudyMembers(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, Model model) {
        StudyQueryDto study = studyQueryRepository.findStudyQueryDtoByPath(path);
        model.addAttribute(accountInfo);
        model.addAttribute("study", study);
        return "study/members";
    }
}
