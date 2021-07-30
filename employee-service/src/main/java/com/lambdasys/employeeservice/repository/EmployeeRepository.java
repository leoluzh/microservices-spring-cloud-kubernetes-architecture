package com.lambdasys.employeeservice.repository;

import com.lambdasys.employeeservice.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends PagingAndSortingRepository<Employee,String> {

    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);
    Page<Employee> findByOrganizationId(Long organizationId, Pageable pageable);

}
