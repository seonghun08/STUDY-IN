package com.studyIn.domain.account.controller;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.account.dto.form.ProfileForm;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.repository.ProfileRepository;
import com.studyIn.domain.account.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final ProfileRepository profileRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/profile")
    public String profileUpdateForm(@CurrentAccount AccountInfo accountInfo, Model model) {
        Profile profile = profileRepository.findById(accountInfo.getProfileId()).orElseThrow();
        model.addAttribute(accountInfo);
        model.addAttribute(modelMapper.map(profile, ProfileForm.class));
        return "account/settings/profile";
    }

    @PostMapping("/profile")
    public String profileUpdate(@CurrentAccount AccountInfo accountInfo, @Valid ProfileForm profileForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(accountInfo);
            return "account/settings/profile";
        }

        attributes.addFlashAttribute("message", "프로필 수정을 완료했습니다.");
        settingsService.updateProfile(profileForm, accountInfo);
        return "redirect:/settings/profile";
    }
}
