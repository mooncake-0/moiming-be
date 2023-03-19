package com.peoplein.moiming.service.shell;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.model.dto.domain.MoimDto;
import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import com.peoplein.moiming.model.dto.domain.MoimMembersDto;
import com.peoplein.moiming.model.dto.domain.MyMoimLinkerDto;
import com.peoplein.moiming.model.dto.request.MoimRequestDto;
import com.peoplein.moiming.model.dto.response.MoimResponseDto;
import com.peoplein.moiming.repository.CategoryRepository;
import com.peoplein.moiming.repository.MemberMoimLinkerRepository;
import com.peoplein.moiming.repository.MoimCategoryLinkerRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.service.input.MoimServiceInput;
import com.peoplein.moiming.service.output.MoimServiceOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MoimServiceShell {


    private final MoimRepository moimRepository;
    private final MemberMoimLinkerRepository memberMoimLinkerRepository;
    private final MoimCategoryLinkerRepository moimCategoryLinkerRepository;
    private final CategoryRepository categoryRepository;


    public Moim getMoim(Long moimId, Member curMember) {
        return moimRepository.findOptionalById(moimId).orElseThrow(() -> new RuntimeException("요청한 모임을 찾을 수 없는 경우"));
    }

    public MoimServiceInput getInputForUpdate(
            MoimRequestDto moimRequestDto, Member curMember) {

        MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findWithMoimByMemberAndMoimId(curMember.getId(), moimRequestDto.getMoimDto().getMoimId());
        Moim moim = memberMoimLinker.getMoim();

        return MoimServiceInput.builder()
                .inputMoimDto(moimRequestDto.getMoimDto())
                .findMoim(moim)
                .updatePermission(memberMoimLinker.getMoimRoleType())
                .curMember(curMember)
                .build();
    }

    public MoimResponseDto updateMoim(MoimServiceOutput moimServiceOutput, MoimServiceInput moimServiceInput) {

        if (!moimServiceOutput.isAnyUpdated()) {
            return null;
        }

        MoimDto inputMoimDto = moimServiceInput.getInputMoimDto();
        Moim findMoim = moimServiceInput.getFindMoim();
        Member curMember = moimServiceInput.getCurMember();


        findMoim.changeMoimName(inputMoimDto.getMoimName());
        findMoim.setMoimInfo(inputMoimDto.getMoimInfo());
        findMoim.changeMoimArea(new Area(inputMoimDto.getArea().getState(), inputMoimDto.getArea().getCity()));

        findMoim.changeUpdatedUid(curMember.getUid());
        findMoim.setUpdatedAt(LocalDateTime.now());

        return createMoimResponseDto(findMoim, curMember);
    }

    public MoimServiceInput readyForCreateMoim(MoimRequestDto requestDto, Member curMember) {

        MoimDto moimDto = requestDto.getMoimDto();
        List<Category> moimCategories = categoryRepository.findByCategoryNames(requestDto.getCategoryNames());

        return MoimServiceInput.builder()
                .inputMoimDto(moimDto)
                .curMember(curMember)
                .requestRuleJoinDto(requestDto.getRuleJoinDto())
                .moimCategoriesForCreate(moimCategories)
                .build();
    }

    public MoimResponseDto wrapUpAfterCreatingMoim(MoimServiceOutput moimServiceOutput, MoimServiceInput moimServiceInput) {

        List<MoimCategoryLinker> createMoimCategoryLinker = moimServiceOutput.getCreatedMoimCategoryLinkers();
        Moim createdMoim = moimServiceOutput.getCreatedMoim();
        MemberMoimLinker curMemberMoimLinker = moimServiceOutput.getCreatedMemberMoimLinker();
        List<Category> moimCategories = moimServiceInput.getMoimCategoriesForCreate();

        // persist
        createMoimCategoryLinker.forEach(moimCategoryLinkerRepository::save);
        moimRepository.save(createdMoim);

        // response
        MoimMembersDto moimMembersDto = new MoimMembersDto(
                new MyMoimLinkerDto(curMemberMoimLinker),
                new ArrayList<>()); // 생성된 모임은 회원이 없음

        return new MoimResponseDto(
                createdMoim,
                createdMoim.getRuleJoin(),
                createdMoim.getRulePersist(),
                moimCategories,
                moimMembersDto);
    }

    private MoimResponseDto createMoimResponseDto(Moim moim, Member curMember) {
        // null인 상태에서 만약 moim.getRuleJoin()의 값이 null이면 그냥 null임.
        // 결국 moim.getRuleJoin()에 따라서 값이 정해진다는 소리. 초기값 null은 아무 소용이 없다.
        List<Category> categories = moimCategoryLinkerRepository.findWithCategoryByMoimId(moim.getId())
                .stream().map(MoimCategoryLinker::getCategory)
                .collect(Collectors.toList());

        MoimMembersDto moimMembersDto = new MoimMembersDto();
        moim.getMemberMoimLinkers()
                .stream().filter(memberMoimLinker -> memberMoimLinker.getMember().getId().equals(curMember.getId()))
                .forEach(memberMoimLinker -> moimMembersDto.setMyMoimLinkerDto(new MyMoimLinkerDto(memberMoimLinker)));

        List<MoimMemberInfoDto> moimMemberInfoDtos = moim.getMemberMoimLinkers()
                .stream().filter(memberMoimLinker -> !memberMoimLinker.getMember().getId().equals(curMember.getId()))
                .map(MoimMemberInfoDto::createMemberInfoDto)
                .collect(Collectors.toList());
        moimMembersDto.setMoimMemberInfoDto(moimMemberInfoDtos);

        return new MoimResponseDto(moim, moim.getRuleJoin(), moim.getRulePersist(), categories, moimMembersDto);
    }

}
