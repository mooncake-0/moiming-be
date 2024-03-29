package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.file.File;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.moim.*;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.inner.MoimCategoryMapperDto;
import com.peoplein.moiming.model.query.QueryMoimSuggestMapDto;
import com.peoplein.moiming.repository.*;
import com.peoplein.moiming.repository.jpa.FileJpaRepository;
import com.peoplein.moiming.repository.jpa.MoimJoinRuleJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.MoimCreateReqDto.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MoimService {

    private final StorageService storageService;
    private final FileJpaRepository fileJpaRepository;

    private final MoimRepository moimRepository;
    private final MoimMemberRepository moimMemberRepository;
    private final CategoryService categoryService;


    private final MoimCategoryLinkerRepository moimCategoryLinkerRepository;
    private final PostCommentRepository postCommentRepository;
    private final MoimPostRepository moimPostRepository;
    private final MoimJoinRuleJpaRepository moimJoinRuleRepository;
    private final MoimCountRepository moimCountRepository;
    private final MoimCountService moimCountService;


    public File updateImg(MultipartFile imgFile, Long moimId, Member member) {

        if (member == null) { // 자신이 활용하는 건에 대해서만 책임
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        File file = storageService.uploadMoimImg(imgFile);

        // moimId 가 있을시 - 1) 모임 존재성 확인 2) member 의 모임장 여부 확인 3) 수정이므로 기존을 삭제하기 위한 storage 요청
        if (moimId != null) {
            Moim moim = moimRepository.findById(moimId).orElseThrow(() ->
                    new MoimingApiException(MOIM_NOT_FOUND)
            );

            if (!member.getId().equals(moim.getCreatorId())) {
                log.warn("{}, updateImg :: {}", this.getClass().getName(), "[" + member.getId() + "] 의 [" + moim.getId() + "] 이미지 변경 시도, 모임장 아님");
                throw new MoimingApiException(MEMBER_NOT_AUTHORIZED);
            }

            storageService.deleteMoimImg(moim.getImgFileId());
            moim.deleteImg();
            moim.changeImg(file);
        }

        return file;
    }


    public void deleteImg(Long moimId, Member member) {

        if (moimId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        Moim moim = moimRepository.findById(moimId).orElseThrow(() ->
                new MoimingApiException(MOIM_NOT_FOUND)
        );

        if (!member.getId().equals(moim.getCreatorId())) {
            log.warn("{}, deleteImg :: {}", this.getClass().getName(), "[" + member.getId() + "] 의 [" + moim.getId() + "] 이미지 삭제 시도, 모임장 아님");
            throw new MoimingApiException(MEMBER_NOT_AUTHORIZED);
        }

        if (moim.getImgFileId() == null) {
            log.info("{}, deleteImg :: {}", this.getClass().getName(), "[" + moim.getId() + "] 는 이미 모임 사진이 없음");
            throw new MoimingApiException(STORAGE_FILE_NOT_FOUND);
        }

        storageService.deleteMoimImg(moim.getImgFileId());
        moim.deleteImg();

    }


    // 모임 생성
    public Moim createMoim(MoimCreateReqDto requestDto, Member curMember) {

        if (requestDto == null || curMember == null || requestDto.getCategoryNameValues().size() != 2) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // 생성 자격에 대해서 논할 필요는 없음
        // 1. 카테고리를 준비한다 (같이 생성 필요)
        // Category 가 완성된걸 추가만 한다
        List<Category> categories = categoryService.generateCategoryList(requestDto.getCategoryNameValues());

        // 2.생성 Trial
        Moim moim = Moim.createMoim(requestDto.getMoimName(), requestDto.getMoimInfo(), requestDto.getMaxMember()
                , new Area(requestDto.getAreaState(), requestDto.getAreaCity()), categories, curMember);

        // 가입조건 있을시 SU
        if (requestDto.getHasJoinRule()) {
            moim.setMoimJoinRule(generateJoinRule(requestDto.getJoinRuleDto()));
        }

        // 프로필 사진이 있을시 SU
        if (requestDto.getImgFileId() != null) {
            File file = fileJpaRepository.findById(requestDto.getImgFileId()).orElseThrow(() -> {
                log.error("{}, createMoim :: {}", this.getClass().getName(), "id: [" + requestDto.getImgFileId() + "] 의 파일을 찾을 수 없습니다");
                return new MoimingApiException(STORAGE_FILE_NOT_FOUND);
            });
            moim.changeImg(file);
        }

        // 카테고리 SU
        moimRepository.save(moim);

        return moim;
    }


    // 컨디션에 따른 해당 멤버의 모임 조회 결과 List<Moim> 으로 반환
    // 사용영역 > Home 화면 및 마이페이지 화면에서 사용될 예정
    public MoimCategoryMapperDto getMemberMoims(Long lastMoimId, boolean isManagerReq, int limit, Member curMember) {

        if (curMember == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        Moim lastMoim = null;
        if (lastMoimId != null) {
            lastMoim = moimRepository.findById(lastMoimId).orElse(null); // NULLABLE
        }

        List<MoimMember> memberMoims = moimMemberRepository.findMemberMoimsWithCursorConditions(curMember.getId(), true, isManagerReq, lastMoim, limit);
        List<Moim> targetMoims = new ArrayList<>();
        List<Long> moimIds = new ArrayList<>();

        for (MoimMember memberMoim : memberMoims) {
            targetMoims.add(memberMoim.getMoim());
            moimIds.add(memberMoim.getMoim().getId());
        }

        List<MoimCategoryLinker> categoryLinkers = new ArrayList<>();
        if (!moimIds.isEmpty()) {
            categoryLinkers = moimCategoryLinkerRepository.findWithCategoryByMoimIds(moimIds);
        }

        return new MoimCategoryMapperDto(targetMoims, categoryLinkers);
    }


    // 모임 세부 조회 - 정보 , 가입조건, 지역 및 카테고리 ALL -- 누구나 요청 가능
    public MoimMember getMoimDetail(Long moimId, Member member) {

        if (moimId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        Moim moim = moimRepository.findWithJoinRuleAndCategoriesById(moimId).orElseThrow(() ->
                new MoimingApiException(MOIM_NOT_FOUND)
        );

        MoimMember creatorMember = moimMemberRepository.findWithMemberAndInfoByMemberAndMoimId(moim.getCreatorId(), moim.getId()).orElseThrow(() -> {
            log.error("{}, {}", "모임 생성자 정보를 찾을 수 없음, C999", COMMON_INVALID_SITUATION.getErrMsg());
            return new MoimingApiException(COMMON_INVALID_SITUATION);
        });

        moimCountService.processMoimCounting(member, moim);


        return creatorMember;
    }


    // 모임 수정하기
    public Moim updateMoim(MoimUpdateReqDto requestDto, Member curMember) {

        // 차피 Moim 존속 여부랑은 별개로, MoimMember 가 있으면 되는 것이니, MoimMember 로 그냥 join 해와버리자
        MoimMember moimMemberPs = moimMemberRepository.findWithMoimAndCategoriesByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElseThrow(() ->
                new MoimingApiException(MOIM_MEMBER_NOT_FOUND)
        );

        if (!moimMemberPs.hasPermissionOfManager()) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        List<Category> requestCategories = categoryService.generateCategoryList(requestDto.getCategoryNameValues());
        moimMemberPs.getMoim().updateMoim(requestDto, requestCategories, curMember.getId()); // Moim 도 영속화됨

        return moimMemberPs.getMoim();

    }


    // 가입 조건 수정
    public Moim updateMoimJoinRule(MoimJoinRuleUpdateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // 가입조건이 있든 없든 요청 들어온 조건을 만들어서 갈아 끼워준다
        Moim moim = moimRepository.findWithJoinRuleById(requestDto.getMoimId()).orElseThrow(() ->
                new MoimingApiException(MOIM_NOT_FOUND)
        );

        // MoimMember 조회,
        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), requestDto.getMoimId()).orElseThrow(() ->
                new MoimingApiException(MOIM_MEMBER_NOT_FOUND)
        );

        // 수정할 권한 확인
        if (!moimMember.hasPermissionOfManager()) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        // 모임 허용 인원 수 수정이 있다면 수정한다
        if (requestDto.getMaxMember() != null) {
            moim.updateMaxMember(requestDto.getMaxMember(), member.getId());
        }

        if (moim.getMoimJoinRule() == null) {
            MoimJoinRule joinRule = MoimJoinRule.createMoimJoinRule(requestDto.getHasAgeRule(), requestDto.getAgeMax(), requestDto.getAgeMin(), requestDto.getMemberGender());
            moim.setMoimJoinRule(joinRule);
        } else {
            moim.getMoimJoinRule().changeJoinRule(requestDto.getHasAgeRule(), requestDto.getAgeMax(), requestDto.getAgeMin(), requestDto.getMemberGender());
        }

        return moim;
    }


    // TODO :: MVP 에선 삭제한다. 실제 데이터를
    //         원래 데이터 삭제는 드문 일임을 알아두자
    //         MoimPost 를 제외하고는 모두 조회는 필요 없음
    // 모임 삭제 (MANAGER 권한)
    public void deleteMoim(Long moimId, Member member) {

        if (moimId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // 모임 삭제를 위한 조회를 진행
        Moim moim = moimRepository.findWithJoinRuleAndCategoriesById(moimId).orElseThrow(() ->
                new MoimingApiException(MOIM_NOT_FOUND)
        );

        // 요청한 인원의 존재 여부와 권한을 확인한다
        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), moimId).orElseThrow(() ->
                new MoimingApiException(MOIM_MEMBER_NOT_FOUND)
        );

        if (!moimMember.hasPermissionOfManager()) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        // 게시물 우선 제어를 위해 조회
        List<MoimPost> moimPosts = moimPostRepository.findByMoimId(moim.getId());

        // MEMO :: JPQL 을 발생시키는건 Flush 를 진행하게 된다 - 영컨을 이용하면 순서가 보장되지 않음, FK 오류 가능, OrphanRemoval, Cascade.REMOVE 옵션은 사용하지 않도록 하자
        // 1) 모든 Post Comment 삭제 - Moim Post 를 Iterate 하면서 진행
        //    IN 절을 써서 한번에 삭제하면 풀스캔을 때려서 일일이 하는 것보도 오래 걸릴 것으로 판단됨
        for (MoimPost moimPost : moimPosts) {
            postCommentRepository.removeAllByMoimPostId(moimPost.getId()); // 모든 댓글들을 실제로 삭제한다
        }

        // 2) 모든 MoimPost 삭제
        moimPostRepository.removeAllByMoimId(moim.getId());

        // 3) 모든 MoimMember 삭제
        moimMemberRepository.removeAllByMoimId(moim.getId());

        // 4) MoimCategoryLinker 삭제
        moimCategoryLinkerRepository.removeAllByMoimId(moim.getId());

        // TODO :: 제대로된 정책 필요 (FK 연관 정책 필요)
        moimCountRepository.removeAllByMoimId(moim.getId());


        // 5) Moim 삭제
        moimRepository.remove(moim.getId());

        // 6) MoimJoinRule 삭제 // MEMO :: Moim 이 JoinRule 과의 연관관계 주인이므로, Moim 을 먼저 삭제한다
        if (moim.getMoimJoinRule() != null) {
            moimJoinRuleRepository.removeById(moim.getMoimJoinRule().getId());
        }
    }


    // Entity 에 DTO 누수하지 않기 위함
    private MoimJoinRule generateJoinRule(JoinRuleCreateReqDto ruleDto) {
        return MoimJoinRule.createMoimJoinRule(ruleDto.getHasAgeRule(), ruleDto.getAgeMax(), ruleDto.getAgeMin(), ruleDto.getMemberGender());
    }


    public MoimCategoryMapperDto getSuggestedMoim(AreaValue areaFilter, CategoryName categoryFilter, int offset, int limit) {

        // 지역 필터가 있으면, 지역 넘겨주면 됨 - AND 조건 걸림, 없으면 NULL 로 전달됨
        // 카테고리 필터가 있으면, 카테고리 가져오면 됨 - AND 조건 걸림, 없으면 NULL 로 전달됨
        if (categoryFilter != null) {
            if (categoryFilter.getDepth() == 0) { // 1 차 카테고리 필터는 금지되어 있다
                log.info("{}, getSuggestedMoim :: {}", this.getClass().getName(), "1차 카테고리는 검색 필터가 될 수 없습니다");
                throw new MoimingApiException(COMMON_INVALID_SITUATION);
            }
        }

        LocalDate conditionThisMonth = LocalDate.now();
        conditionThisMonth = conditionThisMonth.withDayOfMonth(1);
        LocalDate conditionLastMonth = conditionThisMonth.minusMonths(1);

        List<QueryMoimSuggestMapDto> suggestedMoimDto = moimCountRepository.findMonthlyBySuggestedCondition(areaFilter, categoryFilter, List.of(conditionLastMonth, conditionThisMonth), offset, limit);
        List<Moim> targetMoims = new ArrayList<>();
        List<Long> moimIds = new ArrayList<>();

        for (QueryMoimSuggestMapDto queryDto : suggestedMoimDto) {
            targetMoims.add(queryDto.getMoim());
            moimIds.add(queryDto.getMoim().getId());
        }

        List<MoimCategoryLinker> categoryLinkers = new ArrayList<>();
        if (!moimIds.isEmpty()) {
            categoryLinkers = moimCategoryLinkerRepository.findWithCategoryByMoimIds(moimIds);
        }
        return new MoimCategoryMapperDto(targetMoims, categoryLinkers);

    }

}