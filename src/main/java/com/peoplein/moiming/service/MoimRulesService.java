package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.rules.MoimRule;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.domain.rules.RulePersist;
import com.peoplein.moiming.model.dto.domain.RuleJoinDto;
import com.peoplein.moiming.model.dto.domain.RulePersistDto;
import com.peoplein.moiming.model.dto.request.RuleRequestDto;
import com.peoplein.moiming.repository.MemberMoimLinkerRepository;
import com.peoplein.moiming.repository.MoimRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MoimRulesService {

    private final MemberMoimLinkerRepository memberMoimLinkerRepository;
    private final MoimRuleRepository moimRuleRepository;

    /*
     요청을 보낸 유저와 모임의 관계를 확인한다
     소속된 유저임을 확인하며, 모임 내 권한이 있는지 확인한다
     */
    public MemberMoimLinker fetchAndCheckMoimRole(Long memberId, Long moimId) {

        MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findWithMoimByMemberAndMoimId(memberId, moimId);

        if (Objects.isNull(memberMoimLinker) || !memberMoimLinker.getMemberState().equals(MoimMemberState.ACTIVE)) {
            // ERROR:: 권한 없는 모임에 대한 진입
            log.error("속하지 않은 모임");
            throw new RuntimeException("속하지 않은 모임");
        }

        if (memberMoimLinker.getMoimRoleType().equals(MoimRoleType.NORMAL)) {
            log.error("조건을 변경할 권한이 없는 모임");
            throw new RuntimeException("조건을 변경할 권한이 없는 모임");
        }

        return memberMoimLinker;
    }

    public RuleJoinDto createJoinRule(RuleRequestDto ruleRequestDto, Member curMember) {

        MemberMoimLinker memberMoimLinker = fetchAndCheckMoimRole(curMember.getId(), ruleRequestDto.getMoimId());

        if (!memberMoimLinker.getMoim().isHasRuleJoin()) {

            RuleJoinDto ruleJoinDto = ruleRequestDto.getRuleJoinDto();

            if (Objects.isNull(ruleJoinDto)) {
                // ERROR :: ruleJoin 이 오지 않음
                log.error("잘못된 요청, ruleJoinDto 가 없습니다");
                throw new RuntimeException("잘못된 요청, ruleJoinDto 가 없습니다");
            }

            MoimRule ruleJoin = new RuleJoin( // MOIM 측 CASCADE 로 자동으로 등록이 된다
                    ruleJoinDto.getBirthMax(), ruleJoinDto.getBirthMin(), ruleJoinDto.getGender(), ruleJoinDto.getMoimMaxCount(),
                    ruleJoinDto.isDupLeaderAvailable(), ruleJoinDto.isDupManagerAvailable(), memberMoimLinker.getMoim(), curMember.getUid()
            );

            // 생성된 ruleJoin 으로 Dto 응답 재형성
            return new RuleJoinDto((RuleJoin) ruleJoin);

        } else {
            // ERROR :: 이미 Rule Join 이 있음 (잘못된 요청)
            log.error("이미 가입조건이 있는 모임");
            throw new RuntimeException("이미 가입조건이 있는 모임");
        }
    }

    public RulePersistDto createPersistRule(RuleRequestDto ruleRequestDto, Member curMember) {

        MemberMoimLinker memberMoimLinker = fetchAndCheckMoimRole(curMember.getId(), ruleRequestDto.getMoimId());

        if (!memberMoimLinker.getMoim().isHasRulePersist()) {

            RulePersistDto rulePersistDto = ruleRequestDto.getRulePersistDto();

            if (Objects.isNull(rulePersistDto)) {
                // ERROR :: ruleJoin 이 오지 않음
                log.error("잘못된 요청, rulePersistDto 가 없습니다");
                throw new RuntimeException("잘못된 요청, rulePersistDto 가 없습니다");
            }

            MoimRule rulePersist = new RulePersist( // MOIM 측 CASCADE 로 자동으로 등록이 된다
                    rulePersistDto.isDoGreeting(), rulePersistDto.getAttendMonthly(), rulePersistDto.getAttendCount()
                    , memberMoimLinker.getMoim(), curMember.getUid()
            );

            // 생성된 ruleJoin 으로 Dto 응답 재형성
            return new RulePersistDto((RulePersist) rulePersist);

        } else {
            // ERROR :: 이미 Rule Persist 이 있음 (잘못된 요청)
            log.error("이미 유지조건이 있는 모임");
            throw new RuntimeException("이미 유지조건이 있는 모임");
        }
    }

    public RuleJoinDto getJoinRule(Long moimId, Member curMember) {

        RuleJoin ruleJoin = (RuleJoin) moimRuleRepository.findByMoimAndType(moimId, "J");

        if (!Objects.isNull(ruleJoin)) {
            return new RuleJoinDto(ruleJoin);
        } else {
            // ERROR ::
            log.error("가입조건이 없는 모임입니다");
            throw new RuntimeException("가입조건이 없는 모임입니다");
        }
    }

    public RulePersistDto getPersistRule(Long moimId, Member curMember) {

        RulePersist rulePersist = (RulePersist) moimRuleRepository.findByMoimAndType(moimId, "P");

        if (!Objects.isNull(rulePersist)) {
            return new RulePersistDto(rulePersist);
        } else {
            // ERROR ::
            log.error("유지조건이 없는 모임입니다");
            throw new RuntimeException("유지조건이 없는 모임입니다");
        }
    }

    public RuleJoinDto changeJoinRule(RuleRequestDto ruleRequestDto, Member curMember) {

        MemberMoimLinker memberMoimLinker = fetchAndCheckMoimRole(curMember.getId(), ruleRequestDto.getMoimId());

        if (memberMoimLinker.getMoim().isHasRuleJoin()) {

            RuleJoin ruleJoin = (RuleJoin) moimRuleRepository.findByMoimAndType(ruleRequestDto.getMoimId(), "J"); // 조회로 인한 영속화
            RuleJoinDto updater = ruleRequestDto.getRuleJoinDto();

            if (Objects.isNull(ruleJoin)) {
                log.error("데이터 정합성 불일치 :: hasRuleJoin - true, but RuleJoin Not Found");
                throw new RuntimeException("데이터 정합성 불일치 :: hasRuleJoin - true, but RuleJoin Not Found");
            }

            if (Objects.isNull(updater)) {
                log.error("잘못된 요청, ruleJoinDto 가 없습니다");
                throw new RuntimeException("잘못된 요청, ruleJoinDto 가 없습니다");
            }

            boolean isAnyUpdated = false;

            if (updater.getBirthMax() != ruleJoin.getBirthMax()) {
                isAnyUpdated = true;
                ruleJoin.setBirthMax(updater.getBirthMax());
            }

            if (updater.getBirthMin() != ruleJoin.getBirthMin()) {
                isAnyUpdated = true;
                ruleJoin.setBirthMin(updater.getBirthMin());
            }

            if (updater.getGender() != ruleJoin.getGender()) {
                isAnyUpdated = true;
                ruleJoin.setGender(updater.getGender());
            }

            if (updater.getMoimMaxCount() != ruleJoin.getMoimMaxCount()) {
                isAnyUpdated = true;
                ruleJoin.setMoimMaxCount(updater.getMoimMaxCount());
            }

            if (updater.isDupLeaderAvailable() != ruleJoin.isDupManagerAvailable()) {
                isAnyUpdated = true;
                ruleJoin.setDupLeaderAvailable(updater.isDupLeaderAvailable());
            }

            if (updater.isDupManagerAvailable() != ruleJoin.isDupManagerAvailable()) {
                isAnyUpdated = true;
                ruleJoin.setDupManagerAvailable(updater.isDupManagerAvailable());
            }

            if (isAnyUpdated) {

                ruleJoin.setUpdatedAt(LocalDateTime.now());
                ruleJoin.setUpdatedUid(curMember.getUid());

                return new RuleJoinDto(ruleJoin);

            } else {

                // 수정요청이 들어왔으나 수정된 사항이 없음
                log.error("수정된 사항이 없는 경우");
                throw new RuntimeException("수정된 사항이 없는 경우");
            }

        } else {
            // ERROR :: Rule Join 이 없음 (잘못된 요청)
            log.error("수정할 가입 조건이 없는 경우");
            throw new RuntimeException("수정할 가입 조건이 없는 경우");
        }

    }

    public RulePersistDto changePersistRule(RuleRequestDto ruleRequestDto, Member curMember) {

        MemberMoimLinker memberMoimLinker = fetchAndCheckMoimRole(curMember.getId(), ruleRequestDto.getMoimId());

        if (memberMoimLinker.getMoim().isHasRulePersist()) {

            RulePersist rulePersist = (RulePersist) moimRuleRepository.findByMoimAndType(ruleRequestDto.getMoimId(), "P"); // 조회로 인한 영속화
            RulePersistDto updater = ruleRequestDto.getRulePersistDto();

            if (Objects.isNull(rulePersist)) {
                log.error("데이터 정합성 불일치 :: hasRulePersist - true, but RulePersist Not Found");
                throw new RuntimeException("데이터 정합성 불일치 :: hasRulePersist - true, but RulePersist Not Found");
            }

            if (Objects.isNull(updater)) {
                log.error("잘못된 요청, rulePersistDto 가 없습니다");
                throw new RuntimeException("잘못된 요청, rulePersistDto 가 없습니다");
            }

            boolean isAnyUpdated = false;

            if (updater.isDoGreeting() != rulePersist.isDoGreeting()) {
                isAnyUpdated = true;
                rulePersist.setDoGreeting(updater.isDoGreeting());
            }

            if (updater.getAttendMonthly() != rulePersist.getAttendMonthly()) {
                isAnyUpdated = true;
                rulePersist.setAttendMonthly(updater.getAttendMonthly());
            }

            if (updater.getAttendCount() != rulePersist.getAttendCount()) {
                isAnyUpdated = true;
                rulePersist.setAttendCount(updater.getAttendCount());
            }

            if (isAnyUpdated) {

                rulePersist.setUpdatedAt(LocalDateTime.now());
                rulePersist.setUpdatedUid(curMember.getUid());

                return new RulePersistDto(rulePersist);

            } else {

                // 수정요청이 들어왔으나 수정된 사항이 없음
                log.error("수정된 사항이 없는 경우");
                throw new RuntimeException("수정된 사항이 없는 경우");
            }

        } else {
            // ERROR :: Rule Persist 가 없음 (잘못된 요청)
            log.error("수정할 유지 조건이 없는 경우");
            throw new RuntimeException("수정할 유지 조건이 없는 경우");
        }
    }

    public void deleteJoinRule(Long moimId, Member curMember) {

        MemberMoimLinker memberMoimLinker = fetchAndCheckMoimRole(curMember.getId(), moimId);

        if (memberMoimLinker.getMoim().isHasRuleJoin()) {

            RuleJoin ruleJoin = (RuleJoin) moimRuleRepository.findByMoimAndType(moimId, "J"); // 조회로 인한 영속화

            if (Objects.isNull(ruleJoin)) {
                log.error("데이터 정합성 불일치 :: hasRuleJoin - true, but RuleJoin Not Found");
                throw new RuntimeException("데이터 정합성 불일치 :: hasRuleJoin - true, but RuleJoin Not Found");
            }

            memberMoimLinker.getMoim().removeRuleJoin();
            moimRuleRepository.remove(ruleJoin);

        } else {
            // ERROR :: Rule Persist 가 없음 (잘못된 요청)
            log.error("삭제할 가입 조건이 없는 경우");
            throw new RuntimeException("삭제할 가입 조건이 없는 경우");
        }
    }

    public void deletePersistRule(Long moimId, Member curMember) {

        MemberMoimLinker memberMoimLinker = fetchAndCheckMoimRole(curMember.getId(), moimId);

        if (memberMoimLinker.getMoim().isHasRulePersist()) {

            RulePersist rulePersist = (RulePersist) moimRuleRepository.findByMoimAndType(moimId, "P"); // 조회로 인한 영속화

            if (Objects.isNull(rulePersist)) {
                log.error("데이터 정합성 불일치 :: hasRulePersist - true, but RulePersist Not Found");
                throw new RuntimeException("데이터 정합성 불일치 :: hasRulePersist - true, but RulePersist Not Found");
            }

            memberMoimLinker.getMoim().removeRulePersist();
            moimRuleRepository.remove(rulePersist);

        } else {
            // ERROR :: Rule Persist 가 없음 (잘못된 요청)
            log.error("삭제할 유지 조건이 없는 경우");
            throw new RuntimeException("삭제할 유지 조건이 없는 경우");
        }
    }

}