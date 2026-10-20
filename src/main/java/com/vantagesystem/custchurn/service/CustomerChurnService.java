package com.vantagesystem.custchurn.service;


import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vantagesystem.custchurn.domain.ClientDetails;
import com.vantagesystem.custchurn.repository.ClientDetailsRepository;
import com.vantagesystem.custchurn.repository.EmployeeDetailsRepository;

@Service
public class CustomerChurnService {

	private static final Logger logger = LogManager.getLogger(CustomerChurnService.class);

	private final ClientDetailsRepository clientDetailsRepository;
	private final EmployeeDetailsRepository employeeDetailsRepository;


	public CustomerChurnService(ClientDetailsRepository clientDetailsRepository, EmployeeDetailsRepository employeeDetailsRepository)
	{
		this.clientDetailsRepository = clientDetailsRepository;
		this.employeeDetailsRepository = employeeDetailsRepository;
	}

	@Transactional 
	public void updateWeeklyLoginHistory()
	{
		List<ClientDetails> clients = clientDetailsRepository.findAll();
		logger.info("Updating weekly login history for {} client(s)", clients.size());

		for (ClientDetails client : clients) {
			Integer weeklyCount = employeeDetailsRepository.getWeeklyLoginCount(client.getClientIdentifier());
			String history = client.getLoginWeeklyHistory();
			if (history == null || history.isBlank()) 
			{
				history = "[" + weeklyCount + "]";
			} else {
				history = history.trim();
				if (history.endsWith("]")) 
				{
					history = history.substring(0,history.length() - 1);
				}

				history = history + "," + weeklyCount + "]";
			}

			client.setLoginWeeklyHistory(history);

			clientDetailsRepository.save(client);

			employeeDetailsRepository.resetWeeklyLoginCount(client.getClientIdentifier());
		}
		logger.info("Finished updating weekly login history for {} client(s)", clients.size());
	}


	public void calculateAndUpdateChurnValue()
	{
		List<ClientDetails> clients = clientDetailsRepository.findAll();
		logger.info("Calculating churn values for {} client(s)", clients.size());

		for (ClientDetails client : clients) {

			String weeklyLogin = client.getLoginWeeklyHistory();

			double churnPerc2 = 0;
			double churnPerc4 = 0;
			double churnPerc12 = 0;


			if(weeklyLogin != null && weeklyLogin.trim() != "")
			{
				List<Integer> weeklyLoginHistoryList = Arrays.stream(
						weeklyLogin.replace("[", "")
						.replace("]", "")
						.split(","))
						.map(Integer::parseInt)
						.toList();
				int listSize = weeklyLoginHistoryList.size();
				if(listSize > 3)
				{
					churnPerc2 = calPercChange(weeklyLoginHistoryList, listSize, 4);
					if(churnPerc2 != 0)
						client.setChurnRate2Weeks(BigDecimal.valueOf(churnPerc2));
				}
				if(listSize > 7)
				{
					churnPerc4 = calPercChange(weeklyLoginHistoryList, listSize, 8);
					if(churnPerc4 != 0)
						client.setChurnRate4Weeks(BigDecimal.valueOf(churnPerc4));
				}
				if(listSize > 23)
				{
					churnPerc12 = calPercChange(weeklyLoginHistoryList, listSize, 24);
					if(churnPerc12 != 0)
						client.setChurnRate12Weeks(BigDecimal.valueOf(churnPerc12));
				}

				clientDetailsRepository.save(client);
			}
		}
		logger.info("Finished calculating churn values for {} client(s)", clients.size());
	}


	public void generateChurnReport()
	{
		logger.info("Generating client churn report");
		try (Workbook workbook = new XSSFWorkbook()) {

			Sheet sheet = workbook.createSheet("Client Churn Report");

			// Header Style
			CellStyle headerStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerStyle.setFont(headerFont);

			// Column Names
			String[] columns = {
					"Sl.No",
					"Client Name",
					"Client Identifier",
					"License Start Date",
					"License End Date",
					"ChurnRate2Weeks",
					"ChurnRate4Weeks",
					"ChurnRate12Weeks",
					"Need Action"
			};

			// Header Row
			Row headerRow = sheet.createRow(0);

			for (int i = 0; i < columns.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerStyle);
			}

			List<ClientDetails> clientDtlsList = clientDetailsRepository.findAll();

			int i = 0;
			for(ClientDetails clientDtls : clientDtlsList)
			{
				i++;
				Row row = sheet.createRow(i);
				row.createCell(0).setCellValue(i);
				row.createCell(1).setCellValue(clientDtls.getName());
				row.createCell(2).setCellValue(clientDtls.getClientIdentifier());
				row.createCell(3).setCellValue(clientDtls.getLicenseStartDateTime());
				row.createCell(4).setCellValue(clientDtls.getLicenseEndDateTime());

				double churnRate2Weeks = 0;
				double churnRate4Weeks = 0;
				double churnRate12Weeks = 0;

				if(clientDtls.getChurnRate2Weeks() != null)
				{
					churnRate2Weeks = clientDtls.getChurnRate2Weeks().doubleValue();
					row.createCell(5).setCellValue(churnRate2Weeks);
				}
				if(clientDtls.getChurnRate4Weeks() != null)
				{
					churnRate4Weeks = clientDtls.getChurnRate4Weeks().doubleValue();
					row.createCell(6).setCellValue(churnRate4Weeks);
				}
				if(clientDtls.getChurnRate12Weeks() != null)
				{
					churnRate12Weeks = clientDtls.getChurnRate12Weeks().doubleValue();
					row.createCell(7).setCellValue(churnRate12Weeks);
				}

				//move the values to porperties
				if(churnRate2Weeks <= -20 || churnRate4Weeks <= -35 || churnRate12Weeks <= -40)
					row.createCell(8).setCellValue("YES");
				else
					row.createCell(8).setCellValue("NO");

				// Auto-size columns
				for (int col = 0; col < columns.length; col++) {
					sheet.autoSizeColumn(col);
				}

				
				
			}
			
			// Save file under resources folder
			String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			String fileName = "src/main/resources/ClientChurnReport_" + today + ".xlsx";
			Path filePath = Paths.get(fileName);

			try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
				workbook.write(fos);
				logger.info("Generated client churn report at {}", filePath);
			} catch (Exception e) {
				logger.error("Failed to write client churn report to {}", filePath, e);
			}
		}catch (Exception e) {
			logger.error("Failed to generate client churn report", e);
		}
	}


	private double calPercChange(List<Integer> weeklyLoginHistoryList, int listSize, int window)
	{
		List<Integer> calcPeriodWeekHistoryList = weeklyLoginHistoryList.subList(listSize-window, listSize);
		int splitSize = calcPeriodWeekHistoryList.size() / 2;
		double previousPeridAvg = calcPeriodWeekHistoryList.subList(0, splitSize)
				.stream()
				.mapToInt(Integer::intValue)
				.average()
				.orElse(0);

		double recentPeridAvg = calcPeriodWeekHistoryList.subList(splitSize, calcPeriodWeekHistoryList.size())
				.stream()
				.mapToInt(Integer::intValue)
				.average()
				.orElse(0);

		double percChange = ((recentPeridAvg - previousPeridAvg) / previousPeridAvg) * 100;
		return percChange;
	}
}
