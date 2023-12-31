package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "moim_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MoimReview {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "moim_review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id")
    private Moim moim;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "moimReview", cascade = CascadeType.ALL)
    private List<ReviewAnswer> reviewAnswers = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MoimReview writeReview(Member member, Moim moim) {
        MoimReview moimReview = new MoimReview(member, moim);
        return moimReview;
    }

    private MoimReview(Member member, Moim moim) {

        /*
         연관관계 매핑
         */
        this.member = member;
        this.moim = moim;

        /*
         초기화
         */
        createdAt = LocalDateTime.now();
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}