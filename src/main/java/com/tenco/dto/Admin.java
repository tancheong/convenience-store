package com.tenco.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    private int id; // PK
    private String adminId; // 로그인 ID (DB 컬럼명: admin_id)
    private String password; // 비밀번호
    private String name; // 관리자 이름
}
