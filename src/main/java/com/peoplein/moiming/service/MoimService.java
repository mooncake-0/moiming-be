package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.rules.MoimRule;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.model.dto.domain.*;
import com.peoplein.moiming.model.dto.request.MoimMemberActionRequestDto;
import com.peoplein.moiming.model.dto.request.MoimRequestDto;
import com.peoplein.moiming.model.dto.response.MoimResponseDto;
import com.peoplein.moiming.model.query.QueryJoinedMoimBasic;
import com.peoplein.moiming.repository.*;
import com.peoplein.moiming.repository.jpa.query.MoimJpaQueryRepository;
import com.peoplein.moiming.service.core.MoimServiceCore;
import com.peoplein.moiming.service.input.MoimServiceInput;
import com.peoplein.moiming.service.output.MoimServiceOutput;
import com.peoplein.moiming.service.shell.MoimServiceShell;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MoimService {

    private final MoimRepository moimRepository;
    private final CategoryRepository categoryRepository;
    private final MoimCategoryLinkerRepository moimCategoryLinkerRepository;
    private final MoimJpaQueryRepository moimJpaQueryRepository;
    private final MemberMoimLinkerRepository memberMoimLinkerRepository;
    private final MoimPostRepository moimPostRepository;
    private final ScheduleRepository scheduleRepository;
    private final PostCommentRepository postCommentRepository;
    private final MemberScheduleLinkerRepository memberScheduleLinkerRepository;
    private final MoimServiceShell moimServiceShell;
    private final MoimServiceCore moimServiceCore;

    /*
       유저가 소속된 모든 모임의 Basic 정보를 반환한다
     > TODO : 등록되어 있는 일정 확인 들어가면 좋을 듯
     */
    public List<MoimResponseDto> viewMemberMoim(Member curMember) {

        List<MoimResponseDto> moimResponseDtos = new ArrayList<>();
        List<QueryJoinedMoimBasic> queryDataList
                = moimJpaQueryRepository.findQueryMoimBasicAndFetchCollections(curMember.getId());

        for (QueryJoinedMoimBasic queryData : queryDataList) {

            MoimDto moimDto = new MoimDto(
                    queryData.getMoimId(), queryData.getMoimName(), queryData.getMoimInfo(), queryData.getMoimArea(), queryData.getCurMemberCount()
                    , queryData.isHasRuleJoin(), queryData.isHasRulePersist(), queryData.getCreatedAt(), queryData.getCreatedMemberId(), queryData.getUpdatedAt(), queryData.getUpdatedMemberId()
            );

            MyMoimLinkerDto myMoimLinkerDto = new MyMoimLinkerDto(
                    queryData.getMoimRoleType(), queryData.getMemberState(), queryData.getMemberLinkerCreatedAt(), queryData.getMemberLinkerUpdatedAt()
            );

            // MEMO :: Basic 요청에는 모임 멤버들의 정보를 전달하지 않는다
            MoimMembersDto moimMembersDto = new MoimMembersDto(
                    myMoimLinkerDto, new ArrayList<>()
            );

            RuleJoinDto ruleJoinDto = null;
            RulePersistDto rulePersistDto = null;

            if (queryData.isHasRuleJoin()) {
                ruleJoinDto = queryData.getRuleJoinDto();
            }
            if (queryData.isHasRulePersist()) {
                rulePersistDto = queryData.getRulePersistDto();
            }

            moimResponseDtos.add(new MoimResponseDto(
                    moimDto, ruleJoinDto, rulePersistDto, moimMembersDto, queryData.getCategoriesDto()
            ));
        }

        return moimResponseDtos;
    }


    /*
     모임 생성자의 모임 생성 요청을 받고 모임 생성을 수행
     모임 -> 카테고리 연동 -> 가입규칙 연동 -> 모임 생성자 연동으로 진행
     요청 응답 수행
     */
    public MoimResponseDto createMoim(Member curMember, MoimRequestDto requestDto) {
        MoimServiceInput moimServiceInput = moimServiceShell.readyForCreateMoim(requestDto, curMember);
        MoimServiceOutput moimServiceOutput = moimServiceCore.createMoim(moimServiceInput);
        return moimServiceShell.wrapUpAfterCreatingMoim(moimServiceOutput, moimServiceInput);
    }

    @Transactional(readOnly = true)
    public MoimResponseDto getMoim(Long moimId, Member curMember) {

        // Moim 정보 ~ Member 정보 모두 보내줌
        // memberMoimLinker 들 필요시 한번에 Batch 로 들고오게 됨.
        Moim moim = moimRepository.findOptionalById(moimId).orElseThrow(() -> new RuntimeException("요청한 모임을 찾을 수 없는 경우"));
        return createMoimResponseDto(moim, curMember);
    }


    public MoimResponseDto updateMoim(MoimRequestDto moimRequestDto, Member curMember) {
        MoimServiceInput inputForUpdate = moimServiceShell.getInputForUpdate(moimRequestDto, curMember);
        MoimServiceOutput moimServiceOutput = moimServiceCore.hasAnyUpdated(inputForUpdate);
        return moimServiceShell.updateMoim(moimServiceOutput, inputForUpdate);
    }

    private MoimResponseDto createMoimResponseDto(Moim moim, Member curMember) {
        // null인 상태에서 만약 moim.getRuleJoin()의 값이 null이면 그냥 null임.
        // 결국 moim.getRuleJoin()에 따라서 값이 정해진다는 소리. 초기값 null은 아무 소용이 없다.
        List<Category> categories = moimCategoryLinkerRepository.findWithCategoryByMoimId(moim.getId())
                .stream().map(MoimCategoryLinker::getCategory)
                .collect(Collectors.toList());

        MoimMembersDto moimMembersDto = new MoimMembersDto();
        moim.getMemberMoimLinkers().stream().filter(memberMoimLinker -> memberMoimLinker.getMember().getId().equals(curMember.getId()))
                .forEach(memberMoimLinker -> moimMembersDto.setMyMoimLinkerDto(new MyMoimLinkerDto(memberMoimLinker)));

        List<MoimMemberInfoDto> moimMemberInfoDtos = moim.getMemberMoimLinkers().stream().filter(memberMoimLinker -> !memberMoimLinker.getMember().getId().equals(curMember.getId()))
                .map(MoimMemberInfoDto::createMemberInfoDto).collect(Collectors.toList());
        moimMembersDto.setMoimMemberInfoDto(moimMemberInfoDtos);

        return new MoimResponseDto(moim, moim.getRuleJoin(), moim.getRulePersist(), categories, moimMembersDto);
    }


    public void deleteMoim(Long moimId, Member curMember) {

        // MML 로 Moim 까지 확인
        MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findWithMoimByMemberAndMoimId(curMember.getId(), moimId);
        Moim moim = memberMoimLinker.getMoim();
        // Moim 확인
        if (Objects.isNull(moim)) {
            log.error("요청한 모임 혹은 모임관계를 찾을 수 없는 경우");
            throw new RuntimeException("요청한 모임 혹은 모임관계를 찾을 수 없는 경우");
        }

        // 요청자 권한 확인
        if (!memberMoimLinker.getMoimRoleType().equals(MoimRoleType.LEADER)
                && !memberMoimLinker.getMoimRoleType().equals(MoimRoleType.MANAGER)
                && !memberMoimLinker.getMemberState().equals(MoimMemberState.ACTIVE)) {
            log.error("모임을 삭제할 권한이 없는 경우");
            throw new RuntimeException("모임을 삭제할 권한이 없는 경우");
        }

        try {

            // 게시물, 댓글, 게시물 파일 모두 삭제
            List<MoimPost> moimPosts = moimPostRepository.findByMoimId(moimId);
            List<Long> moimPostIds = moimPosts.stream().map(MoimPost::getId).collect(Collectors.toList());
            // moimPostId 에 속하는 모든 댓글, 모든 게시물 삭제
            // TODO:: FILE DOMAIN 추가시 삭제 로직
            postCommentRepository.removeAllByMoimPostIds(moimPostIds);
            moimPostRepository.removeAll(moimPostIds);


            // 일정, 회원 일정 연결자 모두 삭제
            List<Schedule> schedules = scheduleRepository.findByMoimId(moimId);
            List<Long> scheduleIds = schedules.stream().map(Schedule::getId).collect(Collectors.toList());
            memberScheduleLinkerRepository.removeAllByScheduleIds(scheduleIds);
            scheduleRepository.removeAll(scheduleIds);

            // TODO : 모임 후기 모두 삭제
            // TODO : 정산활동, 정산활동 연결자 모두 삭제
            // TODO : 회비, 회비 연결자 모두 삭제

            // 종류 연결자 모두 삭제
            moimCategoryLinkerRepository.removeAllByMoimId(moimId);

            // 연관 회원 연결자 모두 삭제 // Cascade 로 인한 자동 삭제
            // 규칙 모두 삭제 // Cascade 로 인한 자동 삭제

            // 가 되어야 Moim 삭제 가능
            moimRepository.remove(moim);

        } catch (Exception e) {
            log.error("삭제 실패:: " + e.getMessage());
            throw new RuntimeException("삭제 실패:: " + e.getMessage());
        }
    }


    //
    private MoimRule createMoimRule(RuleJoinDto ruleJoinDto, Moim createdMoim, Member curMember) {
        return new RuleJoin(
                ruleJoinDto.getBirthMax(),
                ruleJoinDto.getBirthMin(),
                ruleJoinDto.getGender(),
                ruleJoinDto.getMoimMaxCount(),
                ruleJoinDto.isDupLeaderAvailable(),
                ruleJoinDto.isDupManagerAvailable(),
                createdMoim, curMember.getId());
    }

    public MoimServiceCore getMoimServiceCore() {
        return moimServiceCore;
    }
}