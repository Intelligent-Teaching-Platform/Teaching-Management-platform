package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.Location;
import com.example.dto.*;
import com.example.entity.*;
import com.example.exception.ServiceException;
import com.example.mapper.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SignInService {
    private final SignInMapper signInMapper;
    private final SignInRecordMapper signInRecordMapper;
    private final CourseMapper courseMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final ChoiceMapper choiceMapper;


    private static final double EARTH_RADIUS = 6371000; // 地球半径(米)

    // 创建签到（移除token）
    public SignInDTO createSignIn(SignInCreateDTO dto) {
        // 1. 验证课程和教师
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) throw new ServiceException("课程不存在");

        Teacher teacher = teacherMapper.selectById(course.getTeacherId());
        if (teacher == null) throw new ServiceException("教师不存在");


        // 2. 解析上课地点坐标（示例简化）
        Location location = parseLocation(course.getLocation());

        // 3. 计算时间窗口
        Date startTime = new Date();
        Date endTime = calculateEndTime(dto.getDuration());

        // 4. 保存签到记录
        SignIn signIn = new SignIn();
        signIn.setCourseId(dto.getCourseId());
        signIn.setTeacherId(teacher.getId());
        signIn.setLatitude(location.getLatitude());
        signIn.setLongitude(location.getLongitude());
        signIn.setDistance(dto.getDistance());
        signIn.setStartTime(startTime);
        signIn.setEndTime(endTime);
        signIn.setSnum(course.getAlreadyNum());


        signInMapper.insert(signIn);

        // 5. 返回结果
        SignInDTO result = new SignInDTO();
        result.setId(signIn.getId());
        result.setExpireTime(endTime);
        return result;
    }

    // 记录签到（移除token验证）
    public void recordSignIn(SignInRecordDTO dto) {
        // 参数校验
        if (dto.getStudentId() == null || dto.getCourseId() == null
                || dto.getLatitude() == null || dto.getLongitude() == null) {
            throw new ServiceException("参数不完整");
        }

        // 1. 验证学生是否属于该课程
        if (!choiceMapper.existsRelation(dto.getStudentId(), dto.getCourseId())) {
            throw new ServiceException("学生未选修该课程");
        }

        // 2. 检查有效签到（通过课程ID+时间）
        SignIn signIn = signInMapper.selectThis(dto.getCourseId(), new Date()); // 修改这里
        if (signIn == null) {
            throw new ServiceException("当前没有可用的签到");
        }


//      //3. 防止重复签到，
//        if (signInRecordMapper.existsRecord(dto.getStudentId(), signIn.getId())==1) {
//            throw new ServiceException("你已签到");
//        }



        // 4. 计算距离（如果开启位置验证）
        double distance = 0;
        if (signIn.getDistance() > 0 && signIn.getLatitude() != null && signIn.getLongitude() != null) {
            distance = calculateDistance(
                    signIn.getLatitude().doubleValue(),
                    signIn.getLongitude().doubleValue(),
                    dto.getLatitude(),
                    dto.getLongitude());
        }

        // 5. 保存记录
        SignInRecord record = new SignInRecord();
        record.setSignInId(signIn.getId());
        record.setStudentId(dto.getStudentId());
        record.setLatitude(BigDecimal.valueOf(dto.getLatitude()));
        record.setLongitude(BigDecimal.valueOf(dto.getLongitude()));
        record.setDistance((int) Math.round(distance));
        record.setStatus(signIn.getDistance() == 0 || distance <= signIn.getDistance() ?
                "SUCCESS" : "FAIL");           
        signInRecordMapper.insert(record);

        // 6. 更新签到人数
        if ("SUCCESS".equals(record.getStatus())) {

            signInMapper.updateSignInNum(signIn.getId());
        }

//        SignDTO result = new SignDTO();
//        result.setSignInId(signIn.getId());
//
//
//        Integer num = signInMapper.selectNumById(signIn.getId());
//        Integer snum = signInMapper.selectSnumById(signIn.getId());
//
//        result.setNum(num);
//        result.setUnum(snum - num);
//        return result;


    }

    public SignDTO selectAll(Integer courseId) {
        SignDTO result = new SignDTO();

        if (courseId == null) {
            // 兼容旧逻辑：没有课程ID时返回全局统计（不含姓名）
            Integer num = signInMapper.selectNum();
            Integer snum = signInMapper.selectSnum();
            result.setNum(num);
            result.setUnum(snum - num);
            return result;
        }

        // 1. 获取该课程的所有选课学生
        List<Student> allStudents = studentMapper.selectByCourseId(courseId);
        Set<Integer> allStudentIds = allStudents.stream()
                .map(Student::getId)
                .collect(Collectors.toSet());

        // 2. 获取该课程最近一次签到记录
        SignIn latest = signInMapper.selectActiveByCourseId(courseId);
        if (latest == null) {
            // 没有正在进行的签到，则按最近一条历史签到
            List<SignIn> history = signInMapper.selectByCourseId(courseId);
            if (!history.isEmpty()) {
                latest = history.get(history.size() - 1);
            }
        }

        if (latest == null || allStudentIds.isEmpty()) {
            result.setNum(0);
            result.setUnum(allStudentIds.size());
            result.setSignedNames(Collections.emptyList());
            result.setUnsignedNames(
                    allStudents.stream().map(Student::getName).collect(Collectors.toList())
            );
            return result;
        }

        // 固定当前使用的签到ID，避免在 lambda 中引用可变变量
        final Integer latestId = latest.getId();
        result.setSignInId(latestId);

        // 3. 查询该次签到的签到记录
        List<SignInRecord> records = signInRecordMapper.selectByCourseId(courseId).stream()
                .filter(r -> Objects.equals(r.getSignInId(), latestId))
                .collect(Collectors.toList());

        Set<Integer> signedIds = records.stream()
                .map(SignInRecord::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<String> signedNames = records.stream()
                .map(SignInRecord::getStudentName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> unsignedNames = allStudents.stream()
                .filter(s -> !signedIds.contains(s.getId()))
                .map(Student::getName)
                .collect(Collectors.toList());

        result.setNum(signedNames.size());
        result.setUnum(unsignedNames.size());
        result.setSignedNames(signedNames);
        result.setUnsignedNames(unsignedNames);
        return result;
    }



    // 获取当前有效签到（增加学生课程关系检查）
    public SignIn getActiveSignIn(Integer courseId, Integer studentId) {
        // 检查学生是否属于该课程
//        if (choice.existsRelation(dto.getStudentId(), dto.getCourseId()) <= 0) {
//            throw new ServiceException("学生未选修该课程");
//        }

        // 查询当前课程的有效签到
        return signInMapper.selectOne(new LambdaQueryWrapper<SignIn>()
                .eq(SignIn::getCourseId, courseId)
                .gt(SignIn::getEndTime, new Date())
                .last("LIMIT 1"));
    }

    // 计算两点距离（保留）
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    // 获取签到统计（不变）
    public SignInStatsDTO getSignInStats(Integer courseId) {
        List<Map<String, Object>> stats = signInMapper.selectSignInStats(courseId);
        SignInStatsDTO result = new SignInStatsDTO();

        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (Map<String, Object> stat : stats) {
            dates.add((String) stat.get("date"));
            counts.add(((Long) stat.get("count")).intValue()); // 注意类型转换
        }

        result.setDates(dates);
        result.setCounts(counts);
        return result;
    }

    // 获取签到记录（不变）
    public List<SignInRecord> getSignInRecords(Integer courseId) {
        return signInRecordMapper.selectByCourseId(courseId);
    }

    // --- 私有方法 ---
    private Date calculateEndTime(int durationMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, durationMinutes);
        return calendar.getTime();
    }

    private Location parseLocation(String location) {
        // 示例：实际应调用地图API解析地址
        return new Location(new BigDecimal("39.9042"), new BigDecimal("116.4074"));
    }


//    public PageInfo<SignIn> selectPage(SignIn signIn, Integer pageNum, Integer pageSize) {
//        PageHelper.startPage(pageNum, pageSize);
//        List<SignIn> list = signInMapper.selectByCourseId(signIn.getCourseId());
//        return PageInfo.of(list);
//    }
public PageInfo<SignIn> selectPage(Integer courseId, Integer pageNum, Integer pageSize) {
    PageHelper.startPage(pageNum, pageSize);
    List<SignIn> list = signInMapper.selectByCourseId(courseId);
    return PageInfo.of(list);
}

}