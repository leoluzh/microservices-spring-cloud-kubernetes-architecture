package com.lambdasys.employeeservice.controller;

import com.lambdasys.employeeservice.dto.EmployeeDto;
import com.lambdasys.employeeservice.exception.EmployeeNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;


@Tag(name="Employee API",description="Documentation Employee API v1.0.0")
public interface EmployeeDocsController {

    @Operation(summary = "Create an employee" , description = "Employee creating operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success employee creation.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeDto.class))}),
            @ApiResponse(responseCode = "400", description = "Missing required fields or wrong field range value.", content = @Content)
    })
    ResponseEntity<EmployeeDto> create(
            @Parameter(description = "employee properties")
            EmployeeDto employeeDto);

    ResponseEntity<EmployeeDto> update(String id, EmployeeDto employeeDto) throws EmployeeNotFoundException;

    @Operation(summary = "Find employee by id", description = "Returns employee found by a given id")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "Success employee found in the system.",
                    content = {@Content(mediaType = "application/json" , schema = @Schema(implementation = EmployeeDto.class))}),
            @ApiResponse(responseCode = "404", description = "Employee with given id not found.", content = @Content)
    })
    ResponseEntity<EmployeeDto> findById(
            @Parameter(description="id of employee to be searched")
            String id) throws EmployeeNotFoundException;

    @Operation(summary="Delete employee by id", description="Delete an employee found by a given valid id")
    @ApiResponses(value={
            @ApiResponse(responseCode = "204", description = "Success employee deleted in the system." , content = @Content(mediaType = "application/json")) ,
            @ApiResponse(responseCode = "404", description = "Employee with given id not found.", content = @Content)
    })
    void delete(
            @Parameter(description="id of employee to be deleted")
            String id) throws EmployeeNotFoundException;

    @Operation(summary="Listing employees", description="Return a list of employees")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json" , schema = @Schema(implementation = EmployeeDto.class))}),
    })
    ResponseEntity<Page<EmployeeDto>> findAll(Pageable pageable);


    @Operation(summary = "Listing employees by department id", description="Return a list of employees by a given department id")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json" , schema = @Schema(implementation = EmployeeDto.class))}),
    })
    ResponseEntity<Page<EmployeeDto>> findByDepartment(Long departmentId, Pageable pageable);

    @Operation(summary = "Listing employees by organization id",description="Return a list of employees by a given organization id")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json" , schema = @Schema(implementation = EmployeeDto.class))}),
    })
    ResponseEntity<Page<EmployeeDto>> findByOrganization(Long organizationId, Pageable pageable);

}
