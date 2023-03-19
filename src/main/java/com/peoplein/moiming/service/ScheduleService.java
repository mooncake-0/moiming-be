package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.enums.ScheduleMemberState;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import com.peoplein.moiming.model.dto.domain.ScheduleDto;
import com.peoplein.moiming.model.dto.domain.ScheduleMemberDto;
import com.peoplein.moiming.model.dto.request.ScheduleRequestDto;
import com.peoplein.moiming.model.dto.response.ScheduleResponseDto;
import com.peoplein.moiming.repository.MemberScheduleLinkerRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.ScheduleRepository;
import com.peoplein.moiming.repository.jpa.MemberMoimLinkerJpaRepository;
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

        Moim moim = moimRepository.findById(requestDto.getMoimId());

        Schedule schedule = Schedule.createSchedule(
                requestDto.getScheduleTitle(), requestDto.getScheduleLocation()
                , transferStringToLdt(requestDto.getScheduleDate())
                , requestDto.getMaxCount()
                , moim, curMember
        );

        scheduleRepository.save(schedule);

        ScheduleDto scheduleDto = new ScheduleDto(
                schedule.getId(), schedule.getScheduleTitle(), schedule.getScheduleLocation(), schedule.getScheduleDate(), schedule.getMaxCount()
                , schedule.isClosed(), schedule.getCreatedAt(), schedule.getCreatedUid(), schedule.getUpdatedAt(), schedule.getUpdatedUid()
        );

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

        // Null 체킹
        if (Objects.isNull(schedule)) {
            log.error("요청한 일정을 찾을 수 없는 경우");
            throw new RuntimeException("요청한 일정을 찾을 수 없는 경우");
        }

        // 변경할 권한 유저 체킹 - 생성자, 리더, 운영진 가능
        // 요청한 유저와 이 Schedule 의 Moim Id 확보 필요
        if (!curMember.getUid().equals(schedule.getCreatedUid())) { // 일정 생성자가 아니다
            MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findByMemberAndMoimId(curMember.getId(), schedule.getMoim().getId());
            if (!memberMoimLinker.getMoimRoleType().equals(MoimRoleType.LEADER) && !memberMoimLinker.getMoimRoleType().equals(MoimRoleType.MANAGER)) {
                // 권한 소유자도 아니다
                log.error("일정을 수정할 권한이 없는 경우 :: 일정 생성자, 모임장, 운영진이 아님");
                throw new RuntimeException("일정을 수정할 권한이 없는 경우 :: 일정 생성자, 모임장, 운영진이 아님");
            }
        }

        boolean isAnyUpdate = false;

        if (!scheduleRequestDto.getScheduleTitle().equals(schedule.getScheduleTitle())) {
            isAnyUpdate = true;
            schedule.changeScheduleTitle(scheduleRequestDto.getScheduleTitle());
        }

        if (!scheduleRequestDto.getScheduleLocation().equals(schedule.getScheduleLocation())) {
            isAnyUpdate = true;
            schedule.changeScheduleLocation(scheduleRequestDto.getScheduleLocation());
        }

        LocalDateTime scheduleDateLdt = transferStringToLdt(scheduleRequestDto.getScheduleDate());
        if (!scheduleDateLdt.equals(schedule.getScheduleDate())) {
            isAnyUpdate = true;
            schedule.changeScheduleDate(scheduleDateLdt);
        }

        if (scheduleRequestDto.getMaxCount() != schedule.getMaxCount()) {
            isAnyUpdate = true;
            schedule.setMaxCount(scheduleRequestDto.getMaxCount());
        }

        if (scheduleRequestDto.isFullNotice()) {
            // TODO:: 수정사항 전체 알림 설정(?)
        }

        if (isAnyUpdate) {

            schedule.setUpdatedAt(LocalDateTime.now());
            schedule.setUpdatedUid(curMember.getUid());

            return buildScheduleResponseDto(schedule);

        } else {
            // 수정요청이 들어왔으나 수정된 사항이 없음
            log.error("수정된 사항이 없는 경우");
            throw new RuntimeException("수정된 사항이 없는 경우");
        }
    }

    private ScheduleResponseDto buildScheduleResponseDto(Schedule schedule) {

        // 모든 ScheduleLinker 들과 MemberMoimLinker 들과 연계 필요
        // MSL 의 정보를 가져오기 위해 조회, 사실 N+1 이지만 Batch 설정으로 인해 한번에 들고와준다.
        List<MemberScheduleLinker> memberScheduleLinkers = schedule.getMemberScheduleLinkers();
        List<Long> memberIds = memberScheduleLinkers.stream().map(msl -> msl.getMember().getId())
                .collect(Collectors.toList());

        // memberId 들로 해당 MemberMoimLinker 들을 들고온다.
        List<MemberMoimLinker> memberMoimLinkers = memberMoimLinkerRepository.findByMoimIdAndMemberIds(schedule.getMoim().getId(), memberIds);
        List<ScheduleMemberDto> scheduleMemberDtos = new ArrayList<>();

        // MEMO :: N^2 발생지점
        memberScheduleLinkers.forEach(msl -> {
                    MemberMoimLinker sameMemberLinker = null;
                    for (MemberMoimLinker mml : memberMoimLinkers) {
                        if (msl.getMember().getId().equals(mml.getMember().getId())) {
                            sameMemberLinker = mml;
                        }
                    }
                    if (Objects.isNull(sameMemberLinker)) {
                        throw new RuntimeException("");
                    }
                    MoimMemberInfoDto moimMemberInfoDto = new MoimMemberInfoDto(
                            sameMemberLinker.getMember().getId(), sameMemberLinker.getMember().getUid()
                            , sameMemberLinker.getMember().getMemberInfo().getMemberName()
                            , sameMemberLinker.getMember().getMemberInfo().getMemberEmail()
                            , sameMemberLinker.getMember().getMemberInfo().getMemberGender()
                            , sameMemberLinker.getMember().getMemberInfo().getMemberPfImg()
                            , sameMemberLinker.getMoimRoleType(), sameMemberLinker.getMemberState()
                            , sameMemberLinker.getCreatedAt(), sameMemberLinker.getUpdatedAt()
                    );

                    ScheduleMemberDto scheduleMemberDto = new ScheduleMemberDto(
                            msl.getMemberState(), msl.getCreatedAt(), msl.getUpdatedAt());

                    scheduleMemberDto.setMoimMemberInfoDto(moimMemberInfoDto);
                    scheduleMemberDtos.add(scheduleMemberDto);
                }
        );
        ScheduleResponseDto scheduleResponseDto = new ScheduleResponseDto(new ScheduleDto(
                schedule.getId(), schedule.getScheduleTitle(), schedule.getScheduleLocation(), schedule.getScheduleDate(), schedule.getMaxCount(), schedule.isClosed()
                , schedule.getCreatedAt(), schedule.getCreatedUid(), schedule.getUpdatedAt(), schedule.getUpdatedUid()
        ));

        scheduleResponseDto.setScheduleMemberDto(scheduleMemberDtos);

        return scheduleResponseDto;
    }

    public void deleteSchedule(Long scheduleId, Member curMember) {

        Schedule schedule = scheduleRepository.findById(scheduleId);

        // Null 체킹
        if (Objects.isNull(schedule)) {
            log.error("요청한 일정을 찾을 수 없는 경우");
            throw new RuntimeException("요청한 일정을 찾을 수 없는 경우");
        }

        // 일정 삭제 권한은 생성자 / 리더 / 운영진 에게 있다
        if (!curMember.getUid().equals(schedule.getCreatedUid())) { // 일정 생성자가 아니다
            // 아닐 경우 어떤 멤버인지 판독 필요
            MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findByMemberAndMoimId(curMember.getId(), schedule.getMoim().getId());
            if (!memberMoimLinker.getMoimRoleType().equals(MoimRoleType.LEADER) && !memberMoimLinker.getMoimRoleType().equals(MoimRoleType.MANAGER)) {
                // 권한 소유자도 아니다
                log.error("일정을 삭제할 권한이 없는 경우 :: 일정 생성자, 모임장, 운영진이 아님");
                throw new RuntimeException("일정을 삭제할 권한이 없는 경우 :: 일정 생성자, 모임장, 운영진이 아님");
            }
        }

        // 권한이 있으므로, 삭제를 진행한다
        // 모든 MemberScheduleLinker 를 우선 삭제한다
        try {

            memberScheduleLinkerRepository.removeAllByScheduleId(scheduleId);
            scheduleRepository.remove(schedule);

        } catch (RuntimeException exception) {
            log.error("삭제중 Error 발생: {}", exception.getMessage());
            throw new RuntimeException("삭제중 Error 발생: " + exception.getMessage());
        }
    }

    public ScheduleMemberDto changeMemberState(Long scheduleId, boolean isJoin, Member curMember) {

        Schedule schedule = scheduleRepository.findById(scheduleId);

        if (Objects.isNull(schedule)) {
            log.error("잘못된 요청 : 해당 PK의 일정이 존재하지 않습니다");
            throw new RuntimeException("잘못된 요청 : 해당 PK의 일정이 존재하지 않습니다");
        }

        MemberMoimLinker curMemberMoimLinker = memberMoimLinkerRepository.findWithMemberInfoByMemberAndMoimId(curMember.getId(), schedule.getMoim().getId());

        if (Objects.isNull(curMemberMoimLinker)) {
            log.error("잘못된 요청 : 모임원이 아닙니다");
            throw new RuntimeException("잘못된 요청 : 모임원이 아닙니다");
        }

        MoimMemberInfoDto moimMemberInfoDto = new MoimMemberInfoDto(
                curMemberMoimLinker.getMember().getId(), curMemberMoimLinker.getMember().getUid()
                , curMemberMoimLinker.getMember().getMemberInfo().getMemberName()
                , curMemberMoimLinker.getMember().getMemberInfo().getMemberEmail()
                , curMemberMoimLinker.getMember().getMemberInfo().getMemberGender()
                , curMemberMoimLinker.getMember().getMemberInfo().getMemberPfImg()
                , curMemberMoimLinker.getMoimRoleType(), curMemberMoimLinker.getMemberState()
                , curMemberMoimLinker.getCreatedAt(), curMemberMoimLinker.getUpdatedAt()
        );


        // 해당 멤버에 대한 ScheduleLinker 가 있는지 우선 조회
        MemberScheduleLinker memberScheduleLinker = memberScheduleLinkerRepository.findWithScheduleByMemberAndScheduleId(curMember.getId(), scheduleId);

        if (Objects.isNull(memberScheduleLinker)) {

            MemberScheduleLinker curMemberScheduleLinker = null;

            // 새로 생성후 저장한다
            if (isJoin) {
                curMemberScheduleLinker = MemberScheduleLinker.memberJoinSchedule(curMember, schedule, ScheduleMemberState.ATTEND);
            } else {
                curMemberScheduleLinker = MemberScheduleLinker.memberJoinSchedule(curMember, schedule, ScheduleMemberState.NONATTEND);
            }

            ScheduleMemberDto scheduleMemberDto = new ScheduleMemberDto(
                    curMemberScheduleLinker.getMemberState(), curMemberScheduleLinker.getCreatedAt(), curMemberScheduleLinker.getUpdatedAt()
            );

            scheduleMemberDto.setMoimMemberInfoDto(moimMemberInfoDto);

            return scheduleMemberDto;

        } else {
            // 해당 scheduleLinker 의 상태를 변경한다
            if (isJoin) {
                if (memberScheduleLinker.getMemberState() != ScheduleMemberState.ATTEND) {
                    memberScheduleLinker.changeMemberState(ScheduleMemberState.ATTEND);
                    memberScheduleLinker.setUpdatedAt(LocalDateTime.now());
                }
            } else {
                if (memberScheduleLinker.getMemberState() != ScheduleMemberState.NONATTEND) {
                    memberScheduleLinker.changeMemberState(ScheduleMemberState.NONATTEND);
                    memberScheduleLinker.setUpdatedAt(LocalDateTime.now());
                }
            }

            ScheduleMemberDto scheduleMemberDto = new ScheduleMemberDto(
                    memberScheduleLinker.getMemberState(), memberScheduleLinker.getCreatedAt(), memberScheduleLinker.getUpdatedAt()
            );

            scheduleMemberDto.setMoimMemberInfoDto(moimMemberInfoDto);

            return scheduleMemberDto;
        }

    }

}
