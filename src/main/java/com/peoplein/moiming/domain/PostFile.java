package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.FileType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_file")
public class PostFile {

    @Id
    @GeneratedValue
    @Column(name = "post_file_id")
    private Long id;

    private String filePath;

    private String fileName;

    private FileType fileType;

    private int fileSize;

    /*
     연관관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_post_id")
    private MoimPost moimPost;

    private LocalDateTime createdAt;

}
