package com.lambdasys.employeeservice.service;

import com.lambdasys.employeeservice.dto.EmployeeDto;
import com.lambdasys.employeeservice.exception.EmployeeNotFoundException;
import com.lambdasys.employeeservice.mapper.EmployeeMapper;
import com.lambdasys.employeeservice.model.Employee;
import com.lambdasys.employeeservice.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    @Override
    public EmployeeDto save(final EmployeeDto employeeDto) {
        var result = repository.save(mapper.toEntity(employeeDto));
        return mapper.toDto(result);
    }

    @Override
    public EmployeeDto update(final String id, final EmployeeDto employeeDto) throws EmployeeNotFoundException {
        verifiyExists(id);
        employeeDto.setId(id);
        final var result = repository.save(mapper.toEntity(employeeDto));
        return mapper.toDto(result);
    }

    @Override
    public EmployeeDto findById(final String id) throws EmployeeNotFoundException {
        final var result = repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
        return mapper.toDto(result);
    }

    @Override
    public void delete(final String id) throws EmployeeNotFoundException {
        final var result = verifiyExists(id);
        repository.delete(result);
    }

    @Override
    public Page<EmployeeDto> findAll(final Pageable pageable) {
        log.info("Employee find all: {}",pageable);
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Page<EmployeeDto> findByDepartament(final Long departmentId, final Pageable pageable) {
        log.info("Employee find all: {}",pageable);
        return repository.findByDepartmentId(departmentId, pageable).map(mapper::toDto);
    }

    @Override
    public Page<EmployeeDto> findByOrganization(final Long organizationId, final Pageable pageable) {
        log.info("Employee find all: {}",pageable);
        return repository.findByOrganizationId(organizationId, pageable).map(mapper::toDto);
    }

    protected Employee verifiyExists(final String id) throws EmployeeNotFoundException {
        return this.repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

}
