package com.studyIn.domain.study.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.location.Location;
import com.studyIn.domain.location.LocationForm;
import com.studyIn.domain.location.LocationRepository;
import com.studyIn.domain.study.controller.validator.PathFormValidator;
import com.studyIn.domain.study.dto.form.DescriptionForm;
import com.studyIn.domain.study.dto.form.PathForm;
import com.studyIn.domain.study.dto.form.TitleForm;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.repository.StudyRepository;
import com.studyIn.domain.study.dto.StudyQueryDto;
import com.studyIn.domain.study.repository.query.StudyQueryRepository;
import com.studyIn.domain.study.service.StudySettingsService;
import com.studyIn.domain.tag.Tag;
import com.studyIn.domain.tag.TagForm;
import com.studyIn.domain.tag.TagRepository;
import com.studyIn.domain.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    private final StudySettingsService studySettingsService;
    private final TagService tagService;
    private final StudyRepository studyRepository;
    private final TagRepository tagRepository;
    private final LocationRepository locationRepository;

    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final PathFormValidator pathFormValidator;
    private final TitleFormValidator titleFormValidator;

    @InitBinder("pathForm")
    public void pathFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(pathFormValidator);
    }

    @InitBinder("titleForm")
    public void titleFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(titleFormValidator);
    }

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

    @GetMapping("/tags")
    public String updateTagsForm(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, Model model) throws JsonProcessingException {
        StudyQueryDto study = studySettingsService.getStudyPermissionToUpdate(accountInfo, path);
        List<String> tags = study.getTags().stream()
                .map(tag -> tag.getTitle())
                .collect(Collectors.toList());
        List<String> allTags = tagRepository.findAll().stream()
                .map(Tag::getTitle)
                .collect(Collectors.toList());
        model.addAttribute(accountInfo);
        model.addAttribute("study", study);
        model.addAttribute("tags", tags);
        model.addAttribute("savedList", objectMapper.writeValueAsString(allTags));
        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTagsSettings(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, @RequestBody TagForm tagForm) {
        Tag tag = tagService.findExistingTagOrElseCreateTag(tagForm);
        studySettingsService.addTag(accountInfo, tag, path);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTagsSettings(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, @RequestBody TagForm tagForm) {
        Optional<Tag> tag = tagRepository.findByTitle(tagForm.getTitle());

        if (tag.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        studySettingsService.removeTag(accountInfo, tag.get(), path);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/locations")
    public String locationsSettingsForm(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, Model model) throws JsonProcessingException {
        StudyQueryDto study = studySettingsService.getStudyPermissionToUpdate(accountInfo, path);
        List<String> locations = study.getLocations().stream()
                .map(location -> location.toString())
                .collect(Collectors.toList());
        List<String> allLocations = locationRepository.findAll().stream()
                .map(location -> location.toString())
                .collect(Collectors.toList());
        model.addAttribute(accountInfo);
        model.addAttribute("study", study);
        model.addAttribute("locations", locations);
        model.addAttribute("savedList", objectMapper.writeValueAsString(allLocations));
        return "study/settings/locations";
    }

    @PostMapping("/locations/add")
    @ResponseBody
    public ResponseEntity addLocationsSettings(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, @RequestBody LocationForm locationForm) {
        Optional<Location> location = locationRepository.findByCityAndProvince(locationForm.getCityName(), locationForm.getProvinceName());

        if (location.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        studySettingsService.addLocation(accountInfo, location.get(), path);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/locations/remove")
    @ResponseBody
    public ResponseEntity removeLocationsSettings(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, @RequestBody LocationForm locationForm) {
        Optional<Location> location = locationRepository.findByCityAndProvince(locationForm.getCityName(), locationForm.getProvinceName());

        if (location.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        studySettingsService.removeLocation(accountInfo, location.get(), path);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/study")
    public String studySettingsForm(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, Model model) {
        StudyQueryDto study = studySettingsService.getStudyPermissionToUpdate(accountInfo, path);
        model.addAttribute(accountInfo);
        model.addAttribute("study", study);
        model.addAttribute("pathForm", modelMapper.map(study, PathForm.class));
        model.addAttribute("titleForm", modelMapper.map(study, TitleForm.class));
        return "study/settings/study";
    }

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, RedirectAttributes attributes) {
        studySettingsService.publish(accountInfo, path);
        attributes.addFlashAttribute("message", "스터디를 공개했습니다.");
        return "redirect:/study/" + path + "/settings/study";
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, RedirectAttributes attributes) {
        studySettingsService.close(accountInfo, path);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다.");
        return "redirect:/study/" + path + "/settings/study";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, RedirectAttributes attributes) {
        Study study = studyRepository.findByPath(path).orElseThrow(IllegalArgumentException::new);

        if (!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "인원 모집은 30분마다 변경할 수 있습니다.");
            return "redirect:/study/" + path + "/settings/study";
        }

        studySettingsService.startRecruit(accountInfo, path);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");
        return "redirect:/study/" + path + "/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, RedirectAttributes attributes) {
        Study study = studyRepository.findByPath(path).orElseThrow(IllegalArgumentException::new);

        if (!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "인원 모집은 30분마다 변경할 수 있습니다.");
            return "redirect:/study/" + path + "/settings/study";
        }

        studySettingsService.stopRecruit(accountInfo, path);
        attributes.addFlashAttribute("message", "인원 모집을 종료합니다.");
        return "redirect:/study/" + path + "/settings/study";
    }

    @PostMapping("/study/path")
    public String updatePath(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, @Valid PathForm pathForm, Errors errors,
                             Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            StudyQueryDto study = studySettingsService.getStudyPermissionToUpdate(accountInfo, path);
            model.addAttribute("study", study);
            model.addAttribute(accountInfo);
            model.addAttribute(pathForm);
            model.addAttribute(modelMapper.map(study, TitleForm.class));
            model.addAttribute("message", "이미 존재하는 스터디 경로입니다. 다른 경로를 입력해주세요.");
            return "study/settings/study";
        }

        String updatePath = studySettingsService.updatePath(accountInfo, pathForm, path);
        attributes.addFlashAttribute("message", "스터디 경로를 변경하였습니다.");
        return "redirect:/study/" + updatePath + "/settings/study";
    }

    @PostMapping("/study/title")
    public String updateTitle(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, @Valid TitleForm titleForm, Errors errors,
                              Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            StudyQueryDto study = studySettingsService.getStudyPermissionToUpdate(accountInfo, path);
            model.addAttribute("study", study);
            model.addAttribute(accountInfo);
            model.addAttribute(modelMapper.map(study, PathForm.class));
            model.addAttribute(titleForm);
            model.addAttribute("message", "이미 존재하는 스터디 이름입니다. 다른 이름을 입력해주세요.");
            return "study/settings/study";
        }

        studySettingsService.updateTitle(accountInfo, titleForm, path);
        attributes.addFlashAttribute("message", "스터디 이름을 변경하였습니다.");
        return "redirect:/study/" + path + "/settings/study";
    }

    @PostMapping("study/remove")
    public String removeStudy(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, RedirectAttributes attributes) {
        studySettingsService.removeStudy(accountInfo, path);
        attributes.addFlashAttribute("message", "스터디 삭제를 완료하였습니다.");
        return "redirect:/";
    }
}

