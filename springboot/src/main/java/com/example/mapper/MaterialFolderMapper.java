package com.example.mapper;

import com.example.entity.MaterialFolder;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MaterialFolderMapper {
    int insert(MaterialFolder folder);

    @Delete("delete from material_folder where id = #{id}")
    int deleteById(Integer id);

    List<MaterialFolder> selectByCourseId(@Param("courseId") Integer courseId);
}
