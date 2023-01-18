package com.studyIn.domain.board;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardRepository boardRepository;
    private final BoardService boardService;

    @GetMapping("/community")
    public String viewCommunity(@PageableDefault(size = 10, sort = "seq", direction = DESC) Pageable pageable,
                                @CurrentAccount AccountInfo accountInfo, Model model) {
        if (accountInfo != null) {
            model.addAttribute(accountInfo);
        }

        Page<Board> boardPage = boardRepository.findAll(pageable);
        model.addAttribute("boardPage", boardPage);
        return "board/list";
    }

    @GetMapping("/community/new-writing")
    public String newWritingForm(@CurrentAccount AccountInfo accountInfo, Model model) {
        model.addAttribute(accountInfo);
        model.addAttribute(new BoardForm());
        return "board/form";
    }

    @PostMapping("/community/new-writing")
    public String newWriting(@CurrentAccount AccountInfo accountInfo, @Valid BoardForm boardForm,
                             Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(accountInfo);
            return "board/form";
        }

        boardService.newWriting(boardForm, accountInfo);
        return "redirect:/community";
    }

    @GetMapping("/community/read/{id}")
    public String boardRead(@CurrentAccount AccountInfo accountInfo, @PathVariable("id") Board board, Model model) {
        Board readBoard = boardService.readBoard(board);
        model.addAttribute(accountInfo);
        model.addAttribute("board", readBoard);
        return "board/read";
    }
}
