package com.studyIn.modules.board;

import com.studyIn.modules.account.Account;
import com.studyIn.modules.account.AccountService;
import com.studyIn.modules.account.CurrentAccount;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardRepository boardRepository;
    private final BoardService boardService;
    private final AccountService accountService;
    private final ModelMapper modelMapper;

    @GetMapping("/community")
    public String viewCommunity(@PageableDefault(size = 16, sort = "seq", direction = DESC) Pageable pageable,
                                @CurrentAccount Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }

        Page<Board> boardPage = boardRepository.findAll(pageable);
        model.addAttribute("boardPage", boardPage);
        return "board/list";
    }

    @GetMapping("/community/new-writing")
    public String newWritingForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new BoardForm());
        return "board/form";
    }

    @PostMapping("/community/new-writing")
    public String newWriting(@CurrentAccount Account account, @Valid BoardForm boardForm,
                             Errors errors, Model model) {

        Account findAccount = accountService.findByUsername(account.getUsername());

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "board/form";
        }

        boardService.newWriting(boardForm, findAccount);
        return "redirect:/community";
    }

    @GetMapping("/community/read/{id}")
    public String boardRead(@CurrentAccount Account account, @PathVariable("id") Board board, Model model) {

        Board findBoard = boardRepository.findById(board.getId()).orElseThrow();
        model.addAttribute(account);
        model.addAttribute("board", findBoard);
        return "board/read";
    }
}
