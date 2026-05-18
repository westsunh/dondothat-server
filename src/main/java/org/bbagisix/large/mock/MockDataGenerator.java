package org.bbagisix.large.mock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 대용량 테스트 데이터 생성기
 * 1. generateUsersAndAssets() : user, user_asset 10만 건 DB 삽입
 * 2. generateExpenseFile()    : 지출 내역 CSV 파일 생성
 *
 * 실행 전 DB_URL, DB_USER, DB_PASS, OUTPUT_FILE_PATH 확인
 */
public class MockDataGenerator {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/dondothat";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    private static final int USER_COUNT = 100_000;
    private static final int TRANSACTION_COUNT = 100_000;
    private static final String OUTPUT_FILE_PATH = "C:/my-batch-files/ht_expenses.txt";
    private static final int BATCH_SIZE = 1000; // 배치 삽입 단위

    private static final Random rand = new Random();
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.out.println("대용량 목(Mock) 데이터 생성을 시작합니다...");

        try {
            // generateUsersAndAssets(); // 10만 건 지출 내역
            generateExpenseFile(); // 100만 건 지출 내열

            long endTime = System.currentTimeMillis();
            System.out.println("======================================================");
            System.out.println("🎉 모든 데이터 생성 완료!");
            System.out.println("DB User/Asset: " + USER_COUNT + "건");
            System.out.println("TXT File: " + TRANSACTION_COUNT + "건");
            System.out.println("총 소요 시간: " + (endTime - startTime) / 1000.0 + "초");
            System.out.println("TXT 파일 위치: " + OUTPUT_FILE_PATH);
            System.out.println("======================================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * user, user_asset 데이터를 1000건 단위로 DB에 배치 삽입
     */
    private static void generateUsersAndAssets() throws SQLException {
        System.out.println("1. User 및 UserAsset 10만 건 생성 시작... (DB Insert)");

        String userSql = "INSERT INTO user (name, email, password, point, nickname, asset_connected) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
        String assetSql = "INSERT INTO user_asset (user_id, asset_name, balance, bank_name, bank_account, connected_id) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement psUser = conn.prepareStatement(userSql);
             PreparedStatement psAsset = conn.prepareStatement(assetSql)) {

            conn.setAutoCommit(false); // 배치 작업을 위해 Auto-commit 해제

            for (int i = 1; i <= USER_COUNT; i++) {

                //  User 데이터 준비
                String name = "User_" + UUID.randomUUID().toString().substring(0, 8);
                psUser.setString(1, name);
                psUser.setString(2, "user_" + i + "@test.com");
                psUser.setString(3, "password_" + i);
                psUser.setLong(4, 0L);
                psUser.setString(5, "nick_" + i);
                psUser.setBoolean(6, true);
                psUser.addBatch();

                // UserAsset 데이터
                long userId = i;
                long balance = rand.nextInt(5_000_000) + 100_000;
                String connectedId = "conn_uuid_" + i;

                psAsset.setLong(1, userId);
                psAsset.setString(2, "자산_" + i); 
                psAsset.setLong(3, balance);
                psAsset.setString(4, "테스트은행"); 
                psAsset.setString(5, "000-000-" + String.format("%06d", i));
                psAsset.setString(6, connectedId);
                psAsset.addBatch();

                // 배치 사이즈 단위로 DB에 배치 실행
                if (i % BATCH_SIZE == 0) {
                    psUser.executeBatch();
                    psAsset.executeBatch();
                    System.out.println("... " + i + "건 DB 저장 완료.");
                }
            }

            // 남은 배치 실행
            psUser.executeBatch();
            psAsset.executeBatch();

            conn.commit(); // 최종 커밋
            conn.setAutoCommit(true); // Auto-commit 원복

        } catch (SQLException e) {
            System.err.println("DB 배치 작업 중 오류 발생");
            throw e;
        }
        System.out.println("1. User 및 UserAsset 10만 건 DB 생성 완료.");
    }

    /**
     * 지출 내역 CSV 파일 생성
     * 포맷: connectedId, codefTransactionId, amount, description, expenditureDate
     */
    private static void generateExpenseFile() throws IOException {
        System.out.println("2. 대용량 지출 내역 파일 생성 시작... (TXT File Write)");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE_PATH))) {
            for (int i = 1; i <= TRANSACTION_COUNT; i++) {

                int randomUserId = rand.nextInt(USER_COUNT) + 1;
                String connectedId = "conn_uuid_" + randomUserId;

                String codefTransactionId = "txn_" + UUID.randomUUID().toString();

                long amount = rand.nextInt(100_000) + 100;
                String description = "Random_Item_" + rand.nextInt(1000);
                LocalDateTime date = LocalDateTime.now().minusMinutes(rand.nextInt(365 * 24 * 60));
                String expenditureDate = date.format(DATE_FORMATTER);

                // CSV 포맷에 codefTransactionId 추가 (2번째 컬럼)
                String line = String.join(",",
                        connectedId,
                        codefTransactionId,
                        String.valueOf(amount),
                        description,
                        expenditureDate
                );

                writer.write(line);
                writer.newLine();

                if (i % (TRANSACTION_COUNT / 10) == 0) {
                    System.out.println("... " + (i * 100 / TRANSACTION_COUNT) + "% 파일 생성 중");
                }
            }
            
        } catch (IOException e) {
            System.err.println("파일 쓰기 작업 중 오류 발생: " + OUTPUT_FILE_PATH);
            throw e;
        }
        System.out.println("2. 대용량 지출 내역 파일 생성 완료.");
    }
}