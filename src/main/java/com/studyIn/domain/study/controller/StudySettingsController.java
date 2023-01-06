package com.studyIn.domain.study.controller;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.study.dto.form.DescriptionForm;
import com.studyIn.domain.study.repository.StudyRepository;
import com.studyIn.domain.study.repository.query.StudyQueryDto;
import com.studyIn.domain.study.repository.query.StudyQueryRepository;
import com.studyIn.domain.study.service.StudySettingsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    private final StudySettingsService studySettingsService;
    private final StudyRepository studyRepository;
    private final StudyQueryRepository studyQueryRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/description")
    public String updateDescriptionForm(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, Model model) {
        StudyQueryDto study = studySettingsService.getStudyPermissionToUpdate(accountInfo, path);
        model.addAttribute(accountInfo);
        model.addAttribute("study", study);
        model.addAttribute(modelMapper.map(study, DescriptionForm.class));
        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateDescription(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, @Valid DescriptionForm descriptionForm,
                                    Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            StudyQueryDto study = studySettingsService.getStudyPermissionToUpdate(accountInfo, path);
            model.addAttribute(accountInfo);
            model.addAttribute("study", study);
            return "study/settings/description";
        }

        studySettingsService.updateDescription(accountInfo, descriptionForm, path);
        attributes.addFlashAttribute("message", "스터디 소개를 수정하였습니다.");
        return "redirect:/study/" + path + "/settings/description";
    }

    @GetMapping("/banner")
    public String updateBannerForm(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, Model model) {
        StudyQueryDto study = studySettingsService.getStudyPermissionToUpdate(accountInfo, path);
        model.addAttribute(accountInfo);
        model.addAttribute("study", study);
        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String updateBanner(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, String image,
                               RedirectAttributes attributes) {
        studySettingsService.updateBanner(accountInfo, image, path);
        attributes.addFlashAttribute("message", "배너 이미지를 수정하였습니다.");
        return "redirect:/study/" + path + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableBanner(@CurrentAccount AccountInfo accountInfo, @PathVariable String path) {
        studySettingsService.enableBanner(accountInfo, path);
        return "redirect:/study/" + path + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableBanner(@CurrentAccount AccountInfo accountInfo, @PathVariable String path) {
        studySettingsService.disableBanner(accountInfo, path);
        return "redirect:/study/" + path + "/settings/banner";
    }
}

