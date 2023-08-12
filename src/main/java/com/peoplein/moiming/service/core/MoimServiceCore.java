package com.peoplein.moiming.service.core;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.rules.MoimRule;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.model.dto.domain.MoimDto;
import com.peoplein.moiming.model.dto.domain.RuleJoinDto;
import com.peoplein.moiming.service.MoimService;
import com.peoplein.moiming.service.input.MoimServiceInput;
import com.peoplein.moiming.service.output.MoimServiceOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.naming.NoPermissionException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MoimServiceCore {

    private boolean isAnyUpdated;



    public MoimServiceOutput hasAnyUpdated(MoimServiceInput moimServiceInput) {

        Moim moim = moimServiceInput.getFindMoim();
        MoimDto moimDto = moimServiceInput.getInputMoimDto();
        MoimRoleType updatePermission = moimServiceInput.getUpdatePermission();

        // Validation
        if (Objects.isNull(moim)) {
            log.error("요청한 모임 혹은 모임관계를 찾을 수 없는 경우");
            throw new RuntimeException("요청한 모임 혹은 모임관계를 찾을 수 없는 경우");
        }

        if (updatePermission.equals(MoimRoleType.NORMAL)) {
            log.error("모임을 삭제할 권한이 없는 경우");
            throw new RuntimeException("모임을 삭제할 권한이 없는 경우");
        }

        boolean isAnyUpdated = moimDto.getMoimName().equals(moim.getMoimName())
                || moimDto.getMoimInfo().equals(moim.getMoimInfo())
                || moimDto.getArea().equals(moim.getMoimArea());

        if (!isAnyUpdated) {
            log.error("수정된 사항이 없는 경우");
            throw new RuntimeException("수정된 사항이 없는 경우");
        }

        return MoimServiceOutput.builder()
                .isAnyUpdated(isAnyUpdated)
                .build();
    }


    public MoimServiceOutput createMoim(MoimServiceInput moimServiceInput) {

        MoimDto moimDto = moimServiceInput.getInputMoimDto();
        List<Category> moimCategoriesForCreate = moimServiceInput.getMoimCategoriesForCreate();
        Member curMember = moimServiceInput.getCurMember();
        RuleJoinDto requestRuleJoinDto = moimServiceInput.getRequestRuleJoinDto();

        Moim createdMoim = Moim.createMoim(moimDto.getMoimName()
                , moimDto.getMoimInfo()
                , moimDto.getMoimPfImg()
                , moimDto.getArea()
                , curMember.getId());

        MemberMoimLinker curMemberMoimLinker = MemberMoimLinker.memberJoinMoim(
                curMember, createdMoim, MoimRoleType.CREATOR, MoimMemberState.ACTIVE
        );

        if (Objects.nonNull(requestRuleJoinDto)) {
            MoimRule moimRule = createMoimRule(requestRuleJoinDto, createdMoim, curMember);
        }

        List<MoimCategoryLinker> moimCategoryLinkers = moimCategoriesForCreate.stream()
                .map(category -> new MoimCategoryLinker(createdMoim, category))
                .collect(Collectors.toList());

        return MoimServiceOutput.builder()
                .createdMoim(createdMoim)
                .createdMoimCategoryLinkers(moimCategoryLinkers)
                .createdMemberMoimLinker(curMemberMoimLinker)
                .build();
    }


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

}