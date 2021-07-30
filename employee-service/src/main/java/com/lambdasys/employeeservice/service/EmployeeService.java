package com.lambdasys.employeeservice.service;


import com.lambdasys.employeeservice.dto.EmployeeDto;
import com.lambdasys.employeeservice.exception.EmployeeNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    EmployeeDto save(final EmployeeDto dto);

    EmployeeDto update(final String id, final EmployeeDto dto) throws EmployeeNotFoundException;

    EmployeeDto findById(final String id) throws EmployeeNotFoundException;

    void delete(final String id) throws EmployeeNotFoundException;

    Page<EmployeeDto> findAll(Pageable pageable);

    Page<EmployeeDto> findByDepartament(Long departmentId, Pageable pageable);

    Page<EmployeeDto> findByOrganization(Long organizationId, Pageable pageable);

}
