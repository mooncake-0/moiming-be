package com.peoplein.moiming.service.shell;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.Schedule;
import com.peoplein.moiming.domain.enums.DomainRequestType;
import com.peoplein.moiming.domain.enums.SessionCategoryType;
import com.peoplein.moiming.domain.fixed.SessionCategory;
import com.peoplein.moiming.domain.session.MemberSessionLinker;
import com.peoplein.moiming.domain.session.MoimSession;
import com.peoplein.moiming.domain.session.SessionCategoryItem;
import com.peoplein.moiming.model.dto.SessionCategoryDetailsDto;
import com.peoplein.moiming.model.dto.domain.*;
import com.peoplein.moiming.model.dto.request_b.MoimSessionRequestDto;
import com.peoplein.moiming.model.dto.response_b.MoimSessionResponseDto;
import com.peoplein.moiming.repository.*;
import com.peoplein.moiming.service.input.MoimSessionServiceInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MoimSessionServiceShell {

    private final MemberRepository memberRepository;
    private final MoimMemberRepository moimMemberRepository;
    private final MoimRepository moimRepository;
    private final ScheduleRepository scheduleRepository;
    private final MoimSessionRepository moimSessionRepository;
    private final SessionCategoryRepository sessionCategoryRepository;
    private final MemberSessionLinkerRepository memberSessionLinkerRepository;
    private final SessionCategoryItemRepository sessionCategoryItemRepository;
    private final MemberSessionCategoryLinkerRepository memberSessionCategoryLinkerRepository;


    public MoimSessionServiceInput createInputForNewMoimSesion(MoimSessionRequestDto moimSessionRequestDto) {

        Moim moim = moimRepository.findById(moimSessionRequestDto.getMoimSessionDto().getMoimId()).orElseThrow(() -> new RuntimeException("해당 모임이 존재하지 않습니다"));
        Schedule schedule = null;

        if (!Objects.isNull(moimSessionRequestDto.getMoimSessionDto().getScheduleId())) {
            schedule = scheduleRepository.findOptionalById(moimSessionRequestDto.getMoimSessionDto().getScheduleId()).orElseThrow(() -> new RuntimeException("해당 일정이 존재하지 않습니다"));
        }

        List<SessionCategory> sessionCategories = sessionCategoryRepository.findAllSessionCategories();

        List<Long> memberIds = moimSessionRequestDto.getMemberSessionLinkerDtos().stream().map(MemberSessionLinkerDto::getMemberId).collect(Collectors.toList());
        List<Member> sessionMembers = memberRepository.findMembersByIds(memberIds);

        return MoimSessionServiceInput.builder()
                .moimOfNewMoimSession(moim)
                .scheduleOfNewMoimSession(schedule)
                .allSessionCategories(sessionCategories)
                .allSessionMembers(sessionMembers)
                .build();
    }

    public Long saveMoimSession(MoimSession moimSession) {
        return moimSessionRepository.save(moimSession);
    }

    public List<MoimSession> getAllMoimSessions(Long moimId) {
        return moimSessionRepository.findAllByMoimId(moimId);
    }

    public MoimSession getMoimSession(Long moimSessionId) {
//        moimSessionId 로 우선 조회하되,
        return moimSessionRepository.findOptionalById(moimSessionId).orElseThrow(() -> new RuntimeException("해당 MoimSession 을 찾을 수 없습니다"));
    }

    public MoimSessionResponseDto buildAllResponseModel(MoimSession moimSession) {

        // MoimSession 정보를 기준으로 다 만들어낸다
        MoimSessionDto moimSessionDto = new MoimSessionDto(moimSession);

        // MoimSession 내 Schedule 정보로 ScheduleDto 를 만들어준다
        ScheduleDto scheduleDto = null;
        if (moimSession.getSchedule() != null) {
            // Schedule 은 Join 되어 있지 않으므로, get 하는 순간 쿼리가 발생한다
            scheduleDto = new ScheduleDto(moimSession.getSchedule());
        }

        Map<SessionCategoryType, List<SessionCategoryItem>> tempMap = new HashMap<>();

        // 돌면서 SessionCategoryType 인 애들에 맞춰서 분류한다
        moimSession.getSessionCategoryItems().forEach(sessionCategoryItem -> {

            if (!tempMap.containsKey(sessionCategoryItem.getSessionCategory().getCategoryType())) {
                tempMap.put(sessionCategoryItem.getSessionCategory().getCategoryType(), new ArrayList<>());
            }

            List<SessionCategoryItem> categoryItemList = tempMap.get(sessionCategoryItem.getSessionCategory().getCategoryType());
            categoryItemList.add(sessionCategoryItem);
            tempMap.put(sessionCategoryItem.getSessionCategory().getCategoryType(), categoryItemList);
        });

        List<SessionCategoryDetailsDto> sessionCategoryDetailsDtos = new ArrayList<>();

        // 위에서 넣을 수 있긴 한데 그냥 깔끔하게 하기 위해서 밖에서 한번 더 돌면서 세팅
        for (SessionCategoryType categoryType : tempMap.keySet()) {

            List<SessionCategoryItemDto> sessionCategoryItems = new ArrayList<>();

            tempMap.get(categoryType).forEach(sessionCategoryItem -> {
                sessionCategoryItems.add(new SessionCategoryItemDto(sessionCategoryItem));
            });

            SessionCategoryDetailsDto categoryDetailsDto = new SessionCategoryDetailsDto(
                    categoryType, sessionCategoryItems
            );
            sessionCategoryDetailsDtos.add(categoryDetailsDto);
        }

        ////////////// 회원들 Data 연결

        // MoimMember 들도 Batch 로 불러오고, 아래에서 매핑해서 가져가주기 위해서 Id 및 List 준비
        List<Long> memberIds = moimSession.getMemberSessionLinkers().stream()
                .map(memberSessionLinker -> memberSessionLinker.getMember().getId()).collect(Collectors.toList());

        List<MoimMember> sessionMembersMoimLinkers = moimMemberRepository.findByMoimIdAndMemberIds(moimSession.getMoim().getId(), memberIds);


        List<MemberSessionLinkerDto> memberSessionLinkerDtos = new ArrayList<>();

        moimSession.getMemberSessionLinkers().forEach(memberSessionLinker -> {

            // 이미 Member, MemberInfo 까지 영컨에 올라온 상태
            MoimMember thisMoimMember = sessionMembersMoimLinkers.stream().filter(
                    memberMoimLinker -> memberMoimLinker.getMember().getId().equals(memberSessionLinker.getMember().getId())
            ).findAny().orElseThrow(() -> new RuntimeException("가지고 온 MoimMember 정보 중 " + memberSessionLinker.getMember().getId() + "의 정보를 찾을 수 없습니다"));

            MoimMemberInfoDto moimMemberInfoDto = MoimMemberInfoDto.createMemberInfoDto(thisMoimMember);

            MemberSessionLinkerDto memberSessionLinkerDto = new MemberSessionLinkerDto(
                    memberSessionLinker.getMember().getId(), memberSessionLinker.getSingleCost()
                    , memberSessionLinker.getMemberSessionCategoryTypes()
                    , moimMemberInfoDto
                    , memberSessionLinker.getMemberSessionState(), memberSessionLinker.getCreatedAt(), memberSessionLinker.getUpdatedAt()
            );

            memberSessionLinkerDtos.add(memberSessionLinkerDto);
        });


        return new MoimSessionResponseDto(moimSessionDto, scheduleDto
                , sessionCategoryDetailsDtos
                , memberSessionLinkerDtos);
    }

    // 정산활동 관리는 오직 리더나 관리자
    // REQUEST TYPE 확인 불필요 (정산활동은 생성~삭제 권한 모두 일정)
    // 요구사항 변경 가능성으로 일단 유지
    public void checkAuthority(DomainRequestType requestType, Long moimId, Member curMember) {
        MoimMember mml = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), moimId).orElseThrow();
        if (hasAuthority(mml, MoimMemberRoleType.NORMAL)) {
            throw new RuntimeException("정산활동 관여 권한이 없는 유저입니다");
        }
    }

    private boolean hasAuthority(MoimMember mml, MoimMemberRoleType moimMemberRoleType) {
        return mml.getMemberRoleType().equals(moimMemberRoleType);
    }

    public void processDelete(MoimSession moimSession) {

        // 해당 MoimSesion 에 관련된 모든 MemberSessionLinker 객체들을 가져온다
        List<Long> mslIds = moimSession.getMemberSessionLinkers().stream().map(MemberSessionLinker::getId).collect(Collectors.toList());

        // 2. Session Category Item 을 모두 삭제
        sessionCategoryItemRepository.removeAll(moimSession.getId());

        // 3. Member Session Linker 를 모두 삭제
        memberSessionCategoryLinkerRepository.removeAll(mslIds); // MSL 들을 삭제하기 위해선 MemberSessionCategoryLinker 들을 모두 삭제해야 한다
        memberSessionLinkerRepository.removeAll(moimSession.getId());
        moimSessionRepository.remove(moimSession);

    }

    public MoimMember findMemberMoimLinker(Long memberId, Long moimId) {

        Optional<MoimMember> optionalMml = moimMemberRepository.findByMemberAndMoimId(memberId, moimId);
        return optionalMml.orElseThrow(() -> new RuntimeException("대상 유저는 해당 모임에 속하지 않습니다"));
    }

    public MemberSessionLinker findMoimSessionLinker(Long sessionId, Long memberId) {
        Optional<MemberSessionLinker> optionalMsl = memberSessionLinkerRepository.findOptionalByMemberAndSessionId(memberId, sessionId);
        return optionalMsl.orElseThrow(() -> new RuntimeException("대상 유저는 해당 정산활동에 속하지 않습니다"));
    }
}