package com.example.testSecurity.Enum;


import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.Converter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum RoleType {

    ANONYMOUS("ANONYMOUS", 0),
    GENERAL_MEMBER("GENERAL_MEMBER", 1), // 게시글 참여회원

    COMPANY_MEMBER("COMPANY_MEMBER", 2), // 회사 사업번호 인증해야 부여받을수 있음 // 헤드헌팅
    ADMIN("ADMIN", 3);

    private String name;
    private Integer number;

    public static final Converter<RoleType, Integer> ROLE_TYPE_INTEGER_CONVERTER =
        context -> context.getSource() == null ? null : context.getSource().getNumber();

    public static final Converter<Integer, RoleType> INTEGER_ROLE_TYPE_CONVERTER =
        context -> context.getSource() == null ? null : RoleType.valueOf(context.getSource());

    //Enum 일치하는값
    public static RoleType valueOf(Integer i) {
        return Arrays.stream(RoleType.values())
            .filter(v -> v.getNumber().intValue() == i.intValue())
            .findFirst()
            .orElseGet(null);
    }
}
