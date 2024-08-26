package com.studyIn.domain.comment;

import com.studyIn.domain.BaseTimeEntity;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.board.Board;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name="comment")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account writer;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Lob
    @Column(nullable = false)
    private String content;

    private boolean isRemoved= false;


    //== 생성 메서드 ==//
    public static Comment createComment(Account account, Board board, String content) {
        Comment comment = new Comment();
        comment.writer = account;
        comment.board = board;
        comment.content = content;
        comment.isRemoved = false;
        return comment;
    }


    //== 연관관계 편의 메서드 ==//
    public void confirmWriter(Account account) {
        this.writer = account;
        account.addComment(this);
    }

    public void confirmPost(Board board) {
        this.board = board;
        board.addComment(this);
    }


    //== 수정 ==//
    public void updateContent(String content) {
        this.content = content;
    }

    //== 삭제 ==//
    public void remove() {
        this.isRemoved = true;
    }
}
