package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import com.peoplein.moiming.model.dto.domain.MoimPostDto;
import com.peoplein.moiming.model.dto.domain.PostCommentDto;
import com.peoplein.moiming.model.dto.request_b.MoimPostRequestDto;
import com.peoplein.moiming.model.query.QueryMoimPostDetails;
import com.peoplein.moiming.model.query.QueryPostCommentDetails;
import com.peoplein.moiming.repository.*;
import com.peoplein.moiming.repository.jpa.query.MoimPostJpaQueryRepository;
import com.peoplein.moiming.repository.jpa.query.PostCommentJpaQueryRepository;
import com.peoplein.moiming.service.core.MoimPostServiceCore;
import com.peoplein.moiming.service.input.MoimPostServiceInput;
import com.peoplein.moiming.service.output.MoimPostServiceOutput;
import com.peoplein.moiming.service.shell.MoimPostServiceShell;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MoimPostService {

    private final MoimPostRepository moimPostRepository;
    private final MoimRepository moimRepository;
    private final MemberMoimLinkerRepository memberMoimLinkerRepository;
    private final MoimPostJpaQueryRepository moimPostJpaQueryRepository;
    private final PostCommentJpaQueryRepository postCommentJpaQueryRepository;

    private final PostCommentRepository postCommentRepository;

    private final MoimPostServiceShell moimPostServiceShell;
    private final MoimPostServiceCore moimPostServiceCore;
    private final PostFileRepository postFileRepository;

    /*
     게시물 생성 요청을 처리한다
     게시물 생성 및 이미지를 저장하여 파일 객체도 생성 진행
     */
    public MoimPostDto createPost(MoimPostRequestDto moimPostRequestDto, Member curMember) {
        /*
         TODO : 전송받는 이미지들은 List<MultipartFile> 에 담겨져 올 것이다.
                해당 파일들을 S3 에 저장하는 작업을 진행 후 URL 을 받아오고, 그 과정 이후
                MultipartFile 을 다시 사용 + 받아온 url 을 통해서 PostFile Entity 를 만든다
                그리고 게시물과의 연관관계를 매핑해준다
                >> Cascade 걸어서 자동 저장 되게 지원
         */
        MoimPostServiceInput moimPostServiceInput = moimPostServiceShell.readyForCreatingNewMoimPost(moimPostRequestDto, curMember);
        MoimPostServiceOutput moimPostServiceOutput = moimPostServiceCore.createMoimPost(moimPostServiceInput);
        return moimPostServiceShell.doAfterCreatingMoimPost(moimPostServiceOutput);

    }

    /*
     전달받은 모임의 모든 게시물이 조회될 수 있도록
     데이터를 전달한다 (아마 Paging 예상)
     */
    public List<MoimPostDto> viewAllMoimPost(Long moimId, Member curMember) {

        List<QueryMoimPostDetails> queryDetails = moimPostJpaQueryRepository.findMoimPostDetailsAndFetchCollections(moimId);
        List<MoimPostDto> moimPostDtos = new ArrayList<>();

        for (QueryMoimPostDetails queryMoimPost : queryDetails) {

            boolean isPostCreatorCurrentMember = queryMoimPost.getPostCreatorInfoDto().getMemberId().equals(curMember.getId());

            MoimMemberInfoDto postCreatorInfoDto = null;

            if (!isPostCreatorCurrentMember) {
                postCreatorInfoDto = queryMoimPost.getPostCreatorInfoDto();
            }

            MoimPostDto moimPostDto = new MoimPostDto(
                    queryMoimPost.getMoimPostId()
                    , queryMoimPost.getPostTitle(), queryMoimPost.getPostContent(), queryMoimPost.getMoimPostCategory(), queryMoimPost.isNotice()
                    , queryMoimPost.getCreatedAt(), queryMoimPost.getUpdatedAt(), queryMoimPost.getUpdatedMemberId(), queryMoimPost.isHasFiles()
                    , isPostCreatorCurrentMember, postCreatorInfoDto
            );

            moimPostDtos.add(moimPostDto);
        }

        return moimPostDtos;
    }

    /*
     전달받은 게시물의 모든 정보를 전달한다
     (일반 조회 정보 + Files 및 댓글들 전달)
     */
    public MoimPostDto getMoimPostData(Long moimPostId, Member curMember) {

        List<QueryPostCommentDetails> queryPostCommentDetails = postCommentJpaQueryRepository.findCommentDetailsAndFetchCollections(moimPostId);
        List<PostCommentDto> postCommentsDto = new ArrayList<>();

        for (QueryPostCommentDetails queryDetail : queryPostCommentDetails) {

            boolean isCommentCreatorCurMember = queryDetail.getCommentCreatorInfoDto().getMemberId().equals(curMember.getId());

            MoimMemberInfoDto commentCreatorInfo = null;
            if (!isCommentCreatorCurMember) {
                commentCreatorInfo = queryDetail.getCommentCreatorInfoDto();
            }

            PostCommentDto postCommentDto = new PostCommentDto(
                    queryDetail.getCommentId(), queryDetail.getCommentContent(), queryDetail.getCreatedAt(), queryDetail.getUpdatedAt()
                    , isCommentCreatorCurMember, commentCreatorInfo
            );

            postCommentsDto.add(postCommentDto);
        }

        // MEMO:: DTO Projection 을 사용해도 어짜피 MemberInfoLinker 를 결합시키기 위해 쿼리 한개가 늘어나고
        //          N+1 문제를 발생시키지 않기 때문에, 현재 쿼리를 유지한다

        MoimPost moimPost = moimPostRepository.findWithMoimAndMemberInfoById(moimPostId); // MemberInfo 사용을 위해 변경

        MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findWithMemberInfoAndMoimByMemberAndMoimId(curMember.getId()
                , moimPost.getMoim().getId());

        boolean isPostCreatorCurMember = moimPost.getMember().getId().equals(curMember.getId());

        MoimMemberInfoDto moimMemberInfoDto = null;

        if (!isPostCreatorCurMember) {
            moimMemberInfoDto = new MoimMemberInfoDto(
                    moimPost.getMember().getId()
                    , moimPost.getMember().getMemberInfo().getMemberName()
                    , moimPost.getMember().getMemberEmail()
                    , moimPost.getMember().getMemberInfo().getMemberGender()
                    , memberMoimLinker.getMoimRoleType(), memberMoimLinker.getMemberState()
                    , memberMoimLinker.getCreatedAt(), memberMoimLinker.getUpdatedAt()
            );
        }

        MoimPostDto moimPostDto = new MoimPostDto(
                moimPost.getId(), moimPost.getPostTitle(), moimPost.getPostContent(), moimPost.getMoimPostCategory()
                , moimPost.isNotice(), moimPost.getCreatedAt(), moimPost.getUpdatedAt(), moimPost.getUpdatedMemberId(), moimPost.isHasFiles()
                , isPostCreatorCurMember, moimMemberInfoDto
        );

        moimPostDto.setPostCommentsDto(postCommentsDto);

        return moimPostDto;
    }

    public MoimPostDto updatePost(MoimPostRequestDto moimPostRequestDto, Member curMember) {

        MoimPost moimPost = moimPostRepository.findWithMemberId(moimPostRequestDto.getMoimPostId(),
                curMember.getId());

        // TODO :: 수정할 권한 - 작성자인지 확인
        if (Objects.isNull(moimPost)) {
            throw new IllegalArgumentException("요청할 게시물이 없거나, 작성자가 아닙니다.");
        }

        boolean updated = moimPost.update(
                moimPostRequestDto.getPostTitle(),
                moimPostRequestDto.getPostContent(),
                moimPostRequestDto.isNotice(),
                moimPostRequestDto.getMoimPostCategory(),
                curMember.getId());

        if (updated) {
            return MoimPostDto.createMoimPostDto(moimPost, true);
        } else {
            // TODO : 수정 사항이 없는 것은 에러일까? 이 부분 논의 필요.
            // TODO : 로그가 필요한 지도 확인 필요.
            // 수정요청이 들어왔으나 수정된 사항이 없음
            log.error("수정된 사항이 없는 경우");
            throw new RuntimeException("수정된 사항이 없는 경우");
        }
    }

    public void deletePost(Long moimPostId, Member curMember) {

        MoimPost moimPost = moimPostRepository.findById(moimPostId);

        if (Objects.isNull(moimPost)) {
            log.error("해당 PK 의 게시물을 찾을 수 없습니다");
            throw new RuntimeException("해당 PK 의 게시물을 찾을 수 없습니다");
        }

        // 여기서 Member, Moim, MemberMoimLinker에 대한 3개 쿼리가 나감. --> MemberMoimLinker의 Member, Moim이 Eager이기 때문에 N+1 문제 발생
        MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findByMemberAndMoimId(curMember.getId(), moimPost.getMoim().getId());
        moimPost.delete(memberMoimLinker);


        moimPostRepository.removeMoimPostExecute(moimPost);
    }
}