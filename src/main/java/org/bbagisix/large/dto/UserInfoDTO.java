package org.bbagisix.large.dto;

import lombok.*;

/**
 * connected_id 조회 결과 DTO
 * user_asset 테이블에서 userId, assetId를 함께 반환
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private Long userId;
    private Long assetId;
}
