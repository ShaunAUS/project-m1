package com.example.testSecurity.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @ApiModelProperty(value = "이름")
    private String name;
    @ApiModelProperty(value = "소개")
    private String introduce;
    @ApiModelProperty(value = "사진")
    private MultipartFile image;
    @ApiModelProperty(value = "이메일")
    private String email;
    @ApiModelProperty(value = "연락처")
    private String contactNumber;
    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "career_no")
    private Career career;
}