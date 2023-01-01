package com.studyIn.domain.account.controller;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.account.controller.validator.PasswordFormValidator;
import com.studyIn.domain.account.dto.form.PasswordForm;
import com.studyIn.domain.account.dto.form.ProfileForm;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.repository.ProfileRepository;
import com.studyIn.domain.account.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
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
    private final PasswordFormValidator passwordFormValidator;

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    @GetMapping("/profile")
    public String updateProfileForm(@CurrentAccount AccountInfo accountInfo, Model model) {
        Profile profile = profileRepository.findById(accountInfo.getProfileId()).orElseThrow();
        model.addAttribute(accountInfo);
        model.addAttribute(modelMapper.map(profile, ProfileForm.class));
        return "account/settings/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@CurrentAccount AccountInfo accountInfo, @Valid ProfileForm profileForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(accountInfo);
            return "account/settings/profile";
        }

        attributes.addFlashAttribute("message", "프로필 수정을 완료했습니다.");
        settingsService.updateProfile(profileForm, accountInfo);
        return "redirect:/settings/profile";
    }

    @GetMapping("/password")
    public String updatePasswordForm(@CurrentAccount AccountInfo accountInfo, Model model) {
        model.addAttribute(accountInfo);
        model.addAttribute(new PasswordForm());
        return "account/settings/password";
    }

    @PostMapping("/password")
    public String updatePassword(@CurrentAccount AccountInfo accountInfo, @Valid PasswordForm passwordForm, Errors errors,
                               Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(accountInfo);
            return "account/settings/password";
        }

        settingsService.updatePassword(passwordForm, accountInfo);
        attributes.addFlashAttribute("message", "패스워드를 수정했습니다.");
        return "redirect:/settings/password";
    }
}
