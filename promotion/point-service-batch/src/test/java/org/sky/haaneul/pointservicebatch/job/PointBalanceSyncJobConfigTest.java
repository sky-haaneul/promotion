package org.sky.haaneul.pointservicebatch.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.sky.haaneul.pointservicebatch.domain.PointBalance;
import org.sky.haaneul.pointservicebatch.repository.DailyPointReportRepository;
import org.sky.haaneul.pointservicebatch.repository.PointBalanceRepository;
import org.sky.haaneul.pointservicebatch.repository.PointRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class PointBalanceSyncJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils; // Job이나 Step을 실행시킬 수 있도록 SpringBoot에서 제공해주는 클래스

    @MockitoBean
    private PointRepository pointRepository;

    @MockitoBean
    private PointBalanceRepository pointBalanceRepository;

    @Autowired
    private DailyPointReportRepository dailyPointReportRepository;

    @MockitoBean
    private RedissonClient redissonClient;

    @MockitoBean
    private RMap<String, Long> balanceMap;

    @BeforeEach
    void setUp() {
        // Redis mock 설정
        when(redissonClient.<String, Long>getMap(anyString())).thenReturn(balanceMap);

        // 테스트 데이터 초기화 -> 테스트 케이스가 서로 영향이 없도록!
        dailyPointReportRepository.deleteAll();
    }

    @Test
    @DisplayName("포인트 동기화 Job 실행 성공 테스트")
    void jobExecutionTest() throws Exception {
        //given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("datetime", LocalDateTime.now().toString())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }

    @Test
    @DisplayName("Redis 캐시 동기화 Step 테스트")
    void syncPointBalanceStepTest() throws Exception {
        // given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("datetime", LocalDateTime.now().toString())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("syncPointBalanceStep", jobParameters);

        // then
        assertThat(jobExecution.getStepExecutions()).hasSize(1);
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("일별 리포트 생성 Step 테스트")
    void generateDailyReportStepTest() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("datetime", LocalDateTime.now().toString())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("generateDailyReportStep", jobParameters);

        // then
        assertThat(jobExecution.getStepExecutions()).hasSize(1);
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    }
}