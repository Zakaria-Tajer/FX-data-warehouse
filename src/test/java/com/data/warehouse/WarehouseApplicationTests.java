package com.data.warehouse;
import static org.mockito.Mockito.*;

import com.data.warehouse.config.MessageResolver;
import com.data.warehouse.dto.ResultsDto;
import com.data.warehouse.models.Deal;
import com.data.warehouse.repository.DealRepository;
import com.data.warehouse.services.DealService;
import com.data.warehouse.validator.DealValidator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
@SpringBootTest
class WarehouseApplicationTests {


	@Autowired
	private DealValidator dealValidator;

	@Autowired
	private MessageResolver messageResolver;

	@Autowired
	private DealService dealService;


	@MockitoBean
	private DealRepository dealRepository;


	@Test
	void main_shouldRunWithoutErrors() {
		WarehouseApplication.main(new String[] {});
	}

	@Test
	void testInvalidContentType() {
		MockMultipartFile file = new MockMultipartFile("file", "data.txt", "text/plain", "invalid content".getBytes());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> dealService.importCsv(file));
		assertTrue(exception.getMessage().contains("Invalid file type"));
	}

	@Test
	void testValidDeals() {
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("USD")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now().minusMinutes(1))
				.amount(BigDecimal.valueOf(100))
				.build();

		String error = dealValidator.validate(deal);
		assertNull(error);
	}

	@Test
	void testMissingDealId() {
		Deal deal = Deal.builder()
				.dealId(null)
				.fromCurrency("USD")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now())
				.amount(BigDecimal.valueOf(100))
				.build();

		String error = dealValidator.validate(deal);
		assertEquals(messageResolver.get("error.dealid.required"), error);
	}

	@Test
	void testInvalidFromCurrency() {
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("USDX")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now())
				.amount(BigDecimal.valueOf(100))
				.build();

		String error = dealValidator.validate(deal);
		assertEquals(messageResolver.get("error.fromcurrency.invalid"), error);
	}

	@Test
	void testFutureTimestamps() {
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("USD")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now().plusDays(1))
				.amount(BigDecimal.valueOf(100))
				.build();

		String error = dealValidator.validate(deal);
		assertEquals(messageResolver.get("error.timestamp.invalid"), error);
	}

	@Test
	void testInvalidAmounts() {
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("USD")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now())
				.amount(BigDecimal.ZERO)
				.build();

		String error = dealValidator.validate(deal);
		assertEquals(messageResolver.get("error.amount.invalid"), error);
	}

	@Test
	void ProcessCsvWithoutError() {
		MockMultipartFile emptyFile = new MockMultipartFile(
				"file",
				"empty.csv",
				"text/csv",
				new byte[0]
		);



		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> dealService.importCsv(emptyFile));
		assertTrue(exception.getMessage().contains("File is empty"));

		verify(dealRepository, never()).save(any());

	}

	@Test
	void ProcessInvalidCsv() {
		String csvContent = """
            dealId,fromCurrency,toCurrency,timestamp,amount
            D001,USD,EUR,2025-05-31T10:15:30,1000.50, amount
            D002,GBP,USD,2025-05-30T14:00:00,2500.75
            """;

		MockMultipartFile file = new MockMultipartFile(
				"file",
				"mixed.csv",
				"text/csv",
				csvContent.getBytes()
		);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> dealService.importCsv(file));

		assertTrue(exception.getMessage().contains("Invalid CSV format"));

		verify(dealRepository, never()).save(any());

	}

	@Test
	void testLargeCsv() throws IOException {
		int totalDeals = 1000;
		StringBuilder csvBuilder = new StringBuilder();
		csvBuilder.append("dealId,fromCurrency,toCurrency,timestamp,amount\n");

		for (int i = 0; i < totalDeals; i++) {
			csvBuilder.append(String.format("D%04d,USD,EUR,2025-05-31T10:15:30,%.2f\n", i, 1000.0 + i));
		}

		byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);

		MockMultipartFile file = new MockMultipartFile(
				"file",
				"large_test.csv",
				"text/csv",
				new ByteArrayInputStream(csvBytes)
		);

		ResultsDto result = dealService.importCsv(file);

		assertEquals(0, result.getDuplicates());
		assertEquals(totalDeals, result.getSaved());
	}

	@Test
	void testNonCsvFiles() {
		String content = "dealId,fromCurrency,toCurrency,timestamp,amount\n"
				+ "D001,USD,EUR,2025-05-31T10:15:30,1000.50";

		MockMultipartFile file = new MockMultipartFile(
				"file",
				"test.txt",              // Not a .csv file
				"text/plain",            // Wrong content type
				content.getBytes()
		);

		assertThrows(IllegalArgumentException.class, () -> {
			dealService.importCsv(file);
		});


	}
	@Test
	void testDuplicateDeals() throws IOException {
		String csvContent = """
            dealId,fromCurrency,toCurrency,timestamp,amount
            D001,USD,EUR,2025-05-31T10:15:30,1000.50
            D002,GBP,USD,2025-05-30T14:00:00,2500.75
            D021,USD,CAD,2025-05-15T13:00:00,1000.00
            D001,EUR,JPY,2025-05-29T09:45:00,50000
            D002,GBP,USD,2025-05-30T14:00:00,2500.75
            """;

		MockMultipartFile file = new MockMultipartFile(
				"file",
				"mixed.csv",
				"text/csv",
				csvContent.getBytes()
		);


		ResultsDto result = dealService.importCsv(file);

		assertEquals(2, result.getDuplicates());


	}

	@Test
	void testInvalidDeals() throws IOException {

		File csvFile = new File("src/test/resources/test.csv");
		FileInputStream inputStream = new FileInputStream(csvFile);

		MockMultipartFile file = new MockMultipartFile(
				"file",
				"test.csv",
				"text/csv",
				inputStream
		);

		ResultsDto result = dealService.importCsv(file);

		assertEquals(4, result.getInvalid());

	}

	@Test
	void HandleAlreadyExistingDeals() throws IOException {
		String csvContent = """
            dealId,fromCurrency,toCurrency,timestamp,amount
            D001,USD,EUR,2025-05-31T10:15:30,1000.50
            D002,GBP,USD,2025-05-30T14:00:00,2500.75
            D021,USD,ZZZ,2025-05-15T13:00:00,1000.00
            D022,USD,EUR,2025-05-14T13:00:00,-100.00
            D003,EUR,JPY,2025-05-29T09:45:00,50000
            """;

		MockMultipartFile file = new MockMultipartFile(
				"file",
				"mixed.csv",
				"text/csv",
				csvContent.getBytes()
		);

		when(dealRepository.existsByDealId("D001")).thenReturn(true);
		when(dealRepository.existsByDealId("D002")).thenReturn(false);
		when(dealRepository.existsByDealId("D003")).thenReturn(false);
		when(dealRepository.existsByDealId("D021")).thenReturn(false);
		when(dealRepository.existsByDealId("D022")).thenReturn(false);

		ResultsDto result = dealService.importCsv(file);

		assertEquals(2, result.getSaved());
		assertEquals(1, result.getDuplicates());
		assertEquals(2, result.getInvalid());

		assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Invalid deal [D021]")));
		assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Invalid deal [D022]")));

		verify(dealRepository, times(2)).save(any());
	}
}
