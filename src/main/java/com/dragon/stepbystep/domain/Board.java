package com.dragon.stepbystep.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "board_posts")
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(name = "author_nickname", nullable = false)
    private String authorNickname;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "comments_count", nullable = false)
    private int commentsCount = 0;

    @Builder.Default
    @Column(name = "likes_count", nullable = false)
    private int likesCount = 0;


    public boolean isAuthor(Long userId){
        return author != null && author.getId().equals(userId);
    }

    public void updateContent(String content){
        this.content = content;
    }

    public void increaseLikesCount(){
        this.likesCount += 1;
    }

    public void decreaseLikesCount(){
        if(this.likesCount > 0){
            this.likesCount -= 1;
        }
    }

    public void increaseCommentsCount() {
        this.commentsCount += 1;
    }

    public void decreaseCommentsCount() {
        if (this.commentsCount > 0) {
            this.commentsCount -= 1;
        }
    }
}