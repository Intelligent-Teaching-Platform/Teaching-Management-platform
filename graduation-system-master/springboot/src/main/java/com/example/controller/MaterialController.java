package com.example.controller;

import com.example.common.Result;
import com.example.entity.Material;
import com.example.entity.MaterialFolder;
import com.example.service.MaterialFolderService;
import com.example.service.MaterialService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 资料信息模块前端请求接口入口
 */
@RestController
@RequestMapping("/material")
public class MaterialController {
    @Resource
    private MaterialService materialService;
    @Resource
    private MaterialFolderService materialFolderService;

    /**
     * 增加 - 仅用于新增资料记录（不包含文件上传）
     */
    @PostMapping("/add")
    public Result add(@RequestBody Material material) {
        materialService.add(material);
        return Result.success();
    }

    /** 按课程ID查询资料列表（教师/学生查看当前课程资料） */
    @GetMapping("/listByCourse")
    public Result listByCourse(@RequestParam Integer courseId) {
        return Result.success(materialService.selectByCourseId(courseId));
    }

    /** 获取某课程下的文件夹列表（教师上传时选择或新建） */
    @GetMapping("/folders")
    public Result listFolders(@RequestParam Integer courseId) {
        return Result.success(materialFolderService.listByCourseId(courseId));
    }

    /** 新建文件夹 */
    @PostMapping("/folder")
    public Result createFolder(@RequestParam String name,
                               @RequestParam Integer courseId,
                               @RequestParam(required = false) Integer parentId,
                               @RequestParam Integer teacherId) {
        MaterialFolder f = materialFolderService.create(name, courseId, parentId, teacherId);
        return Result.success(f);
    }

    /** 教师上传课程资料（可选 folderId，不传或 null 表示根目录） */
    @PostMapping("/upload")
    public Result upload(@RequestParam("files") MultipartFile[] files,
                        @RequestParam Integer courseId,
                        @RequestParam Integer teacherId,
                        @RequestParam(required = false) String teacherName,
                        @RequestParam(required = false) Integer folderId) {
        try {
            return Result.success(materialService.uploadFiles(files, courseId, teacherId, teacherName, folderId));
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /** 下载资料（根据资料ID） */
    @GetMapping("/download/{id}")
    public void download(@PathVariable Integer id, HttpServletResponse response) {
        try {
            materialService.downloadFile(id, response);
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("下载失败: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }

    /** 删除资料（仅教师） */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        materialService.deleteById(id);
        return Result.success();
    }

    /**
     * 上传文件（旧注释保留）
     */
//    @PostMapping("/upload")
//    public Result upload(@RequestParam(value = "files", required = false) MultipartFile[] files,
//                         @RequestParam(required = false) Integer folderId,
//                         @RequestParam(required = false) Integer teacherId,
//                         @RequestParam(required = false) String teacherName,
//                         @RequestParam(required = false) Integer courseId) {
//        try {
//            Material material = materialService.uploadFiles(files, folderId, teacherId, teacherName, courseId);
//            return Result.success(material);
//        } catch (Exception e) {
//            return Result.error("文件上传失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 批量上传文件
//     */
//    @PostMapping("/batchUpload")
//    public Result batchUpload(@RequestParam("files") MultipartFile[] files,
//                              @RequestParam(required = false) Integer folderId,
//                              @RequestParam Integer teacherId) {
//        try {
//            List<Material> materials = materialService.batchUploadFiles(files, folderId, teacherId);
//            return Result.success(materials);
//        } catch (Exception e) {
//            return Result.error("批量上传失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 更新
//     */
//    @PutMapping("/update")
//    public Result update(@RequestBody Material material) {
//        materialService.updateByID(material);
//        return Result.success();
//    }
//
//    /**
//     * 分页查询接口
//     */
//    @GetMapping("/selectPage")
//    public Result selectPage(Material material,
//                             @RequestParam(defaultValue = "1") Integer pageNum,
//                             @RequestParam(defaultValue = "10") Integer pageSize) {
//        PageInfo<Material> pageInfo = materialService.selectPage(material, pageNum, pageSize);
//        return Result.success(pageInfo);
//    }

//    /**
//     * 列表查询（支持分页和条件查询）
//     */
//    @GetMapping("/list")
//    public Result list(@RequestParam(required = false) Integer teacherId,
//                       @RequestParam(required = false) Integer studentId,
//                       @RequestParam(required = false) Integer folderId,
//                       @RequestParam(required = false) Integer courseId,
//                       @RequestParam(required = false) String keyword,
//                       @RequestParam(defaultValue = "1") Integer pageNum,
//                       @RequestParam(defaultValue = "10") Integer pageSize) {
//        PageInfo<Material> pageInfo = materialService.selectList(teacherId, studentId, folderId, courseId, keyword, pageNum, pageSize);
//        return Result.success(pageInfo);
//    }
//
//    /**
//     * 按ID查询
//     */
//    @GetMapping("/{id}")
//    public Result getById(@PathVariable Integer id) {
//        Material material = materialService.selectById(id);
//        return Result.success(material);
//    }
//
//    /**
//     * 删除
//     */
//    @DeleteMapping("/delete/{id}")
//    public Result deleteById(@PathVariable Integer id) {
//        materialService.deleteById(id);
//        return Result.success();
//    }
//
//    /**
//     * 批量删除
//     */
//    @DeleteMapping("/batchDelete")
//    public Result batchDelete(@RequestBody List<Integer> ids) {
//        materialService.batchDelete(ids);
//        return Result.success();
//    }
//
    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Material> list = materialService.selectAll();
        return Result.success(list);
    }
//
//    /**
//     * 根据教师ID查询资料
//     */
//    @GetMapping("/teacher/{teacherId}")
//    public Result selectByTeacherId(@PathVariable Integer teacherId) {
//        List<Material> list = materialService.selectByTeacherId(teacherId);
//        return Result.success(list);
//    }
//
//    /**
//     * 根据课程ID查询资料
//     */
//    @GetMapping("/course/{courseId}")
//    public Result selectByCourseId(@PathVariable Integer courseId,
//                                   @RequestParam(required = false) Integer studentId) {
//        List<Material> list = materialService.selectByCourseId(courseId, studentId);
//        return Result.success(list);
//    }
//
//    /**
//     * 根据文件夹ID查询资料
//     */
//    @GetMapping("/folder/{folderId}")
//    public Result selectByFolderId(@PathVariable Integer folderId) {
//        List<Material> list = materialService.selectByFolderId(folderId);
//        return Result.success(list);
//    }
//
//    /**
//     * 下载文件
//     */
//    @GetMapping("/download/{id}")
//    public void download(@PathVariable Integer id, HttpServletResponse response) {
//        try {
//            materialService.downloadFile(id, response);
//        } catch (Exception e) {
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            try {
//                response.getWriter().write("文件下载失败: " + e.getMessage());
//            } catch (Exception ex) {
//                // ignore
//            }
//        }
//    }
//
//    /**
//     * 获取文件统计信息
//     */
//    @GetMapping("/statistics")
//    public Result getStatistics(@RequestParam(required = false) Integer teacherId,
//                                @RequestParam(required = false) Integer courseId) {
//        Map<String, Object> statistics = materialService.getStatistics(teacherId, courseId);
//        return Result.success(statistics);
//    }
//
//    /**
//     * 移动文件到其他文件夹
//     */
//    @PutMapping("/move")
//    public Result moveToFolder(@RequestParam Integer materialId,
//                               @RequestParam(required = false) Integer targetFolderId) {
//        materialService.moveToFolder(materialId, targetFolderId);
//        return Result.success();
//    }
//
//    /**
//     * 搜索资料
//     */
//    @GetMapping("/search")
//    public Result search(@RequestParam String keyword,
//                         @RequestParam(required = false) Integer teacherId,
//                         @RequestParam(required = false) Integer courseId,
//                         @RequestParam(defaultValue = "1") Integer pageNum,
//                         @RequestParam(defaultValue = "10") Integer pageSize) {
//        PageInfo<Material> pageInfo = materialService.search(keyword, teacherId, courseId, pageNum, pageSize);
//        return Result.success(pageInfo);
//    }
}