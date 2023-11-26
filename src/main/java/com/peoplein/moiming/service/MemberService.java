package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.repository.*;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class MemberService {

    private final MoimMemberRepository moimMemberRepository;
    private final MoimPostRepository moimPostRepository;
    private final MemberScheduleLinkerRepository memberScheduleLinkerRepository;
    private final MoimRepository moimRepository;
    private final ScheduleRepository scheduleRepository;

    public MemberService(MoimMemberRepository moimMemberRepository,
                         MoimPostRepository moimPostRepository,
                         MemberScheduleLinkerRepository memberScheduleLinkerRepository,
                         MoimRepository moimRepository,
                         ScheduleRepository scheduleRepository) {
        this.moimMemberRepository = moimMemberRepository;
        this.moimPostRepository = moimPostRepository;
        this.memberScheduleLinkerRepository = memberScheduleLinkerRepository;
        this.moimRepository = moimRepository;
        this.scheduleRepository = scheduleRepository;
    }


    // TODO : MemberSchdule은 Attended 일 때만 가져오는지?
    // TODO : 회비 납부 일정 업데이트 필요.

    /**
     * 모임 리스트 : 갱신 시마다 랜덤으로 노출 → 여러 개 전달해야 함.
     * 모임 카드 : 회원수, 가장 빠른 정모일정 및 정모명 노출.
     * 전달받은 객체를 Controller에서 Front가 원하는 형태로 변경 필요함.
     */
    public MemberHomeDto serviceHome(Member curMember) {

        List<MoimPost> findNotices = getMoimNoticesLatestTop3(curMember);
        List<MemberScheduleLinker> findSchedules = memberScheduleLinkerRepository.findMemberScheduleLatest5ByMemberId(curMember.getId());

        List<Moim> allMoim = moimRepository.findAllMoim();
        List<Schedule> allSchedule = scheduleRepository.findAllSchedule();
        MoimScheduleDto moimScheduleDto = new MoimScheduleDto(allMoim, allSchedule);

        return new MemberHomeDto(findSchedules, findNotices, moimScheduleDto);
    }

    private List<MoimPost> getMoimNoticesLatestTop3(Member curMember) {
        List<MoimMember> findMoimMembers = moimMemberRepository.findByMemberId(curMember.getId());
        List<Long> uniqueMoimIds = getUniqueMoimIds(findMoimMembers);
        return null;
//        return moimPostRepository.findNoticesLatest3ByMoimIds(uniqueMoimIds);
    }

    private List<Long> getUniqueMoimIds(List<MoimMember> findMoimMembers) {
        return findMoimMembers.stream()
                .map(memberMoimLinker -> memberMoimLinker.getMoim().getId())
                .distinct()
                .collect(Collectors.toList());
    }

    // 추후 필요한 정보로 수정 필요함.
    // 정산 필드 추가 필요함. 필요한 경우 아래 필드도 수정해야함. (엔티티 그대로 노출하기 때문)
    @Getter
    public static final class MemberHomeDto {
        private final List<MemberScheduleLinker> memberSchedule;
        private final List<MoimPost> moimNotices;
        private final MoimScheduleDto moimScheduleDto;

        public MemberHomeDto(List<MemberScheduleLinker> memberSchedule,
                             List<MoimPost> moimNotices,
                             MoimScheduleDto moimScheduleDto) {
            this.memberSchedule = memberSchedule;
            this.moimNotices = moimNotices;
            this.moimScheduleDto = moimScheduleDto;
        }
    }

    @Getter
    public static final class MoimScheduleDto {

        private final Map<Moim, Schedule> moimScheduleMap = new HashMap<>();

        public MoimScheduleDto(List<Moim> moims, List<Schedule> schedules) {
            initKeyByExistedMoim(moims);
            updateScheduleByCreatedTime(schedules);
        }

        private void initKeyByExistedMoim(List<Moim> moims) {
            moims.forEach(moim -> moimScheduleMap.put(moim, null));
        }

        private void updateScheduleByCreatedTime(List<Schedule> schedules) {
            schedules.stream()
                    .filter(value -> !isNotExistMoim(value.getMoim()))
                    .forEach(value -> moimScheduleMap.put(value.getMoim(), decideValue(value)));
        }

        private boolean isNotExistMoim(Moim key) {
            return !moimScheduleMap.containsKey(key);
        }

        private boolean isFirstData(Moim key) {
            return moimScheduleMap.get(key) == null;
        }

        private Schedule decideValue(Schedule value) {
            Moim key = value.getMoim();
            return isFirstData(key) ? value : getLatestSchedule(value);
        }

        private Schedule getLatestSchedule(Schedule value) {
            Moim key = value.getMoim();
            Schedule oldValue = moimScheduleMap.get(key);
            return oldValue.getScheduleDate().isAfter(value.getScheduleDate()) ?
                    oldValue : value;
        }
    }
}
