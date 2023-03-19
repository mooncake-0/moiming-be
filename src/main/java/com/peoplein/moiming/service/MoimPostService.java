package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import com.peoplein.moiming.model.dto.domain.MoimPostDto;
import com.peoplein.moiming.model.dto.domain.PostCommentDto;
import com.peoplein.moiming.model.dto.request.MoimPostRequestDto;
import com.peoplein.moiming.model.query.QueryMoimPostDetails;
import com.peoplein.moiming.model.query.QueryPostCommentDetails;
import com.peoplein.moiming.repository.MemberMoimLinkerRepository;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
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
import java.time.LocalDateTime;
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

            boolean isPostCreatorCurrentMember = queryMoimPost.getPostCreatorInfoDto().getMemberUid().equals(curMember.getUid());

            MoimMemberInfoDto postCreatorInfoDto = null;

            if (!isPostCreatorCurrentMember) {
                postCreatorInfoDto = queryMoimPost.getPostCreatorInfoDto();
            }

            MoimPostDto moimPostDto = new MoimPostDto(
                    queryMoimPost.getMoimPostId()
                    , queryMoimPost.getPostTitle(), queryMoimPost.getPostContent(), queryMoimPost.getMoimPostCategory(), queryMoimPost.isNotice()
                    , queryMoimPost.getCreatedAt(), queryMoimPost.getUpdatedAt(), queryMoimPost.getUpdatedUid(), queryMoimPost.isHasFiles()
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

            boolean isCommentCreatorCurMember = queryDetail.getCommentCreatorInfoDto().getMemberUid().equals(curMember.getUid());

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

        boolean isPostCreatorCurMember = moimPost.getMember().getUid().equals(curMember.getUid());

        MoimMemberInfoDto moimMemberInfoDto = null;

        if (!isPostCreatorCurMember) {
            moimMemberInfoDto = new MoimMemberInfoDto(
                    moimPost.getMember().getId(), moimPost.getMember().getUid()
                    , moimPost.getMember().getMemberInfo().getMemberName()
                    , moimPost.getMember().getMemberInfo().getMemberEmail()
                    , moimPost.getMember().getMemberInfo().getMemberGender()
                    , moimPost.getMember().getMemberInfo().getMemberPfImg()
                    , memberMoimLinker.getMoimRoleType(), memberMoimLinker.getMemberState()
                    , memberMoimLinker.getCreatedAt(), memberMoimLinker.getUpdatedAt()
            );
        }

        MoimPostDto moimPostDto = new MoimPostDto(
                moimPost.getId(), moimPost.getPostTitle(), moimPost.getPostContent(), moimPost.getMoimPostCategory()
                , moimPost.isNotice(), moimPost.getCreatedAt(), moimPost.getUpdatedAt(), moimPost.getUpdatedUid(), moimPost.isHasFiles()
                , isPostCreatorCurMember, moimMemberInfoDto
        );

        moimPostDto.setPostCommentsDto(postCommentsDto);

        return moimPostDto;
    }

    public MoimPostDto updatePost(MoimPostRequestDto moimPostRequestDto, Member curMember) {

        // MoimPost 조회
        MoimPost moimPost = moimPostRepository.findWithMemberById(moimPostRequestDto.getMoimPostId());

        // 요청 유저의 권한 체킹, NULL 체킹
        if (Objects.isNull(moimPost)) {
            log.error("요청한 게시물을 찾을 수 없는 경우");
            throw new RuntimeException("요청한 게시물을 찾을 수 없는 경우");
        }

        // TODO :: 수정할 권한 - 작성자인지 확인
        if (moimPost.getMember().getId().equals(curMember.getId())) {
            log.error("게시물을 수정할 권한이 없는 경우 :: 작성자가 아님");
            throw new RuntimeException("게시물을 수정할 권한이 없는 경우 :: 작성자가 아님");
        }

        boolean isAnyUpdated = false;

        // 현재 Post 와 들어온 요청의 차이점 확인, Update 진행
        if (!moimPostRequestDto.getPostTitle().equals(moimPost.getPostTitle())) {
            isAnyUpdated = true;
            moimPost.changePostTitle(moimPostRequestDto.getPostTitle());
        }

        if (!moimPostRequestDto.getPostContent().equals(moimPost.getPostContent())) {
            isAnyUpdated = true;
            moimPost.changePostContent(moimPostRequestDto.getPostContent());
        }

        if (moimPostRequestDto.isNotice() != moimPost.isNotice()) {
            isAnyUpdated = true;
            moimPost.setNotice(moimPost.isNotice());
        }

        if (moimPostRequestDto.getMoimPostCategory().equals(moimPost.getMoimPostCategory())) {
            isAnyUpdated = true;
            moimPost.changePostCategory(moimPostRequestDto.getMoimPostCategory());
        }

        if (isAnyUpdated) {

            moimPost.setUpdatedAt(LocalDateTime.now());
            moimPost.setUpdatedUid(curMember.getUid());

            return new MoimPostDto(moimPost.getId()
                    , moimPost.getPostTitle()
                    , moimPost.getPostContent()
                    , moimPost.getMoimPostCategory()
                    , moimPost.isNotice()
                    , moimPost.getCreatedAt()
                    , moimPost.getUpdatedAt()
                    , moimPost.getUpdatedUid()
                    , moimPost.isHasFiles()
                    , true // 수정자는 생성자
                    , null
            );
        } else {
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
        } else {

            MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findByMemberAndMoimId(curMember.getId(), moimPost.getMoim().getId());

            if (!moimPost.getMember().getId().equals(curMember.getId())) {
                // 작성자가 아니라면, 관리자인가?
                if (!memberMoimLinker.getMoimRoleType().equals(MoimRoleType.LEADER) && !memberMoimLinker.getMoimRoleType().equals(MoimRoleType.MANAGER)) {
                    log.error("삭제할 권한이 없는 유저의 요청입니다");
                    throw new RuntimeException("삭제할 권한이 없는 유저의 요청입니다");
                }
            }

            // TODO :: POST FILE 연결자들 삭제 필요
            postCommentRepository.removeAllByMoimPostId(moimPostId);
            moimPostRepository.remove(moimPost);

        }
    }
}

