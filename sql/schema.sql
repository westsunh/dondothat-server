DROP DATABASE IF EXISTS dondothat;
CREATE DATABASE dondothat;
USE dondothat;

-- 사용자 테이블
CREATE TABLE `user` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,    
    `point` BIGINT NOT NULL,
    `nickname` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `asset_connected` BOOLEAN NOT NULL DEFAULT FALSE,  -- [추가] 자산 연동 여부
    PRIMARY KEY (`user_id`)
);

-- 카테고리 테이블 (챌린지용)
CREATE TABLE `category` (
    `category_id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `icon_url` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`category_id`)
);

-- [추가] 기본 카테고리 데이터 (미분류 지출용)
INSERT INTO `category` (`category_id`, `name`, `icon_url`)
VALUES (14, '미지정', '');

-- 챌린지 테이블
CREATE TABLE `challenge` (
    `challenge_id` BIGINT NOT NULL AUTO_INCREMENT,
    `category_id` BIGINT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `summary` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`challenge_id`),
    FOREIGN KEY (`category_id`) REFERENCES `category`(`category_id`) ON DELETE CASCADE
);

-- 챌린지 참여 테이블
CREATE TABLE `user_challenge` (
    `user_challenge_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `challenge_id` BIGINT NOT NULL,
    `status` ENUM('ongoing', 'completed', 'failed') NOT NULL,
    `period` BIGINT NOT NULL,
    `progress` BIGINT NOT NULL,
    `start_date` TIMESTAMP NOT NULL,
    `end_date` TIMESTAMP NOT NULL,
    `point` BIGINT NOT NULL,
    PRIMARY KEY (`user_challenge_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE,
    FOREIGN KEY (`challenge_id`) REFERENCES `challenge`(`challenge_id`) ON DELETE CASCADE
);

-- 채팅 메시지 테이블
CREATE TABLE `chat_message` (
    `message_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `challenge_id` BIGINT NOT NULL,
    `message` VARCHAR(255) NOT NULL,
    `sent_at` TIMESTAMP NOT NULL,
    `message_type` VARCHAR(20) DEFAULT 'MESSAGE' NOT NULL,
    PRIMARY KEY (`message_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE,
    FOREIGN KEY (`challenge_id`) REFERENCES `challenge`(`challenge_id`) ON DELETE CASCADE
);

-- 지출 테이블
CREATE TABLE `expenditure` (
    `expenditure_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `category_id` BIGINT NOT NULL,
    `asset_id` BIGINT NOT NULL,
    `amount` BIGINT NOT NULL,
    `description` VARCHAR(255),
    `expenditure_date` DATETIME NOT NULL,              -- TIMESTAMP → DATETIME (배치 처리 정밀도)
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `user_modified` BOOLEAN NOT NULL DEFAULT FALSE,    -- [추가] 사용자 수동 수정 여부
    `deleted_at` TIMESTAMP NULL,                       -- [추가] 소프트 삭제용
    `codef_transaction_id` VARCHAR(255) NULL,
    PRIMARY KEY (`expenditure_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE,
    FOREIGN KEY (`category_id`) REFERENCES `category`(`category_id`) ON DELETE CASCADE
);

-- user_asset 테이블
CREATE TABLE `user_asset` (
    `asset_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `asset_name` VARCHAR(255) NOT NULL,
    `balance` BIGINT NOT NULL,
    `bank_name` VARCHAR(255) NOT NULL,
    `bank_account` VARCHAR(255) NOT NULL,
    `bank_id` VARCHAR(255),
    `bank_pw` VARCHAR(255),
    `connected_id` VARCHAR(255),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`asset_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE
);

-- 배치 Processor 조회 성능 향상을 위한 인덱스
CREATE INDEX idx_user_asset_connected_id ON `user_asset` (`connected_id`);
-- 중복 방지용 인덱스
CREATE UNIQUE INDEX uk_codef_txn_id ON expenditure (codef_transaction_id);