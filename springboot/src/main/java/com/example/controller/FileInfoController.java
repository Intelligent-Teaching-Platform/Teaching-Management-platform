//package com.example.controller;
//
//import com.example.common.Result;
//import com.example.entity.FileInfo;
//import com.example.service.FileInfoService;
//import com.github.pagehelper.PageInfo;
//import jakarta.annotation.Resource;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * 文件夹管理接口
// */
//@RestController
//@RequestMapping("/fileInfo")
//public class FileInfoController {
//    @Resource
//    private FileInfoService fileInfoService;
//
//    /**
//     * 创建文件夹
//     */
//    @PostMapping("/create")
//    public Result create(@RequestBody FileInfo fileInfo) {
//        fileInfoService.create(fileInfo);
//        return Result.success(fileInfo);
//    }
//
//    /**
//     * 更新文件夹
//     */
//    @PutMapping("/update")
//    public Result update(@RequestBody FileInfo fileInfo) {
//        fileInfoService.update(fileInfo);
//        return Result.success();
//    }
//
//    /**
//     * 删除文件夹
//     */
//    @DeleteMapping("/delete/{id}")
//    public Result delete(@PathVariable Integer id) {
//        fileInfoService.delete(id);
//        return Result.success();
//    }
//
//    /**
//     * 根据ID查询文件夹
//     */
//    @GetMapping("/{id}")
//    public Result getById(@PathVariable Integer id) {
//        FileInfo fileInfo = fileInfoService.selectById(id);
//        return Result.success(fileInfo);
//    }
//
//    /**
//     * 查询文件夹列表
//     */
//    @GetMapping("/list")
//    public Result list(@RequestParam(required = false) Integer teacherId,
//                       @RequestParam(required = false) Integer courseId,
//                       @RequestParam(required = false) Integer parentId,
//                       @RequestParam(defaultValue = "1") Integer pageNum,
//                       @RequestParam(defaultValue = "100") Integer pageSize) {
//        PageInfo<FileInfo> pageInfo = fileInfoService.selectList(teacherId, courseId, parentId, pageNum, pageSize);
//        return Result.success(pageInfo);
//    }
//
//    /**
//     * 查询文件夹树形结构
//     */
//    @GetMapping("/tree")
//    public Result getTree(@RequestParam(required = false) Integer teacherId,
//                          @RequestParam(required = false) Integer courseId) {
//        List<FileInfo> fileInfoTree = fileInfoService.getFileInfoTree(teacherId, courseId);
//        return Result.success(fileInfoTree);
//    }
//
//    /**
//     * 查询子文件夹
//     */
//    @GetMapping("/children/{parentId}")
//    public Result getChildren(@PathVariable Integer parentId) {
//        List<FileInfo> children = fileInfoService.selectByParentId(parentId);
//        return Result.success(children);
//    }
//
//    /**
//     * 重命名文件夹
//     */
//    @PutMapping("/rename/{id}")
//    public Result rename(@PathVariable Integer id, @RequestParam String name) {
//        fileInfoService.rename(id, name);
//        return Result.success();
//    }
//
//    /**
//     * 移动文件夹
//     */
//    @PutMapping("/move/{id}")
//    public Result move(@PathVariable Integer id, @RequestParam(required = false) Integer targetParentId) {
//        fileInfoService.move(id, targetParentId);
//        return Result.success();
//    }
//
//    /**
//     * 获取文件夹路径
//     */
//    @GetMapping("/path/{id}")
//    public Result getPath(@PathVariable Integer id) {
//        List<FileInfo> path = fileInfoService.getFileInfoPath(id);
//        return Result.success(path);
//    }
//
//    /**
//     * 查询文件夹下的所有资料（包含子文件夹）
//     */
//    @GetMapping("/{id}/materials")
//    public Result getFileInfoMaterials(@PathVariable Integer id) {
//        List<Object> materials = fileInfoService.getFileInfoMaterials(id);
//        return Result.success(materials);
//    }
//
//    /**
//     * 批量删除文件夹
//     */
//    @DeleteMapping("/batchDelete")
//    public Result batchDelete(@RequestBody List<Integer> ids) {
//        fileInfoService.batchDelete(ids);
//        return Result.success();
//    }
//}