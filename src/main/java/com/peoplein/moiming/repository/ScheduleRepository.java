package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.Schedule;

import java.util.List;

public interface ScheduleRepository {

    Long save(Schedule schedule);

    Schedule findById(Long scheduleId);

    Schedule findWithMoimById(Long scheduleId);

    Schedule findWithMemberScheduleLinkersById(Long scheduleId);

    List<Schedule> findByMoimId(Long moimId);

    void removeAll(List<Long> scheduleIds);

    void remove(Schedule schedule);

}
