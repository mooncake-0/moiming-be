//package com.peoplein.moiming.service;
//
//import com.peoplein.moiming.domain.member.Member;
//import com.peoplein.moiming.domain.moim.MoimMember;
//import com.peoplein.moiming.model.dto.request_b.RuleRequestDto;
//import com.peoplein.moiming.repository.MoimMemberRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import javax.transaction.Transactional;
//
//@Service
//@Transactional
//@Slf4j
//@RequiredArgsConstructor
//public class MoimRulesService {
//
//    private final MoimMemberRepository moimMemberRepository;
//
//    /*
//     요청을 보낸 유저와 모임의 관계를 확인한다
//     소속된 유저임을 확인하며, 모임 내 권한이 있는지 확인한다
//     */
//    public MoimMember fetchAndCheckMoimRole(Long memberId, Long moimId) {
//        return null;
////        MoimMember memberMoimLinker = moimMemberRepository.findWithMoimByMemberAndMoimId(memberId, moimId);
////
////        if (Objects.isNull(memberMoimLinker) || !memberMoimLinker.getMemberState().equals(MoimMemberState.ACTIVE)) {
////            // ERROR:: 권한 없는 모임에 대한 진입
////            log.error("속하지 않은 모임");
////            throw new RuntimeException("속하지 않은 모임");
////        }
////
////        if (memberMoimLinker.getMoimMemberRoleType().equals(MoimMemberRoleType.NORMAL)) {
////            log.error("조건을 변경할 권한이 없는 모임");
////            throw new RuntimeException("조건을 변경할 권한이 없는 모임");
////        }
////
////        return memberMoimLinker;
//    }
//
//    public Object createJoinRule(RuleRequestDto ruleRequestDto, Member curMember) {
//        return null;
////        MoimMember memberMoimLinker = fetchAndCheckMoimRole(curMember.getId(), ruleRequestDto.getMoimId());
////
////        if (!memberMoimLinker.getMoim().isHasRuleJoin()) {
////
////            RuleJoinDto ruleJoinDto = ruleRequestDto.getRuleJoinDto();
////
////            if (Objects.isNull(ruleJoinDto)) {
////                // ERROR :: ruleJoin 이 오지 않음
////                log.error("잘못된 요청, ruleJoinDto 가 없습니다");
////                throw new RuntimeException("잘못된 요청, ruleJoinDto 가 없습니다");
////            }
////
////            MoimRule ruleJoin = new RuleJoin( // MOIM 측 CASCADE 로 자동으로 등록이 된다
////                    ruleJoinDto.getBirthMax(), ruleJoinDto.getBirthMin(), ruleJoinDto.getGender(), ruleJoinDto.getMoimMaxCount(),
////                    ruleJoinDto.isDupLeaderAvailable(), ruleJoinDto.isDupManagerAvailable(), memberMoimLinker.getMoim(), curMember.getId()
////            );
////
////            // 생성된 ruleJoin 으로 Dto 응답 재형성
////            return new RuleJoinDto((RuleJoin) ruleJoin);
////
////        } else {
////            // ERROR :: 이미 Rule Join 이 있음 (잘못된 요청)
////            log.error("이미 가입조건이 있는 모임");
////            throw new RuntimeException("이미 가입조건이 있는 모임");
////        }
////
//    }
//
//
//    public Object getJoinRule(Long moimId, Member curMember) {
//        return null;
////        RuleJoin ruleJoin = (RuleJoin) moimRuleRepository.findByMoimAndType(moimId, "J");
////
////        if (!Objects.isNull(ruleJoin)) {
////            return new RuleJoinDto(ruleJoin);
////        } else {
////            // ERROR ::
////            log.error("가입조건이 없는 모임입니다");
////            throw new RuntimeException("가입조건이 없는 모임입니다");
////        }
//    }
//
//    public Object changeJoinRule(RuleRequestDto ruleRequestDto, Member curMember) {
//
////        MoimMember memberMoimLinker = fetchAndCheckMoimRole(curMember.getId(), ruleRequestDto.getMoimId());
////
////        if (memberMoimLinker.getMoim().isHasRuleJoin()) {
////
////            RuleJoin ruleJoin = (RuleJoin) moimRuleRepository.findByMoimAndType(ruleRequestDto.getMoimId(), "J"); // 조회로 인한 영속화
////            RuleJoinDto updater = ruleRequestDto.getRuleJoinDto();
////
////            if (Objects.isNull(ruleJoin)) {
////                log.error("데이터 정합성 불일치 :: hasRuleJoin - true, but RuleJoin Not Found");
////                throw new RuntimeException("데이터 정합성 불일치 :: hasRuleJoin - true, but RuleJoin Not Found");
////            }
////
////            if (Objects.isNull(updater)) {
////                log.error("잘못된 요청, ruleJoinDto 가 없습니다");
////                throw new RuntimeException("잘못된 요청, ruleJoinDto 가 없습니다");
////            }
////
////            boolean isAnyUpdated = false;
////
////            if (updater.getBirthMax() != ruleJoin.getBirthMax()) {
////                isAnyUpdated = true;
////                ruleJoin.setBirthMax(updater.getBirthMax());
////            }
////
////            if (updater.getBirthMin() != ruleJoin.getBirthMin()) {
////                isAnyUpdated = true;
////                ruleJoin.setBirthMin(updater.getBirthMin());
////            }
////
////            if (updater.getGender() != ruleJoin.getGender()) {
////                isAnyUpdated = true;
////                ruleJoin.setGender(updater.getGender());
////            }
////
////            if (updater.getMoimMaxCount() != ruleJoin.getMoimMaxCount()) {
////                isAnyUpdated = true;
////                ruleJoin.setMoimMaxCount(updater.getMoimMaxCount());
////            }
////
////            if (updater.isDupLeaderAvailable() != ruleJoin.isDupManagerAvailable()) {
////                isAnyUpdated = true;
////                ruleJoin.setDupLeaderAvailable(updater.isDupLeaderAvailable());
////            }
////
////            if (updater.isDupManagerAvailable() != ruleJoin.isDupManagerAvailable()) {
////                isAnyUpdated = true;
////                ruleJoin.setDupManagerAvailable(updater.isDupManagerAvailable());
////            }
////
////            if (isAnyUpdated) {
////
////                ruleJoin.setUpdatedAt(LocalDateTime.now());
////                ruleJoin.setUpdatedMemberId(curMember.getId());
////
////                return new RuleJoinDto(ruleJoin);
////
////            } else {
////
////                // 수정요청이 들어왔으나 수정된 사항이 없음
////                log.error("수정된 사항이 없는 경우");
////                throw new RuntimeException("수정된 사항이 없는 경우");
////            }
////
////        } else {
////            // ERROR :: Rule Join 이 없음 (잘못된 요청)
////            log.error("수정할 가입 조건이 없는 경우");
////            throw new RuntimeException("수정할 가입 조건이 없는 경우");
////        }
//        return null;
//    }
//
//
//
//    public void deleteJoinRule(Long moimId, Member curMember) {
////        MoimMember memberMoimLinker = fetchAndCheckMoimRole(curMember.getId(), moimId);
////
////        if (memberMoimLinker.getMoim().isHasRuleJoin()) {
////
////            RuleJoin ruleJoin = (RuleJoin) moimRuleRepository.findByMoimAndType(moimId, "J"); // 조회로 인한 영속화
////
////            if (Objects.isNull(ruleJoin)) {
////                log.error("데이터 정합성 불일치 :: hasRuleJoin - true, but RuleJoin Not Found");
////                throw new RuntimeException("데이터 정합성 불일치 :: hasRuleJoin - true, but RuleJoin Not Found");
////            }
////
////            memberMoimLinker.getMoim().removeRuleJoin();
////            moimRuleRepository.remove(ruleJoin);
////
////        } else {
////            // ERROR :: Rule Persist 가 없음 (잘못된 요청)
////            log.error("삭제할 가입 조건이 없는 경우");
////            throw new RuntimeException("삭제할 가입 조건이 없는 경우");
////        }
//    }
//
//}