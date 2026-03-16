package com.studyhelper.service;

import com.studyhelper.entity.Course;
import com.studyhelper.entity.StudyRecord;
import com.studyhelper.entity.Task;
import com.studyhelper.repository.CourseRepository;
import com.studyhelper.repository.StudyRecordRepository;
import com.studyhelper.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticService {

    @Autowired
    private StudyRecordRepository studyRecordRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * 获取用户学习统计数据
     */
    public Map<String, Object> getUserStatistics(Long userId, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (period.toLowerCase()) {
            case "week":
                startDate = endDate.minusDays(6);
                break;
            case "month":
                startDate = endDate.minusDays(29);
                break;
            case "year":
                startDate = endDate.minusDays(364);
                break;
            default:
                startDate = endDate.minusDays(6); // 默认一周
        }

        Map<String, Object> result = new HashMap<>();

        // 1. 总学习时长
        Integer totalDuration = studyRecordRepository.getTotalDurationByUserIdAndDateRange(userId, startDate, endDate);
        result.put("totalDuration", totalDuration);
        result.put("totalHours", Math.round(totalDuration / 60.0 * 10) / 10.0); // 转换为小时，保留1位小数

        // 2. 按课程统计学习时长（饼图数据）
        List<Object[]> courseStats = studyRecordRepository.getDurationByCourse(userId, startDate, endDate);
        List<Map<String, Object>> courseDistribution = courseStats.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("courseId", row[0]);
            item.put("courseName", row[1]);
            item.put("duration", row[2]);
            item.put("hours", Math.round(((Number) row[2]).doubleValue() / 60.0 * 10) / 10.0);
            return item;
        }).collect(Collectors.toList());
        result.put("courseDistribution", courseDistribution);

        // 3. 每日学习时长趋势（折线图数据）
        List<Object[]> dailyStats = studyRecordRepository.getDailyDuration(userId, startDate, endDate);
        List<Map<String, Object>> dailyTrend = new ArrayList<>();
        
        // 填充缺失日期的数据
        LocalDate currentDate = startDate;
        Map<LocalDate, Integer> dailyMap = dailyStats.stream()
                .collect(Collectors.toMap(
                    row -> (LocalDate) row[0],
                    row -> ((Number) row[1]).intValue()
                ));

        while (!currentDate.isAfter(endDate)) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", currentDate.toString());
            dayData.put("duration", dailyMap.getOrDefault(currentDate, 0));
            dayData.put("hours", Math.round(dailyMap.getOrDefault(currentDate, 0) / 60.0 * 10) / 10.0);
            dailyTrend.add(dayData);
            currentDate = currentDate.plusDays(1);
        }
        result.put("dailyTrend", dailyTrend);

        // 4. 任务完成统计
        List<Task> tasks = taskRepository.findByUserIdAndPlanDateBetween(userId, startDate, endDate);
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == Task.Status.COMPLETED)
                .count();
        
        result.put("totalTasks", totalTasks);
        result.put("completedTasks", completedTasks);
        result.put("completionRate", totalTasks > 0 ? Math.round((double) completedTasks / totalTasks * 1000) / 10.0 : 0);

        // 5. 学习天数
        long studyDays = dailyStats.size();
        result.put("studyDays", studyDays);
        result.put("totalDays", startDate.until(endDate).getDays() + 1);

        return result;
    }

    /**
     * 获取对比统计数据（本期 vs 上期）
     */
    public Map<String, Object> getComparisonStatistics(Long userId, String period) {
        LocalDate now = LocalDate.now();
        LocalDate currentStart, currentEnd, previousStart, previousEnd;

        switch (period.toLowerCase()) {
            case "week":
                currentEnd = now;
                currentStart = now.minusDays(6);
                previousEnd = currentStart.minusDays(1);
                previousStart = previousEnd.minusDays(6);
                break;
            case "month":
                currentEnd = now;
                currentStart = now.minusDays(29);
                previousEnd = currentStart.minusDays(1);
                previousStart = previousEnd.minusDays(29);
                break;
            case "year":
                currentEnd = now;
                currentStart = now.minusDays(364);
                previousEnd = currentStart.minusDays(1);
                previousStart = previousEnd.minusDays(364);
                break;
            default:
                currentEnd = now;
                currentStart = now.minusDays(6);
                previousEnd = currentStart.minusDays(1);
                previousStart = previousEnd.minusDays(6);
        }

        Map<String, Object> current = getUserStatistics(userId, period);
        current.put("period", "本期");

        // 计算上期数据
        Integer prevTotalDuration = studyRecordRepository.getTotalDurationByUserIdAndDateRange(userId, previousStart, previousEnd);
        List<Object[]> prevCourseStats = studyRecordRepository.getDurationByCourse(userId, previousStart, previousEnd);
        List<Task> prevTasks = taskRepository.findByUserIdAndPlanDateBetween(userId, previousStart, previousEnd);
        
        long prevCompletedTasks = prevTasks.stream()
                .filter(t -> t.getStatus() == Task.Status.COMPLETED)
                .count();

        Map<String, Object> previous = new HashMap<>();
        previous.put("period", "上期");
        previous.put("totalDuration", prevTotalDuration);
        previous.put("totalHours", Math.round(prevTotalDuration / 60.0 * 10) / 10.0);
        previous.put("completedTasks", prevCompletedTasks);
        previous.put("totalTasks", prevTasks.size());
        previous.put("completionRate", prevTasks.size() > 0 ? Math.round((double) prevCompletedTasks / prevTasks.size() * 1000) / 10.0 : 0);

        return Map.of(
            "current", current,
            "previous", previous
        );
    }

    /**
     * 创建学习记录（从任务完成情况推断）
     */
    public void createStudyRecordFromTasks(Long userId) {
        LocalDate today = LocalDate.now();
        List<Task> completedTodayTasks = taskRepository.findByUserIdAndPlanDateAndStatus(
                userId, today, Task.Status.COMPLETED);

        if (completedTodayTasks.isEmpty()) return;

        // 按课程分组统计
        Map<Long, List<Task>> tasksByCourse = completedTodayTasks.stream()
                .filter(t -> t.getCourse() != null)
                .collect(Collectors.groupingBy(t -> t.getCourse().getId()));

        for (Map.Entry<Long, List<Task>> entry : tasksByCourse.entrySet()) {
            Long courseId = entry.getKey();
            List<Task> courseTasks = entry.getValue();

            // 估算学习时长：每个任务约30分钟
            int estimatedDuration = courseTasks.size() * 30;

            StudyRecord record = new StudyRecord();
            record.setUser(courseTasks.get(0).getUser());
            record.setCourse(courseTasks.get(0).getCourse());
            record.setStudyDate(today);
            record.setDurationMinutes(estimatedDuration);
            record.setTaskCount(courseTasks.size());
            record.setNote("从" + courseTasks.size() + "个完成的任务推断");

            studyRecordRepository.save(record);
        }
    }
}
