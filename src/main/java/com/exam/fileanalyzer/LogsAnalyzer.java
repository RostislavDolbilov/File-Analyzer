package com.exam.fileanalyzer;

import org.springframework.stereotype.Service;
import java.io.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@Service
public class LogsAnalyzer {
	/**
	 * Given a zip file, a search query, and a date range,
	 * count the number of occurrences of the search query in each file in the zip file
	 *
	 * @param searchQuery  The string to search for in the file.
	 * @param zipFile      The zip file to search in.
	 * @param startDate    The start date of the search.
	 * @param numberOfDays The number of days to search for.
	 * @return A map of file names and the number of occurrences of the search query in the file.
	 */
	private final String DIRECTORY_PATH = "temporarydir/files";

	public Map<String, Integer> countEntriesInZipFile(String searchQuery, File zipFile, LocalDate startDate, Integer numberOfDays) {
		unzip(zipFile);
		String datePattern = "\\d{4}-\\d{2}-\\d{2}";
		LocalDate endDate = startDate.plusDays(numberOfDays - 1);
		List<File> files = new ArrayList<>();

		try (Stream<Path> pathStream = Files.list(Paths.get(DIRECTORY_PATH))) {
			pathStream.filter(Files::isRegularFile)
					.filter(path -> matchesDatePattern(path.getFileName().toString(), datePattern))
					.filter(path -> isWithinDateRange(extractDateFromFileName(path.getFileName().toString(), datePattern), startDate, endDate))
					.forEach(path -> {
						files.add(path.toFile());
					});
		} catch (IOException e) {
			e.printStackTrace();
		}

		return getMatchMap(files, searchQuery);
//		throw new UnsupportedEncodingException("Need to implement!");
	}

	private boolean matchesDatePattern(String fileName, String datePattern) {
		Pattern pattern = Pattern.compile(datePattern);
		Matcher matcher = pattern.matcher(fileName);
		return matcher.find();
	}

	private LocalDate extractDateFromFileName(String fileName, String datePattern) {
		Pattern pattern = Pattern.compile(datePattern);
		Matcher matcher = pattern.matcher(fileName);

		if (matcher.find()) {
			String dateString = matcher.group();
			return LocalDate.parse(dateString);
		}

		return null;
	}

	private boolean isWithinDateRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
		return date != null && !date.isBefore(startDate) && !date.isAfter(endDate);
	}

	public void unzip(File zip) {
		try {
			File destinationDirectory = new File(DIRECTORY_PATH);
			if (!destinationDirectory.exists()) {
				destinationDirectory.mkdirs();
			}

			Resource resource = new FileSystemResource(zip);
			ZipFile zipFile = new ZipFile(resource.getFile());
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File entryFile = new File(destinationDirectory, entry.getName());

				if (entry.isDirectory()) {
					entryFile.mkdirs();
				} else {
					InputStream inputStream = zipFile.getInputStream(entry);
					OutputStream outputStream = new FileOutputStream(entryFile);
					byte[] buffer = new byte[4096];
					int bytesRead;

					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}

					outputStream.close();
					inputStream.close();
				}
			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public long countOccurrences(String line, String searchString) {
		Pattern pattern = Pattern.compile(searchString);
		Matcher matcher = pattern.matcher(line);

		long count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	public Map<String, Integer> getMatchMap(List<File> files, String searchQuery){
		Map<String, Integer> searchQueryMap = new HashMap<>();
		for (File file : files){
			try {
				long count = Files.lines(Paths.get(file.getPath()))
						.map(line -> countOccurrences(line, searchQuery))
						.mapToLong(Long::valueOf)
						.sum();
				searchQueryMap.put(file.getName(), (int) count);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return searchQueryMap;
	}
}









