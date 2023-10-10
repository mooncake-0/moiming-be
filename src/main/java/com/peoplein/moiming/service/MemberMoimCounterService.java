package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.MemberMoimCounter;
import com.peoplein.moiming.domain.MemberMoimMaterializedView;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.repository.MemberMoimCounterRepository;
import com.peoplein.moiming.repository.MemberMoimMaterializedViewRepository;
import com.peoplein.moiming.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberMoimCounterService {

    private static final int RETRY_COUNT = 3;
    private final MemberMoimCounterRepository memberMoimCounterRepository;
    private final MemberMoimMaterializedViewRepository materializedViewRepository;
    private final MoimRepository moimRepository;

    // 트랜잭션은 1개씩
    public void create(Long memberId, Long moimId, LocalDate visitDate) {
        // Named Lock get
        final String lockName = String.format("%d-%d", memberId, moimId);

        for (int i = 0; i < RETRY_COUNT; i++) {
            boolean lock = memberMoimCounterRepository.acquireLock(lockName);
            if (lock)
                break;
        }

        Optional<MemberMoimCounter> findInstance = memberMoimCounterRepository.findBy(memberId, moimId, visitDate);
        if (findInstance.isPresent()) {
            memberMoimCounterRepository.releaseLock(lockName);
            return;
        }

        MemberMoimCounter createdInstance = MemberMoimCounter.create(memberId, moimId, visitDate);
        memberMoimCounterRepository.save(createdInstance);

        // Named Lock release
        memberMoimCounterRepository.releaseLock(lockName);
    }

    public void update() {
        final List<MemberMoimCounterRepository.MoimViewCountTuple> groupByResult = memberMoimCounterRepository.findByGroupByMoimId();
        final Map<Long, MemberMoimMaterializedView> collected = groupByResult.stream()
                .map(moimViewCountTuple -> MemberMoimMaterializedView.create(moimViewCountTuple.getViewCount(), moimViewCountTuple.getMoimId()))
                .collect(Collectors.toMap(MemberMoimMaterializedView::getMoimId, memberMoimMaterializedView -> memberMoimMaterializedView));

        final List<Long> moimIdList = new ArrayList<>(collected.keySet());
        final List<MemberMoimMaterializedView> findMaterializedViewByMoimIds = materializedViewRepository.findByMoimIds(moimIdList);

        findMaterializedViewByMoimIds.forEach(mView -> mView.updateCount(
                collected.get(mView.getMoimId()).getCount()));

        final Set<Long> inDBMoimIds = findMaterializedViewByMoimIds.stream()
                .map(MemberMoimMaterializedView::getMoimId)
                .collect(Collectors.toSet());

        final Set<Long> findMViewMoimIds = collected.keySet();
        findMViewMoimIds.removeAll(inDBMoimIds);

        findMViewMoimIds.stream()
                .map(collected::get)
                .forEach(materializedViewRepository::save);
    }

    public void deleteDoesNotExistMoim() {

        final List<MemberMoimMaterializedView> mViews = materializedViewRepository.findAll();
        final Set<Long> mViewMoimIds = mViews.stream().map(MemberMoimMaterializedView::getMoimId).collect(Collectors.toSet());

        final Set<Long> findMoimIds = moimRepository.findAllMoim().stream().map(Moim::getId).collect(Collectors.toSet());

        mViewMoimIds.removeAll(findMoimIds);

        final List<Long> shouldRemoveMoimIds = new ArrayList<>(mViewMoimIds);
        materializedViewRepository.deleteMViews(shouldRemoveMoimIds);
        memberMoimCounterRepository.deleteByMoimIds(shouldRemoveMoimIds);
    }
}
