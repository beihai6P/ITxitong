package com.company.itoms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WorkOrderStatisticsMapper {
    
    @Select("SELECT COUNT(*) FROM work_order WHERE is_deleted = 0")
    long countAll();
    
    @Select("SELECT COUNT(*) FROM work_order WHERE is_deleted = 0 AND create_time >= #{todayStart}")
    long countTodayNew(String todayStart);
    
    @Select("SELECT COUNT(*) FROM work_order WHERE is_deleted = 0 AND status = 1")
    long countWaitDispatch();
    
    @Select("SELECT COUNT(*) FROM work_order WHERE is_deleted = 0 AND status = 2")
    long countWaitAccept();
    
    @Select("SELECT COUNT(*) FROM work_order WHERE is_deleted = 0 AND status = 3")
    long countProcessing();
    
    @Select("SELECT COUNT(*) FROM work_order WHERE is_deleted = 0 AND status IN (4, 5, 6)")
    long countCompleted();
    
    @Select("SELECT COUNT(*) FROM work_order WHERE is_deleted = 0 AND create_time < #{timeoutTime} AND status NOT IN (4, 5, 6)")
    long countTimeout(String timeoutTime);
    
    @Select("SELECT DATE_FORMAT(create_time, '%m-%d') as date, COUNT(*) as count FROM work_order WHERE is_deleted = 0 AND create_time >= #{startDate} GROUP BY DATE_FORMAT(create_time, '%m-%d') ORDER BY date")
    List<Map<String, Object>> countByDay(String startDate);
    
    @Select("SELECT COALESCE(u.real_name, u.username, '未知') as name, COUNT(*) as value FROM work_order w LEFT JOIN user u ON w.assignee_id = u.id WHERE w.is_deleted = 0 AND w.assignee_id IS NOT NULL GROUP BY w.assignee_id, u.real_name, u.username")
    List<Map<String, Object>> countByEngineer();
    
    @Select("SELECT CASE status WHEN 1 THEN '待处理' WHEN 2 THEN '已派单' WHEN 3 THEN '处理中' WHEN 4 THEN '已完成' WHEN 5 THEN '已关闭' ELSE '其他' END as name, COUNT(*) as value FROM work_order WHERE is_deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();
    
    @Select("SELECT COALESCE(u.real_name, u.username, '未知') as engineerName, COUNT(*) as totalOrder, SUM(CASE WHEN status IN (4, 5, 6) THEN 1 ELSE 0 END) as finishOrder, ROUND(SUM(CASE WHEN status IN (4, 5, 6) THEN 1 ELSE 0 END) / COUNT(*) * 100, 2) as finishRate, AVG(TIMESTAMPDIFF(MINUTE, create_time, IFNULL(update_time, NOW()))) as avgProcessTime FROM work_order w LEFT JOIN user u ON w.assignee_id = u.id WHERE w.is_deleted = 0 AND w.assignee_id IS NOT NULL GROUP BY w.assignee_id, u.real_name, u.username")
    List<Map<String, Object>> countEngineerStats();
}
