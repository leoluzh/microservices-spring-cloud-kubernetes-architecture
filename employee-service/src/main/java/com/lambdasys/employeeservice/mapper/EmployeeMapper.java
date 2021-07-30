package com.lambdasys.employeeservice.mapper;

import com.lambdasys.employeeservice.dto.EmployeeDto;
import com.lambdasys.employeeservice.model.Employee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    Employee toEntity(EmployeeDto source);

    EmployeeDto toDto(Employee source);

}
