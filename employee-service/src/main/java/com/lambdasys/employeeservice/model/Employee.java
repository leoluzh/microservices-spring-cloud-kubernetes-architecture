package com.lambdasys.employeeservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Document(collection = "employee")
public class Employee implements Serializable {

    @Id
    private String id;
    private Long organizationId;
    private Long departmentId;
    private String name;
    private Date birthdate;
    private String position;

}
