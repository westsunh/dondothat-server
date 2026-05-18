//!
package org.bbagisix.large.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bbagisix.asset.mapper.AssetMapper;
import org.bbagisix.large.dto.UserInfoDTO;
import org.bbagisix.large.mapper.LargeMapper;
import org.bbagisix.expense.domain.ExpenseVO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 지출 내역 파일 처리 서비스
 * CSV 파일을 읽어 1000건 단위로 DB에 배치 저장
 */
@Service
@Log4j2
@RequiredArgsConstructor // final 필드를 위한 생성자 자동 생성
public class LargeServiceImpl implements LargeService {
    private final LargeMapper largeMapper;

    private static final String FILE_PATH_10 = "temp/ht_expenses.txt";
    private static final String FILE_PATH_100 = "temp/large_expenses.txt";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final int BATCH_SIZE = 1000;
    private final AssetMapper assetMapper;

    private static final Long TBC = 14L; // 미지정

    /**
     * CSV 파일 한 줄씩 읽어 ExpenseVO 변환 후 DB 저장
     * 파싱 실패 시 해당 줄 skip
     */
    @Override
    @Transactional
    public void processExpenseFile() {
        log.info("start - processExpenseFile() ");
        List<ExpenseVO> largeList = new ArrayList<>();
        int totalCount = 0;

        ClassPathResource resource = new ClassPathResource(FILE_PATH_100);

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        )){
            String line;

            while((line = br.readLine()) != null){
                try{
                    // TXT 파일 형식 (CSV) : connectedId, codefTransactionId, amount, description, expenditureDate
                    String[] data = line.split(",");


                    String connectedId = data[0];
                    String codefTransactionId = data[1];
                    Long amount = Long.parseLong(data[2]);
                    String description = data[3];
                    Date expenditureDate = DATE_FORMAT.parse(data[4]);

                    UserInfoDTO info = largeMapper.getInfoByCI(connectedId);

                    if (info != null) {
                        ExpenseVO expense = ExpenseVO.builder()
                                .userId(info.getUserId())
                                .assetId(info.getAssetId())
                                .categoryId(TBC) // 미지정
                                .amount(amount)
                                .description(description)
                                .expenditureDate(expenditureDate)
                                .codefTransactionId(codefTransactionId)
                                .build();

                        largeList.add(expense);
                        totalCount++;

                        // 1000건 쌓이면 DB에 저장
                        if (largeList.size() >= BATCH_SIZE) {
                            assetMapper.insertExpenses(largeList);
                            largeList.clear();
                            log.info("✅" + totalCount + " : end (middle commit)");
                        }
                    } else {
                        log.warn("No Asset! : {}", connectedId);
                    }
                } catch (Exception e) {
                    log.error("pasing fail (line: {}) - {}", line, e.getMessage());
                }
            }
           
            if (!largeList.isEmpty()) { // 남은 데이터 마저 저장
                assetMapper.insertExpenses(largeList);
                log.info("남은 {}건 처리 완료.", largeList.size());
            }

        } catch (IOException e) {
            log.error("파일 읽기 실패: {}", FILE_PATH_100, e);
        }

        log.info("File processing end : total {}", totalCount);
    }
}