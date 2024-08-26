package com.studyIn.domain.board;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final AccountRepository accountRepository;
    private final BoardRepository boardRepository;

    public Board newWriting(BoardForm boardForm, AccountInfo accountInfo) {
        Account account = accountRepository.findById(accountInfo.getAccountId()).orElseThrow(IllegalArgumentException::new);
        Board board = Board.createBoard(boardForm);
        board.confirmWriter(account);
        return boardRepository.save(board);
    }

    public Board readBoard(Board board) {
        Board readBoard = boardRepository.findById(board.getId()).orElseThrow();
        readBoard.increaseInViews();
        return readBoard;
    }
}
