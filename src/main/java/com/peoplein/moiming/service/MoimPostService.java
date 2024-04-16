package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.file.File;
import com.peoplein.moiming.domain.file.FileDomain;
import com.peoplein.moiming.domain.file.RelatedFile;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.inner.MoimPostWithFileDto;
import com.peoplein.moiming.model.dto.inner.PostDetailsInnerDto;
import com.peoplein.moiming.model.dto.inner.StateMapperDto;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.peoplein.moiming.domain.file.FileDomain.POST;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.inner.PostDetailsInnerDto.*;
import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;


/*

해당 게시글을 작성한 사람은 수정/삭제가 가능하다.

운영자가 작성할 수 있는 카테고리는 공지, 가입인사, 모임후기, 자유글

참여자가 작성할 수 있는 카테고리는 가입인사, 모임후기, 자유글

모이밍 서비스를 탈퇴한 멤버가 작성한 글은 user명이 --> 탈퇴한 사용자으로 남겨진다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MoimPostService {

    private final StorageService storageService;
    private final RelatedFileService relatedFileService;
    private final NotificationService notificationService;
    private final PostCommentService postCommentService;
    private final MoimMemberService moimMemberService;
    private final MoimMemberRepository moimMemberRepository;
    private final MoimPostRepository moimPostRepository;
    private final MoimRepository moimRepository;
    private final PostCommentRepository postCommentRepository;


    @Transactional
    public List<File> updateImgs(List<MultipartFile> imgFiles, Long moimId, Long moimPostId, Member member) {

        if (moimId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), moimId).orElseThrow(() -> {
                    log.warn("{}, updateImgs :: {}", this.getClass().getName(), "[" + member.getId() + "] 의 유저는 [" + moimId + "]의 모임원이 아닙니다");
                    return new MoimingApiException(MOIM_MEMBER_NOT_FOUND);
                }
        );

        if (!moimMember.hasActivePermission()) {
            log.warn("{}, updateImgs :: {}", this.getClass().getName(), "[" + member.getId() + "] 요청한 유저는 활동중인 유저가 아닙니다");
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }


        if (moimPostId != null) {
            // 수정 Variation
            // moimPost 의 존재성 확인
            MoimPost moimPost = moimPostRepository.findById(moimPostId).orElseThrow(() -> {
                        log.warn("{}, updateImgs :: {}", this.getClass().getName(), "[" + moimPostId + "] 의 게시물은 존재하지 않습니다");
                        return new MoimingApiException(MOIM_POST_NOT_FOUND);
                    }
            );

            // moimMember 가 update 권한이 있는지 확인 (작성자가 아니면 안됨)
            if (!moimPost.getMember().getId().equals(member.getId())) {
                log.warn("{}, updateImgs :: {}", this.getClass().getName(), "[" + member.getId() + "] 요청한 유저는 해당 게시물을 수정할 권한이 없습니다");
                throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
            }

        }

        List<File> files = storageService.updateMoimPostImgs(imgFiles, member);

        return files;
    }


    @Transactional
    public MoimPostWithFileDto createMoimPost(MoimPostCreateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), requestDto.getMoimId())
                .orElseThrow(() -> new MoimingApiException(MOIM_MEMBER_NOT_FOUND));

        if (!moimMember.getMemberState().equals(MoimMemberState.ACTIVE)) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        // Notification 을 위해서 필요하므로, Moim 과 관련 유저들은 모두 조회한다
        // MOIM 에 대한 EAGER 조회
        Moim moim = moimRepository.findWithActiveMoimMembersById(requestDto.getMoimId())
                .orElseThrow(() -> new MoimingApiException(MOIM_NOT_FOUND));

        MoimPost post = MoimPost.createMoimPost(requestDto.getPostTitle(), requestDto.getPostContent()
                , MoimPostCategory.fromValue(requestDto.getMoimPostCategory())
                , requestDto.getHasPrivateVisibility(), requestDto.getHasFiles()
                , moim, member);

        moimPostRepository.save(post); // 영속화 후 Id 받은 상태 필요

        if(requestDto.getHasFiles() != null) {
            checkFileReqStatus(requestDto.getHasFiles(), requestDto.getImgFileIds());
            if (requestDto.getHasFiles()) { // 존재할 경우 저장
                relatedFileService.saveRelatedFiles(post.getId(), requestDto.getImgFileIds(), member.getId());
            }
        }else{
            // 여기서 발생할 리는 없음, Validation 존재
            throw new MoimingApiException(COMMON_REQUEST_VALIDATION);
        }

        // 게시글 생성자를 제외하고 전달시킨다
        List<MoimMember> moimMembers = moim.getMoimMembers();
        List<Member> receivers = moimMembers.stream()
                .map(MoimMember::getMember)
                .filter(mem -> !mem.getId().equals(member.getId()))
                .collect(Collectors.toList());

        notificationService.createManyNotification(NotificationTopCategory.MOIM, NotificationSubCategory.POST_CREATE, NotificationType.INFORM
                , receivers, "", moim.getMoimName() + "에 새로운 게시글이 등록되었습니다", requestDto.getMoimId(), post.getId());

        List<RelatedFile> relatedFiles = relatedFileService.getRelatedFilesBySeq(POST, post.getId());
        return new MoimPostWithFileDto(post, relatedFiles);
    }


    // MEMO :: 아쉬운 부분 : 오직 모임장 ID 와의 일치 정보를 위해 Moim 을 Fetch Join 해야 함
    // 모임의 모든 Post 전달 (필요 정보는 사실상 File 빼고 전부 다)
    @Transactional(readOnly = true)
    public StateMapperDto<MoimPost> getMoimPosts(Long moimId, Long lastPostId, MoimPostCategory category, int limit, Member member) {

        if (moimId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        MoimPost lastPost = null;
        if (lastPostId != null) {
            lastPost = moimPostRepository.findById(lastPostId).orElseThrow(() ->
                    new MoimingApiException(MOIM_POST_NOT_FOUND)
            );
        }

        // 멤버가 구성원인지 확인 필요 - 자체 필드에 대한 제어 로직 - 도메인단에 둘 수가 없음
        boolean moimMemberRequest = true;
        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), moimId).orElse(null);
        if (moimMember == null || !moimMember.getMemberState().equals(MoimMemberState.ACTIVE)) {
            moimMemberRequest = false;
        }

        // Sort 됨, 중요!
        List<MoimPost> moimPosts = moimPostRepository.findWithMemberAndInfoByCategoryAndLastPostOrderByDateDesc(moimId, lastPost, category, limit, moimMemberRequest);

        // Post 들의 작성자들 상태
        Set<Long> postCreatorIds = moimPosts.stream().map(moimPost -> moimPost.getMember().getId()).collect(Collectors.toSet());
        Map<Long, MoimMemberState> postCreatorStates = moimMemberService.getMoimMemberStates(moimId, postCreatorIds);

        return new StateMapperDto<>(moimPosts, postCreatorStates);
    }


    // 특정 Post 의 모든 Data 전달
    // 게시물 정보 반환, 게시물 댓글들 반환,
    @Transactional(readOnly = true)
    public PostDetailsDto getMoimPostDetail(Long postId, Member member) {

        if (postId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // post 존재성 확인
        MoimPost moimPost = moimPostRepository.findWithMoimAndMemberAndInfoById(postId).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_NOT_FOUND)
        );

        // 요청자가 게시물 세부 정보를 요청할 수 있는가?
        Optional<MoimMember> moimMemberOp = moimMemberRepository.findByMemberAndMoimId(member.getId(), moimPost.getMoim().getId());
        moimPost.checkMemberAccessibility(moimMemberOp);

        PostCommentDetailsDto commentsDto = postCommentService.getSortedPostComments(postId);
        commentsDto.getCommentCreatorIds().add(moimPost.getMember().getId()); // 게시물 생성자가 없을 수도 있음, 추가해준다 // 게시물 생성자의 memberState 메핑을 위함

        // moimMemberService 한테 이 MoimMember 들에 대한 상태를 조회한다 - 단, Member 는 이시점에서 Join 되지 않는다, PostComment 객체에서 다 fetchJoin 되었기 때문에 Controller 에선 이걸 사용한다
        Map<Long, MoimMemberState> memberStates = moimMemberService.getMoimMemberStates(moimPost.getMoim().getId(), commentsDto.getCommentCreatorIds());

        List<RelatedFile> files = new ArrayList<>();
        if (moimPost.isHasFiles()) {
            files = relatedFileService.getRelatedFilesBySeq(POST, moimPost.getId());
        }
        return new PostDetailsDto(new MoimPostWithFileDto(moimPost, files), memberStates, commentsDto.getParentComments(), commentsDto.getChildCommentsMap());

    }


    @Transactional
    public MoimPostWithFileDto updateMoimPost(MoimPostUpdateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        checkFileUpdateStatus(requestDto);

        // post 존재성 확인
        // 게시물 생성자도 같이 보내줘야 하기 때문에 fetch join 해놓는다
        MoimPost moimPost = moimPostRepository.findWithMoimAndMemberAndInfoById(requestDto.getMoimPostId()).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_NOT_FOUND)
        );

        // post 에 저장된 moimId + member 로 moimMember 조회
        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), moimPost.getMoim().getId()).orElseThrow(() -> {
                    log.error("{}, updateMoimPost :: {}", this.getClass().getName(), "요청한 유저는 해당 모임에 가입한 적이 없습니다");
                    return new MoimingApiException(MOIM_MEMBER_NOT_FOUND);
                }
        );

        // MoimMember 권한 확인 (활동중이여야 하며, 작성자여야 한다)
        if (!moimMember.hasActivePermission() || !moimPost.getMember().getId().equals(member.getId())) {
            log.error("{}, updateMoimPost :: {}", this.getClass().getName(), "요청한 유저는 모임 게시물을 수정할 권한이 없습니다");
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        // 설정은 다 넣어주고, 변화를 요청한 애들에 한해서 최신화를 해줌
        moimPost.changeMoimPostInfo(requestDto.getPostTitle(), requestDto.getPostContent(), MoimPostCategory.fromValue(requestDto.getMoimPostCategory())
                , requestDto.getHasPrivateVisibility(), member.getId());

        if (requestDto.getFileChanged()) { // 차피 파일이 있다가 없어도, 없다가 있어도 HasFiles 는 변해야 함, 그래서 이 분기 안에서 처리한다
            checkFileReqStatus(requestDto.getHasFiles(), requestDto.getImgFileIds());
            moimPost.changeHasFiles(requestDto.getHasFiles(), member.getId());
            // 다 삭제든, 추가든, 수정이든 다 해당 로직으로 처리된다
            List<String> removalFileNames = relatedFileService.updateDomainRelatedFilesAndReturnRemovalNames(POST, moimPost.getId(), requestDto.getImgFileIds(), member.getId());
            storageService.deleteMoimPostImgs(removalFileNames);
        }

        List<RelatedFile> files = new ArrayList<>();
        if (moimPost.isHasFiles()) {
            files = relatedFileService.getRelatedFilesBySeq(POST, moimPost.getId());
        }

        return new MoimPostWithFileDto(moimPost, files);
    }


    // Post 삭제 (댓글 / 답글에 대한 로직 처리 후 최종 진행)
    @Transactional
    public void deleteMoimPost(Long postId, Member member) {

        if (postId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // post 존재성 확인
        MoimPost moimPost = moimPostRepository.findById(postId).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_NOT_FOUND)
        );

        // post 에 저장된 moimId + member 로 moimMember 조회
        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), moimPost.getMoim().getId()).orElseThrow(() ->
                new MoimingApiException(MOIM_MEMBER_NOT_FOUND)
        );

        // ACTIVE 아니면 무조건 안됨
        // ACTIVE 안에서, MoimMember 권한 확인 (운영자 or 작성자) > 역은 둘다 동시에 아닌 것
        if (!moimMember.hasActivePermission() ||
                (!moimMember.hasPermissionOfManager() && !moimPost.getMember().getId().equals(member.getId()))) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        // 댓글 삭제 진행
        postCommentRepository.removeAllByMoimPostId(postId);

        // 게시물 삭제 진행
        moimPostRepository.remove(moimPost);

        // TODO:: 파일 삭제 진행
    }

    private void checkFileReqStatus(boolean hasFiles, List<Long> fileIds) {
        if (hasFiles) { // 있다고 했는데 파일 없음
            if (fileIds == null || fileIds.isEmpty()) {
                throw new MoimingApiException(MOIM_POST_IMG_STATE_NOT_VALID);
            }
        } else {// 없다고 했는데 파일 있음
            if (fileIds != null && !fileIds.isEmpty()) {
                throw new MoimingApiException(MOIM_POST_IMG_STATE_NOT_VALID);
            }
        }
    }

    private void checkFileUpdateStatus(MoimPostUpdateReqDto requestDto) {

        if (requestDto.getFileChanged() == null) {
            throw new MoimingApiException(MOIM_POST_IMG_STATE_NOT_VALID);
        }

        if (requestDto.getFileChanged()) { // TRUE 인데
            // 변경 사항에 대한 정보들이 들어오지 않을 경우
            // ImgFileId 가 더 이상 없어도 (삭제되어도) 빈 리스트를 담아야 함
            if (requestDto.getHasFiles() == null || requestDto.getImgFileIds() == null) {
                throw new MoimingApiException(MOIM_POST_IMG_STATE_NOT_VALID);
            }
        }

    }

}
