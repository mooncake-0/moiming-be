package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.enums.ScheduleMemberState;
import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import com.peoplein.moiming.model.dto.domain.ScheduleDto;
import com.peoplein.moiming.model.dto.domain.ScheduleMemberDto;
import com.peoplein.moiming.model.dto.request.ScheduleRequestDto;
import com.peoplein.moiming.model.dto.response.ScheduleResponseDto;
import com.peoplein.moiming.repository.MemberScheduleLinkerRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.ScheduleRepository;
import com.peoplein.moiming.repository.jpa.MemberMoimLinkerJpaRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ScheduleService {

    private final MoimRepository moimRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberMoimLinkerJpaRepository memberMoimLinkerRepository;
    private final MemberScheduleLinkerRepository memberScheduleLinkerRepository;

    private LocalDateTime transferStringToLdt(String date) {
        // DATE FORMAT :: yyyyMMddHHmm
        DateTimeFormatter scheduleFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        return LocalDateTime.parse(date, scheduleFormat);
    }

    public ScheduleResponseDto createSchedule(ScheduleRequestDto requestDto, Member curMember) {

        // TODO :: 현재 모임원들에게 FCM Push 를 전송한다
        if (requestDto.isFullNotice()) {
        }
        Moim findMoim = moimRepository.findById(requestDto.getMoimId());

        Schedule newSchedule = createNewScheduleWithDto(requestDto, findMoim, curMember);
        scheduleRepository.save(newSchedule);

        ScheduleDto scheduleDto = ScheduleDto.createScheduleDto(newSchedule);
        return new ScheduleResponseDto(scheduleDto);
    }

    public List<ScheduleResponseDto> viewAllMoimSchedule(Long moimId, Member curMember) {

        // 우선 moim 의 모든 schedule 을 들고온다. 이 때, schedule 의 moim 정보도 join 하여 들고와서 각 객체의 컬렉션들이 batch 옵션 적용된채로 들고와질 수 있도록 한다
        List<Schedule> schedules = scheduleRepository.findByMoimId(moimId);

        // 각 Member 의 MemberInfo 를 Join 해주기 위해 추가 Query
        List<MemberMoimLinker> memberMoimLinkers = memberMoimLinkerRepository.findWithMemberInfoAndMoimByMoimId(moimId);

        // ScheduleMemberDto 에 set 될 MoimMemberInfo 를 미리 형성해둔다
        Map<Long, MoimMemberInfoDto> memberInfoMap = new HashMap<>();
        memberMoimLinkers.forEach(mml -> {
            memberInfoMap.put(mml.getMember().getId(), new MoimMemberInfoDto(
                    mml.getMember().getId(), mml.getMember().getUid(),
                    mml.getMember().getMemberInfo().getMemberName(), mml.getMember().getMemberInfo().getMemberEmail(),
                    mml.getMember().getMemberInfo().getMemberGender(), mml.getMember().getMemberInfo().getMemberPfImg(),
                    mml.getMoimRoleType(), mml.getMemberState(), mml.getCreatedAt(), mml.getUpdatedAt()
            ));
        });

        // 1차 - KEY : 각 Schedule_id, VAL : 2차 Map
        // 2차 - KEY : 각 Member_id. VAL : 각자의 Schedule 송신용 정보
        Map<Long, Map<Long, ScheduleMemberDto>> scheduleMemberInfoMap = new HashMap<>();

        // MEMO :: N^2 발생
        schedules.forEach(schedule -> {
            Map<Long, ScheduleMemberDto> eachScheduleMemberInfoMap = new HashMap<>();
            // MSL 의 정보를 가져오기 위해 조회, 사실 N+1 이지만 Batch 설정으로 인해 한번에 들고와준다.
            schedule.getMemberScheduleLinkers().forEach(sml -> {
                // 각 스케줄 참여 멤버의 정보를 형성후 2차 Map 에 저장해두되, 같은 스케줄에 대해 한 value map 으로 형성하여 저장한다
                Long thisMemberId = sml.getMember().getId();
                eachScheduleMemberInfoMap.put(thisMemberId, new ScheduleMemberDto(
                        sml.getMemberState(), sml.getCreatedAt(), sml.getUpdatedAt()));
                if (memberInfoMap.containsKey(thisMemberId)) { // 위에서 형성해 놨던 정보를 동일한 Member 에 할당한다
                    eachScheduleMemberInfoMap.get(thisMemberId).setMoimMemberInfoDto(memberInfoMap.get(thisMemberId));
                }
            });
            // 각 schedule 에 대한 멤버들 Dto 형성후 자료구조에 넣는다
            scheduleMemberInfoMap.put(schedule.getId(), eachScheduleMemberInfoMap);
        });

        List<ScheduleResponseDto> responseData = new ArrayList<>();

        // schedule 을 돌리면서 responseData 를 채운다
        schedules.forEach(schedule -> {

            ScheduleDto scheduleDto = new ScheduleDto(
                    schedule.getId(), schedule.getScheduleTitle(), schedule.getScheduleLocation(), schedule.getScheduleDate()
                    , schedule.getMaxCount(), schedule.isClosed(), schedule.getCreatedAt(), schedule.getCreatedUid(), schedule.getUpdatedAt(), schedule.getUpdatedUid()
            );

            List<ScheduleMemberDto> scheduleMemberDto = new ArrayList<>(scheduleMemberInfoMap.get(schedule.getId()).values());

            ScheduleResponseDto scheduleResponseDto = new ScheduleResponseDto(scheduleDto);
            scheduleResponseDto.setScheduleMemberDto(scheduleMemberDto);

            responseData.add(scheduleResponseDto);
        });

        return responseData;
    }

    public ScheduleResponseDto updateSchedule(ScheduleRequestDto scheduleRequestDto, Member curMember) {

        Schedule schedule = scheduleRepository.findWithMoimById(scheduleRequestDto.getScheduleId());

        // When repository does not have such schedule in DB.
        String errorMessage = "요청한 일정을 찾을 수 없는 경우";
        throwIfObjectIsNull(schedule, errorMessage);

        String authorityFailMessage = "일정을 수정할 권한이 없는 경우 :: 일정 생성자, 모임장, 운영진이 아님";
        checkAuthority(curMember, schedule, authorityFailMessage);

        boolean isAnyUpdated = updateSchedule(scheduleRequestDto, schedule, curMember.getUid());

        if (isAnyUpdated) {
            return buildScheduleResponseDto(schedule);
        } else {
            log.error("수정된 사항이 없는 경우");
            throw new RuntimeException("수정된 사항이 없는 경우");
        }
    }

    private ScheduleResponseDto buildScheduleResponseDto(Schedule schedule) {

        // 모든 ScheduleLinker 들과 MemberMoimLinker 들과 연계 필요
        // MSL 의 정보를 가져오기 위해 조회, 사실 N+1 이지만 Batch 설정으로 인해 한번에 들고와준다.
        List<MemberScheduleLinker> memberScheduleLinkers = schedule.getMemberScheduleLinkers();
        List<Long> memberIds = memberScheduleLinkers.stream()
                .map(msl -> msl.getMember().getId())
                .distinct()
                .collect(Collectors.toList());

        // memberId 들로 해당 MemberMoimLinker들을 들고온다.
        List<MemberMoimLinker> memberMoimLinkers = memberMoimLinkerRepository.findByMoimIdAndMemberIds(schedule.getMoim().getId(), memberIds);

        // Moim에서 Member / Shedule 정보가 있는 녀석들을 찾아서 ScheduleMemberDto를 만듦.
        List<ScheduleMemberDto> scheduleMemberDtos = getDtosIfMoimHasScheduleAndMember(memberScheduleLinkers, memberMoimLinkers);

        ScheduleDto scheduleDto = ScheduleDto.createScheduleDto(schedule);
        return ScheduleResponseDto.create(scheduleDto, scheduleMemberDtos);
    }

    public void deleteSchedule(Long scheduleId, Member curMember) {

        Schedule schedule = scheduleRepository.findById(scheduleId);

        String errorMessage = "요청한 일정을 찾을 수 없는 경우";
        throwIfObjectIsNull(schedule, errorMessage);

        String failMessage = "일정을 삭제할 권한이 없는 경우 :: 일정 생성자, 모임장, 운영진이 아님";
        checkAuthority(curMember, schedule, failMessage);

        // Error 발생 시, RuntimeException이 아닌 다른 에러가 발생함.
        // 현재 위치에서 RollBack Exception으로 Controller에 전달됨.
        memberScheduleLinkerRepository.removeAllByScheduleId(scheduleId);
        scheduleRepository.remove(schedule);
    }

    public ChangeMemberTuple changeMemberState(Long scheduleId, boolean isJoin, Member curMember) {

        Schedule schedule = scheduleRepository.findById(scheduleId);
        String errorMessageForSchedule = "잘못된 요청 : 해당 PK의 일정이 존재하지 않습니다";
        throwIfObjectIsNull(schedule, errorMessageForSchedule);

        MemberMoimLinker curMemberMoimLinker = memberMoimLinkerRepository.findWithMemberInfoByMemberAndMoimId(curMember.getId(), schedule.getMoim().getId());
        String errorMessageForLinker = "잘못된 요청 : 모임원이 아닙니다";
        throwIfObjectIsNull(curMemberMoimLinker, errorMessageForLinker);

        // 해당 멤버에 대한 ScheduleLinker 가 있는지 우선 조회
        MemberScheduleLinker memberScheduleLinker = memberScheduleLinkerRepository.findWithScheduleByMemberAndScheduleId(curMember.getId(), scheduleId);

        if (Objects.isNull(memberScheduleLinker)) {
            memberScheduleLinker =
                    isJoin ?
                    MemberScheduleLinker.memberJoinSchedule(curMember, schedule, ScheduleMemberState.ATTEND) :
                    MemberScheduleLinker.memberJoinSchedule(curMember, schedule, ScheduleMemberState.NONATTEND);
        } else {
            memberScheduleLinker.changeMemberStateWithJoin(isJoin);
        }

        MoimMemberInfoDto moimMemberInfoDto = MoimMemberInfoDto.createWithMemberMoimLinker(curMemberMoimLinker);
        return new ChangeMemberTuple(memberScheduleLinker, moimMemberInfoDto);
    }

    private boolean updateSchedule(ScheduleRequestDto scheduleRequestDto, Schedule schedule, String updaterUid) {
        // 시간 변환
        LocalDateTime scheduleDateLdt = transferStringToLdt(scheduleRequestDto.getScheduleDate());

        boolean isAnyUpdate = schedule.hasAnyUpdate(
                scheduleRequestDto.getScheduleTitle(),
                scheduleRequestDto.getScheduleLocation(),
                scheduleDateLdt,
                scheduleRequestDto.getMaxCount());

        schedule.changeScheduleTitle(scheduleRequestDto.getScheduleTitle());
        schedule.changeScheduleLocation(scheduleRequestDto.getScheduleLocation());
        schedule.changeScheduleDate(scheduleDateLdt);
        schedule.setMaxCount(scheduleRequestDto.getMaxCount());

        if (isAnyUpdate)
            schedule.setUpdatedUid(updaterUid);

        /*if (scheduleRequestDto.isFullNotice()) {
            // TODO:: 수정사항 전체 알림 설정(?)
            // Not Supported Operation.
        }*/
        return isAnyUpdate;
    }

    private List<ScheduleMemberDto> getDtosIfMoimHasScheduleAndMember(
            List<MemberScheduleLinker> memberScheduleLinkers,
            List<MemberMoimLinker> memberMoimLinkers) {

        return memberScheduleLinkers.stream()
                .filter(memberScheduleLinker -> getMemberMoimLinkerCorrspondScheduleLinker(memberMoimLinkers, memberScheduleLinker).isPresent())
                .map(memberScheduleLinker ->
                        new Tuple(memberScheduleLinker, getMemberMoimLinkerCorrspondScheduleLinker(memberMoimLinkers, memberScheduleLinker).orElseThrow()))
                .map(Tuple::asScheduleMemberDto)
                .collect(Collectors.toList());
    }

    private Optional<MemberMoimLinker> getMemberMoimLinkerCorrspondScheduleLinker(List<MemberMoimLinker> memberMoimLinkers, MemberScheduleLinker msl) {
        return memberMoimLinkers.stream()
                .filter(memberMoimLinker -> msl.getMember().getId().equals(memberMoimLinker.getMember().getId()))
                .findFirst();
    }


    @Getter
    private static final class Tuple {
        private final MemberScheduleLinker memberScheduleLinker;
        private final MoimMemberInfoDto moimMemberInfoDto;

        public Tuple(MemberScheduleLinker memberScheduleLinker, MemberMoimLinker memberMoimLinker) {
            this.memberScheduleLinker = memberScheduleLinker;
            this.moimMemberInfoDto = MoimMemberInfoDto.createWithMemberMoimLinker(memberMoimLinker);
        }

        public ScheduleMemberDto asScheduleMemberDto() {
            return ScheduleMemberDto.create(this.memberScheduleLinker, this.moimMemberInfoDto);
        }
    }

    private Schedule createNewScheduleWithDto(ScheduleRequestDto requestDto, Moim moim, Member curMember) {
        return Schedule.createSchedule(
                requestDto.getScheduleTitle(),
                requestDto.getScheduleLocation(),
                transferStringToLdt(requestDto.getScheduleDate()),
                requestDto.getMaxCount(),
                moim,
                curMember);
    }

    // 변경할 권한 유저 체킹 - 생성자, 리더, 운영진 가능
    // 요청한 유저와 이 Schedule 의 Moim Id 확보 필요
    private void checkAuthority(Member curMember, Schedule schedule, String failMessage) {
        MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findByMemberAndMoimId(curMember.getId(), schedule.getMoim().getId());
        if (!hasPermissionForUpdateSchedule(curMember, schedule, memberMoimLinker)) { // 일정 생성자가 아니다
            log.error(failMessage);
            throw new RuntimeException(failMessage);
        }
    }

    private boolean hasPermissionForUpdateSchedule(Member curMember, Schedule schedule, MemberMoimLinker memberMoimLinker) {
        return curMember.isSameUid(schedule.getCreatedUid()) || memberMoimLinker.hasPermissionForUpdate();
    }

    private void throwIfObjectIsNull(Object object, String message) {
        if (Objects.isNull(object)) {
            log.error(message);
            throw new RuntimeException(message);
        }
    }

    @Getter
    public static class ChangeMemberTuple {

        private final MemberScheduleLinker memberScheduleLinker;
        private final MoimMemberInfoDto moimMemberInfoDto;

        public ChangeMemberTuple(MemberScheduleLinker memberScheduleLinker, MoimMemberInfoDto moimMemberInfoDto) {
            this.memberScheduleLinker = memberScheduleLinker;
            this.moimMemberInfoDto = moimMemberInfoDto;
        }
    }



}
