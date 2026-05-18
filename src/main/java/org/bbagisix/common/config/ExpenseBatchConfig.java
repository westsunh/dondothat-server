package org.bbagisix.common.config;

import org.bbagisix.large.dto.UserInfoDTO;
import org.bbagisix.large.mapper.LargeMapper;
import org.bbagisix.expense.domain.ExpenseVO;
import org.bbagisix.asset.mapper.AssetMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Spring Batch 지출 처리 Job 설정
 * CSV 파일 → Reader → Processor → Writer → expenditure 테이블 저장
 */
@Configuration
public class ExpenseBatchConfig {

    private final LargeMapper largeMapper;
    private final AssetMapper assetMapper;

    public ExpenseBatchConfig(LargeMapper largeMapper, AssetMapper assetMapper) {
        this.largeMapper = largeMapper;
        this.assetMapper = assetMapper;
    }

    private static final String FILE_PATH_10 = "temp/ht_expenses.txt";
    private static final String FILE_PATH_100 = "temp/large_expenses.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final Long TBC = 14L; // 미지정

    /**
     * CSV 파일을 읽어 ExpenseVO로 변환
     * connected_id로 user_asset 조회 → userId, assetId 획득
     * 조회 실패 또는 파싱 실패 시 null 반환 (skip)
     */
    @Bean
    public ItemReader<ExpenseVO> expenseReader() {
        FlatFileItemReader<ExpenseVO> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(FILE_PATH_10));
        reader.setLineMapper(new DefaultLineMapper<ExpenseVO>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("connectedId", "codefTransactionId", "amount", "description", "expenditureDate");
            }});
            setFieldSetMapper(fieldSet -> {
                try {
                    String connectedId = fieldSet.readString("connectedId");
                    String codefTransactionId = fieldSet.readString("codefTransactionId");
                    Long amount = Long.parseLong(fieldSet.readString("amount"));
                    String description = fieldSet.readString("description");
                    Date expenditureDate = DATE_FORMAT.parse(fieldSet.readString("expenditureDate"));

                    // userId, assetId 조회
                    UserInfoDTO info = largeMapper.getInfoByCI(connectedId);
                    if (info == null) return null; // 조회 실패 시 skip

                    return ExpenseVO.builder()
                            .userId(info.getUserId())
                            .assetId(info.getAssetId())
                            .categoryId(TBC)
                            .amount(amount)
                            .description(description)
                            .expenditureDate(expenditureDate)
                            .codefTransactionId(codefTransactionId)
                            .build();
                } catch (Exception e) {
                    return null; // 파싱 실패 시 skip
                }
            });
        }});
        return reader;
    }

    /**
     * amount <= 0 이면 skip
     */
    @Bean
    public ItemProcessor<ExpenseVO, ExpenseVO> expenseProcessor() {
        return item -> {
            // amount <= 0 이면 skip
            if (item == null || item.getAmount() <= 0) return null;
            return item;
        };
    }

    /**
     * expenditure 테이블에 bulk insert
     */
    @Bean
    public ItemWriter<ExpenseVO> expenseWriter() {
        return new ItemWriter<ExpenseVO>() {
            @Override
            public void write(List<? extends ExpenseVO> items) {
                List<ExpenseVO> expenses = items.stream()
                        .map(item -> item)  // 단순 캐스팅
                        .collect(Collectors.toCollection(ArrayList::new));
                assetMapper.insertExpenses(expenses);
            }
        };
    }

    /**
     * 50000건 단위 chunk 처리
     * 파싱/처리 실패 시 최대 10건까지 skip 허용
     */
    @Bean
    public Step expenseStep(StepBuilderFactory stepBuilderFactory,
                            ItemReader<ExpenseVO> reader,
                            ItemProcessor<ExpenseVO, ExpenseVO> processor,
                            ItemWriter<ExpenseVO> writer) {

        return stepBuilderFactory.get("expenseStep")
                .<ExpenseVO, ExpenseVO>chunk(50000) // 1000건 단위 commit
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .build();
    }

    /**
     * expenseStep으로 구성된 지출 처리 Job
     */
    @Bean
    public Job expenseJob(JobBuilderFactory jobBuilderFactory, Step expenseStep) {
        return jobBuilderFactory.get("expenseJob")
                .start(expenseStep)
                .build();
    }
}
