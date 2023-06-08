package com.exam.fileanalyzer;


import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class FileAnalyzerApplicationTests {
	@Autowired
	private LogsAnalyzer logsAnalyzer;

	@Test
	void contextLoads() {
		Map<String, Integer> expected = new HashMap<>();
		expected.put("logs_2018-03-01-access.log", 23);
		expected.put("logs_2018-02-27-access.log", 40);
		expected.put("logs_2018-02-28-access.log", 18);
		File zipFile = new File("src/test/resources/logs-27_02_2018-03_03_2018.zip");
		LocalDate startDate = LocalDate.of(2018, 2, 27);

		Map<String, Integer> forTest = this.logsAnalyzer.countEntriesInZipFile("Mozilla", zipFile, startDate, 3);

		assertThat(forTest).isEqualTo(expected);
	}
}
