package com.data.warehouse;
import static org.mockito.Mockito.*;

import com.data.warehouse.config.MessageResolver;
import com.data.warehouse.models.Deal;
import com.data.warehouse.repository.DealRepository;
import com.data.warehouse.services.DealService;
import com.data.warehouse.validator.DealValidator;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class WarehouseApplicationTests {


	@Autowired
	private DealValidator dealValidator;

	@Autowired
	private MessageResolver messageResolver;

	@Autowired
	private DealService dealService;


//	@Autowired
	@MockitoBean
	private DealRepository dealRepository;


	@Test
	void testValidDeal() {
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
	void testFutureTimestamp() {
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
	void testInvalidAmount() {
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
	void shouldProcessCsvWithoutError() throws IOException {
		File csvFile = new File("src/test/resources/test.csv");
		FileInputStream inputStream = new FileInputStream(csvFile);

		MockMultipartFile file = new MockMultipartFile(
				"file",
				"test.csv",
				"text/csv",
				inputStream
		);

		dealService.importCsv(file);


		verify(dealRepository, atLeastOnce()).save(any(Deal.class));
	}

}
