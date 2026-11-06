package com.example.demo.mapper;

import com.example.demo.dto.request.DirectorRequest;
import com.example.demo.entity.Director;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DirectorMapper {

    @Mapping(target = "id", ignore = true)
    Director toEntity(DirectorRequest request);
}
