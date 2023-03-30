package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostFileSpringJpaRepository extends JpaRepository<PostFile, Long> {

//    void removeByMoimPostId(Long moimPostId);
}
