//package com.peoplein.moiming.temp;
//
//import com.peoplein.moiming.domain.*;
//import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
//import com.peoplein.moiming.domain.enums.ReviewQuestionType;
//import com.peoplein.moiming.domain.fixed.ReviewQuestion;
//import com.peoplein.moiming.domain.member.Member;
//import com.peoplein.moiming.domain.moim.MoimMember;
//import com.peoplein.moiming.domain.moim.Moim;
//import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
//import com.peoplein.moiming.model.dto.domain.QuestionChoiceDto;
//import com.peoplein.moiming.model.dto.domain.ReviewAnswerDto;
//import com.peoplein.moiming.model.dto.domain.ReviewQuestionDto;
//import com.peoplein.moiming.model.dto.request_b.MoimReviewRequestDto;
//import com.peoplein.moiming.model.dto.request_b.ReviewAnswerRequestDto;
//import com.peoplein.moiming.model.dto.response_b.MoimReviewResponseDto;
//import com.peoplein.moiming.model.dto.response_b.ReviewQuestionAnswerDto;
//import com.peoplein.moiming.model.dto.response_b.ReviewQuestionResponseDto;
//import com.peoplein.moiming.repository.MoimMemberRepository;
//import com.peoplein.moiming.repository.MoimRepository;
//import com.peoplein.moiming.repository.MoimReviewRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import javax.transaction.Transactional;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//@Transactional
//public class MoimReviewService {
//
//    private final MoimRepository moimRepository;
//    private final MoimReviewRepository moimReviewRepository;
//    private final MoimMemberRepository moimMemberRepository;
//
//
//    public MoimReviewResponseDto createReview(MoimReviewRequestDto moimReviewRequestDto, Member curMember) {
//
//        Optional<MoimReview> existCheck = moimReviewRepository.findOptionalWithMemberByMemberAndMoimId(curMember.getId(), moimReviewRequestDto.getMoimId());
//
//        if (existCheck.isPresent()) {
//            log.error(curMember.getId() + "님은 이미 후기를 작성하셨습니다");
//            throw new RuntimeException(curMember.getId() + "님은 이미 후기를 작성하셨습니다");
//        }
//        // Repository 단 접근을 통해서 필요한 객체들
//        // 목적 : Review 객체 및 ReviewAnswer 객체들을 만든다
//        List<ReviewAnswerRequestDto> requests = moimReviewRequestDto.getReviewAnswerRequestDtos();
//        Moim moim = moimRepository.findById(moimReviewRequestDto.getMoimId()).orElseThrow();
//
//        // 해당 멤버가 해당 모임에 대해 작성한 MoimReview 가 없는지 확인한다
//
//        // MoimReview 를 먼저 형성하고, 답변에 따른 ReviewQuestion 을 불러와 ReviewAnswer 를 형성한다.
//        MoimReview moimReview = MoimReview.writeReview(curMember, moim);
//        List<Long> reviewQuestionIds = requests.stream().map(dto -> dto.getReviewQuestionDto().getReviewQuestionId()).collect(Collectors.toList());
//        // 한번에 불러온 뒤,
//        List<ReviewQuestion> answeredQuestions = moimReviewRepository.findReviewQuestionByIds(reviewQuestionIds);
//
//
//        // TODO: DB 통신결과로 불러오는 객체들은 따로 묶어 놓는다. 따로 Test 될 수 있도록
//        if (Objects.isNull(moim)) {
//            log.error("존재하지 않는 모임입니다");
//            throw new RuntimeException("존재하지 않는 모임입니다");
//        }
//
//
//        requests.forEach(dto -> {
//
//            ReviewQuestion reviewQuestion = answeredQuestions.stream().filter(x -> x.getId().equals(dto.getReviewQuestionDto().getReviewQuestionId())).findFirst().get();
//
//            boolean isChoiceExist = reviewQuestion.getQuestionChoices().stream().anyMatch(questionChoice -> questionChoice.getChoiceOrder() == dto.getReviewAnswerDto().getAnwChoice());
//
//            if (!isChoiceExist) throw new RuntimeException("해당 질문에 대한 적절한 선택지가 아닙니다");
//
//            ReviewAnswer reviewAnswer = ReviewAnswer.createAnswer(dto.getReviewAnswerDto().getAnwChoice(), dto.getReviewAnswerDto().getAnwText(), moimReview, reviewQuestion);
//        });
//
//        moimReviewRepository.save(moimReview);
//
//        // curMember 가 보낸 create 에 대한 response_b 이므로, moimMemberInfo 는 추가하지 않는다
//
//        MoimReviewResponseDto moimReviewResponseDto = new MoimReviewResponseDto();
//
//        List<ReviewQuestionAnswerDto> reviewQuestionAnswerDtos = new ArrayList<>();
//
//        moimReview.getReviewAnswers().forEach(reviewAnswer -> {
//
//            ReviewQuestionDto reviewQuestionDto = new ReviewQuestionDto(reviewAnswer.getReviewQuestion().getId(), reviewAnswer.getReviewQuestion().getReviewQuestionType(), reviewAnswer.getReviewQuestion().getQuestionName(), reviewAnswer.getReviewQuestion().getQuestionInfo());
//
//            ReviewAnswerDto reviewAnswerDto = new ReviewAnswerDto(reviewAnswer.getId(), reviewAnswer.getAnwChoice(), reviewAnswer.getAnwText());
//
//            ReviewQuestionAnswerDto reviewQuestionAnswerDto = new ReviewQuestionAnswerDto(reviewQuestionDto, reviewAnswerDto);
//            reviewQuestionAnswerDtos.add(reviewQuestionAnswerDto);
//
//        });
//
//        moimReviewResponseDto.setMoimReviewId(moimReview.getId());
//        moimReviewResponseDto.setMoimReviewQuestionAnswerDto(reviewQuestionAnswerDtos);
//        moimReviewResponseDto.setCreatedAt(moimReview.getCreatedAt());
//        moimReviewResponseDto.setUpdatedAt(moimReviewResponseDto.getUpdatedAt());
//
//        return moimReviewResponseDto;
//
//    }
//
//
//    public List<ReviewQuestionResponseDto> getReviewQuestions() {
//
//        List<ReviewQuestion> allQuestions = moimReviewRepository.findAllReviewQuestions();
//
//        List<ReviewQuestionResponseDto> questionResponseDtos = new ArrayList<>();
//
//        allQuestions.forEach(reviewQuestion -> {
//
//            ReviewQuestionDto reviewQuestionDto = new ReviewQuestionDto(reviewQuestion.getId(), reviewQuestion.getReviewQuestionType(), reviewQuestion.getQuestionName(), reviewQuestion.getQuestionInfo());
//
//            List<QuestionChoiceDto> questionChoiceDtos = new ArrayList<>();
//            reviewQuestion.getQuestionChoices().forEach(questionChoice -> {
//
//                QuestionChoiceDto questionChoiceDto = new QuestionChoiceDto(questionChoice.getId(), questionChoice.getChoiceInfo(), questionChoice.getChoiceOrder());
//                questionChoiceDtos.add(questionChoiceDto);
//
//            });
//
//            questionResponseDtos.add(new ReviewQuestionResponseDto(reviewQuestionDto, questionChoiceDtos));
//        });
//
//        return questionResponseDtos;
//    }
//
//    public MoimReviewResponseDto getReview(Long reviewId, Member curMember) {
//
//        // MoimAnswer 중 reviewId 를 가지고 있는 모든 것을 가져오며, MoimReview 와 AnswerQuestion 모두 Join 하여 가져온다.
//        List<ReviewAnswer> reviewAnswers = moimReviewRepository.findReviewAnswerByMoimReviewId(reviewId);
//        MoimReview moimReview = reviewAnswers.get(0).getMoimReview();
//
//        MoimReviewResponseDto moimReviewResponseDto = new MoimReviewResponseDto();
//
//        List<ReviewQuestionAnswerDto> reviewQuestionAnswerDtos = new ArrayList<>();
//
//        moimReview.getReviewAnswers().forEach(reviewAnswer -> {
//
//            ReviewQuestionDto reviewQuestionDto = new ReviewQuestionDto(reviewAnswer.getReviewQuestion().getId(), reviewAnswer.getReviewQuestion().getReviewQuestionType(), reviewAnswer.getReviewQuestion().getQuestionName(), reviewAnswer.getReviewQuestion().getQuestionInfo());
//
//            ReviewAnswerDto reviewAnswerDto = new ReviewAnswerDto(reviewAnswer.getId(), reviewAnswer.getAnwChoice(), reviewAnswer.getAnwText());
//
//            ReviewQuestionAnswerDto reviewQuestionAnswerDto = new ReviewQuestionAnswerDto(reviewQuestionDto, reviewAnswerDto);
//            reviewQuestionAnswerDtos.add(reviewQuestionAnswerDto);
//
//        });
//
//        moimReviewResponseDto.setMoimReviewId(moimReview.getId());
//        moimReviewResponseDto.setMoimReviewQuestionAnswerDto(reviewQuestionAnswerDtos);
//        moimReviewResponseDto.setCreatedAt(moimReview.getCreatedAt());
//        moimReviewResponseDto.setUpdatedAt(moimReviewResponseDto.getUpdatedAt());
//
//        Member reviewCreator = moimReview.getMember();
//        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(reviewCreator.getId(), moimReview.getMoim().getId()).orElseThrow();
//
//        MoimMemberInfoDto moimMemberInfoDto = new MoimMemberInfoDto(reviewCreator.getId()
//                , reviewCreator.getMemberInfo().getMemberName(), reviewCreator.getMemberEmail()
//                , reviewCreator.getMemberInfo().getMemberGender(), moimMember.getMemberRoleType()
//                , moimMember.getMemberState(), moimMember.getCreatedAt(), moimMember.getUpdatedAt());
//
//        moimReviewResponseDto.setMoimMemberInfoDto(moimMemberInfoDto);
//
//        return moimReviewResponseDto;
//    }
//
//
//    public List<MoimReviewResponseDto> viewAllMoimReview(Long moimId, Member curMember) {
//
////        MoimReviewREsponseDto --> 필요한 것 // moimReivew 정보, 생성자와 Moim 간의 정보, ReviewQuestion & Answer 정보
//
//        // 특정 moimId >> 모든 MoimReview, 모든 ReviewAnswer, ReviewQuestion 까지는 조회해올 수 있음
//        List<MoimReview> moimReviews = moimReviewRepository.findAllByMoimId(moimId);
//        List<MoimReviewResponseDto> moimReviewResponseDtos = new ArrayList<>();
//        List<Long> memberIds = new ArrayList<>();
//
//        moimReviews.forEach(moimReview -> {
//
//            memberIds.add(moimReview.getMember().getId());
//
//            MoimReviewResponseDto moimReviewResponseDto = new MoimReviewResponseDto();
//
//            moimReviewResponseDto.setMoimReviewId(moimReview.getId());
//            moimReviewResponseDto.setCreatedAt(moimReview.getCreatedAt());
//            moimReviewResponseDto.setUpdatedAt(moimReview.getUpdatedAt());
//
//            List<ReviewQuestionAnswerDto> moimReviewQuestionAnswerDto = new ArrayList<>();
//
//            // N^2 발생
//            moimReview.getReviewAnswers().forEach(reviewAnswer -> {
//
//                ReviewQuestionDto reviewQuestionDto = new ReviewQuestionDto(reviewAnswer.getReviewQuestion().getId(), reviewAnswer.getReviewQuestion().getReviewQuestionType(), reviewAnswer.getReviewQuestion().getQuestionName(), reviewAnswer.getReviewQuestion().getQuestionInfo());
//
//                ReviewAnswerDto reviewAnswerDto = new ReviewAnswerDto(reviewAnswer.getId(), reviewAnswer.getAnwChoice(), reviewAnswer.getAnwText());
//
//                ReviewQuestionAnswerDto reviewQuestionAnswerDto = new ReviewQuestionAnswerDto(reviewQuestionDto, reviewAnswerDto);
//
//                moimReviewQuestionAnswerDto.add(reviewQuestionAnswerDto);
//            });
//
//            // 미리 아래 매칭을 위해서 Member 를 가능한 정보들로 세틍해 놓는다.
//            moimReviewResponseDto.setMoimMemberInfoDto(new MoimMemberInfoDto(moimReview.getMember().getId()
//                    ,  moimReview.getMember().getMemberInfo().getMemberName()
//                    , moimReview.getMember().getMemberEmail(), moimReview.getMember().getMemberInfo().getMemberGender()));
//            moimReviewResponseDto.setMoimReviewQuestionAnswerDto(moimReviewQuestionAnswerDto);
//            moimReviewResponseDtos.add(moimReviewResponseDto);
//        });
//
//        // MMInfoDto 도 같이 세팅해준다
//        List<MoimMember> membersLinkers = moimMemberRepository.findByMoimIdAndMemberIds(moimId, memberIds);
//
//        membersLinkers.forEach(mml -> {
//
//            MoimReviewResponseDto moimReviewResponseDto = moimReviewResponseDtos.stream().filter(reviewDto -> reviewDto.getMoimMemberInfoDto().getMemberId().equals(mml.getMember().getId())).findAny().orElseThrow(() -> new RuntimeException("이상한 에러입니다"));
//
//            moimReviewResponseDto.getMoimMemberInfoDto().setMoimMemberState(mml.getMemberState());
//            moimReviewResponseDto.getMoimMemberInfoDto().setMoimMemberRoleType(mml.getMemberRoleType());
//            moimReviewResponseDto.getMoimMemberInfoDto().setCreatedAt(mml.getCreatedAt());
//            moimReviewResponseDto.getMoimMemberInfoDto().setUpdatedAt(mml.getUpdatedAt());
//
//        });
//        return moimReviewResponseDtos;
//    }
//
//    public MoimReviewResponseDto updateReview(MoimReviewRequestDto moimReviewRequestDto, Member curMember) {
//
//        // 1. DTO  확인을 통한 객체 확인
//        MoimReview moimReview = moimReviewRepository.findOptionalWithMemberById(moimReviewRequestDto.getMoimReviewId()).orElseThrow(() -> new RuntimeException("해당 MoimReview 는 존재하지 않는디..?"));
//
//        // 2. curMember 를 통한 권한 확인
//        if (!curMember.getId().equals(moimReview.getMember().getId())) {
//            log.error("후기는 후기 작성자만이 수정할 수 있습니다");
//            throw new RuntimeException("후기는 후기 작성자만이 수정할 수 있습니다");
//        }
//
//        // ResponseDto 를 위한 준비
//        MoimReviewResponseDto moimReviewResponseDto = new MoimReviewResponseDto();
//        List<ReviewQuestionAnswerDto> reviewQuestionAnswerDtos = new ArrayList<>();
//
//        // 3. 변경 내역 확인
//        List<ReviewAnswer> reviewAnswers = moimReview.getReviewAnswers();
//
//        // 4. 변경 진행
//        // N^2
//        moimReviewRequestDto.getReviewAnswerRequestDtos().forEach(requestDto -> {
//
//            // 변경 진행
//            ReviewAnswer thisAnswer = reviewAnswers.stream().filter(
//                    reviewAnswer -> requestDto.getReviewAnswerDto().getReviewAnswerId().equals(reviewAnswer.getId())
//            ).findAny().orElseThrow(() -> new RuntimeException("보낸 요청에 대한 Review 가 존재하지 않는 이상한 상황입니다"));
//
//            if (requestDto.getReviewQuestionDto().getReviewQuestionType().equals(ReviewQuestionType.CHOICE)) {
//                if (requestDto.getReviewAnswerDto().getAnwChoice() != thisAnswer.getAnwChoice()) {
//                    thisAnswer.setAnwChoice(requestDto.getReviewAnswerDto().getAnwChoice());
//                    moimReview.setUpdatedAt(LocalDateTime.now());
//                }
//            }
//
//            if (requestDto.getReviewQuestionDto().getReviewQuestionType().equals(ReviewQuestionType.SHORT_WRITE)) {
//                if (!requestDto.getReviewAnswerDto().getAnwText().equals(thisAnswer.getAnwText())) {
//                    thisAnswer.setAnwText(requestDto.getReviewAnswerDto().getAnwText());
//                    moimReview.setUpdatedAt(LocalDateTime.now());
//                }
//            }
//
//            // 변경을 토대로 DTO 준비
//            ReviewQuestionDto reviewQuestionDto = new ReviewQuestionDto(thisAnswer.getReviewQuestion().getId(), thisAnswer.getReviewQuestion().getReviewQuestionType(), thisAnswer.getReviewQuestion().getQuestionName(), thisAnswer.getReviewQuestion().getQuestionInfo());
//
//            ReviewAnswerDto reviewAnswerDto = new ReviewAnswerDto(thisAnswer.getId(), thisAnswer.getAnwChoice(), thisAnswer.getAnwText());
//
//            ReviewQuestionAnswerDto reviewQuestionAnswerDto = new ReviewQuestionAnswerDto(reviewQuestionDto, reviewAnswerDto);
//            reviewQuestionAnswerDtos.add(reviewQuestionAnswerDto);
//        });
//
//        moimReviewResponseDto.setMoimReviewId(moimReview.getId());
//        moimReviewResponseDto.setMoimReviewQuestionAnswerDto(reviewQuestionAnswerDtos);
//        moimReviewResponseDto.setCreatedAt(moimReview.getCreatedAt());
//        moimReviewResponseDto.setUpdatedAt(moimReviewResponseDto.getUpdatedAt());
//        // 요청자가 본인이므로 별도의 유저 정보는 보내주지 않는다
//
//        return moimReviewResponseDto;
//    }
//
//    public void deleteReview(Long reviewId, Member curMember) {
//
//        // 후기 삭제 권한 -> 모임 운영자, 관리자 // 작성자
//        // 1. MoimReview 객체 확인
//        MoimReview moimReview = moimReviewRepository.findOptionalWithMemberById(reviewId).orElseThrow(() -> new RuntimeException("해당 MoimReview 는 존재하지 않는디..?"));
//
//        // 2. 권한 확인
//        if (!curMember.getId().equals(moimReview.getMember().getId())) { // 1. 모임 생성자인지 확인
//            // MML 조회를 통한 권한 확인
//            MoimMember curMoimMember = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), moimReview.getMoim().getId())
//                    .orElseThrow(() -> new RuntimeException("해당 모임에 연관관계가 존재하지 않는 유저입니다"));
//
//            // 둘 다 아닐 경우 에러가 발생해야 한다
//            if (!curMoimMember.getMemberRoleType().equals(MoimMemberRoleType.MANAGER)) {
//                log.error("후기를 삭제할 권한이 없습니다");
//                throw new RuntimeException("후기를 삭제할 권한이 없습니다");
//            }
//        }
//
//        // 3. MoimReview 를 삭제하기 위해 유일한 의존성이 존재하는 ReviewAnswers 들을 모두 삭제해주는 것도 병행한다
//        try {
//            List<Long> reviewAnswerIds = moimReview.getReviewAnswers().stream().map(ReviewAnswer::getId).collect(Collectors.toList());
//            moimReviewRepository.removeWithAllReviewAnswers(reviewId, reviewAnswerIds);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw new RuntimeException(e.getMessage());
//        }
//    }
//
//}