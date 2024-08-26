package com.studyIn.domain.board;

import com.studyIn.domain.BaseTimeEntity;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.comment.Comment;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
@Table(name = "board")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    private Long seq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account writer;

    @Column(length = 40, nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    private LocalDateTime dateCreated;

    private int views;

    @OneToMany(mappedBy = "board", cascade = ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();


    //== 생성 메서드 ==//
    public static Board createBoard(BoardForm boardForm) {
        Board board = new Board();
        board.title = board.getTitle();
        board.content = boardForm.getContent();
        board.dateCreated = LocalDateTime.now();
        return board;
    }


    //== 연관관계 메서드 ==//
    public void confirmWriter(Account account) {
        this.writer = account;
        account.addBoard(this);
    }

    public void addComment(Comment comment){
        comments.add(comment);
    }


    public void increaseInViews() {
        this.views++;
    }


}
