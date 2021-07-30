package com.lambdasys.employeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeDto implements Serializable {

    private String id;

    @NotNull
    private Long organizationId;

    @NotNull
    private Long departmentId;

    @NotBlank
    private String name;

    @NotNull
    private Date birthdate;

    @NotNull
    private String position;

}
