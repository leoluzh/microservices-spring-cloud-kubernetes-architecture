package com.lambdasys.employeeservice.controller;

import com.lambdasys.employeeservice.dto.EmployeeDto;
import com.lambdasys.employeeservice.exception.EmployeeNotFoundException;
import com.lambdasys.employeeservice.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/employees")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmployeeController implements EmployeeDocsController {

    private final EmployeeService service;

    @PostMapping("/")
    @Override
    public ResponseEntity<EmployeeDto> create(@RequestBody @Valid final EmployeeDto employeeDto) {
        log.info("Employee create: {}",employeeDto);
        return ResponseEntity.ok(service.save(employeeDto));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<EmployeeDto> update(@PathVariable("id") final String id, @RequestBody @Valid final EmployeeDto employeeDto) throws EmployeeNotFoundException {
        log.info("Employee update: id={} value={}",id,employeeDto);
        return ResponseEntity.ok(service.update(id, employeeDto));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<EmployeeDto> findById(@PathVariable("id") final String id) throws EmployeeNotFoundException {
        log.info("Employee find: id={}",id);
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(value= HttpStatus.NO_CONTENT)
    @Override
    public void delete(@PathVariable("id") final String id) throws EmployeeNotFoundException {
        log.info("Employee delete: id={}",id);
        service.delete(id);
    }

    @GetMapping("/")
    @Override
    public ResponseEntity<Page<EmployeeDto>> findAll(final @PageableDefault Pageable pageable) {
        log.info("Employee find all: pageable={}",pageable);
        return ResponseEntity.ok(this.service.findAll(pageable));
    }

    @GetMapping("/department/{departmentId}")
    @Override
    public ResponseEntity<Page<EmployeeDto>> findByDepartment(@PathVariable("departmentId") Long departmentId, @PageableDefault Pageable pageable) {
        log.info("Employee find by department: id={} pageable={}",departmentId,pageable);
        return ResponseEntity.ok(this.service.findByDepartament(departmentId, pageable));
    }

    @GetMapping("/organization/{organizationId}")
    @Override
    public ResponseEntity<Page<EmployeeDto>> findByOrganization(@PathVariable("organizationId") Long organizationId, @PageableDefault Pageable pageable) {
        log.info("Employee delete: id={} pageable={}",organizationId,pageable);
        return ResponseEntity.ok(this.service.findByOrganization(organizationId, pageable));
    }
}
