//package com.peoplein.moiming.repository;
//
//import com.peoplein.moiming.temp.Schedule;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface ScheduleRepository {
//
//    Long save(Schedule schedule);
//
//    Schedule findById(Long scheduleId);
//
//    Optional<Schedule> findOptionalById(Long scheduleId);
//
//    Schedule findWithMoimById(Long scheduleId);
//
//    Schedule findWithMemberScheduleLinkersById(Long scheduleId);
//
//    List<Schedule> findByMoimId(Long moimId);
//
//    void removeAll(List<Long> scheduleIds);
//
//    void remove(Schedule schedule);
//
//    List<Schedule> findAllSchedule();
//}
